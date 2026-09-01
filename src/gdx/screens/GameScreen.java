package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gdx.render.AnimatedEntity;
import gdx.render.PamAssets;
import gdx.render.PlantVisualManager;
import gdx.render.ZombieVisualManager;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.ImageUtils;
import gdx.util.SoundManager;

import model.game.Board;
import model.game.GameSession;
import model.game.Grave;
import model.game.Tile;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.projectile.Projectile;
import model.sun.FallingSun;
import model.user.User;
import model.zombie.Zombie;

import java.util.List;

/**
 * Main gameplay screen (fixed).
 */
public class GameScreen implements Screen {

    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // Board configuration
    private static final float BOARD_LEFT = 260f;
    private static final float BOARD_TOP = 640f;
    private static final float TILE_W = 100f;
    private static final float TILE_H = 96f;

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final GameSession session;
    private final int chapter;
    private final int level;

    private final Table sidebarTable = new Table();
    private final Table hudTable = new Table();
    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Table zombieProgressBar = new Table();

    private final ZombieVisualManager zombieVisuals = new ZombieVisualManager();
    private final PlantVisualManager plantVisuals = new PlantVisualManager();

    private String selectedPlantToPlant = null; // Plant to be planted
    private boolean shovelSelected = false;      // Shovel selected
    private boolean plantFoodSelected = false;   // Plant food selected

    private float tickAccumulator = 0f;
    private static final float SECONDS_PER_TICK = 0.1f; // Defined by model

    private boolean announcedGameOver = false;

    public GameScreen(PvZGame game, GameSession session, int chapter, int level) {
        this.game = game;
        this.session = session;
        this.chapter = chapter;
        this.level = level;
        this.skin = game.getSkin();

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        sunLabel = new Label("0", skin);
        plantFoodLabel = new Label("0", skin);

        buildHud();
        buildSidebar();
        buildZombieProgressBar();

        SoundManager.playMusic(AssetPaths.MUSIC_MENU);

        Gdx.input.setInputProcessor(stage);
    }

    // ==================== HUD Construction ====================

    private void buildHud() {
        // --- Pause menu ---
        TextButton pauseButton = new TextButton("II", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                openPauseMenu();
            }
        });

        // --- Sun counter ---
        Table sunBox = new Table();
        sunBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(36f).padRight(4f);
        sunBox.add(sunLabel).padRight(16f);

        // --- Plant food counter ---
        Table foodBox = new Table();
        foodBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_PLANT_FOOD_LEAF))).size(30f).padRight(4f);
        foodBox.add(plantFoodLabel).padRight(16f);

        // --- Coins and Diamonds ---
        User user = session.getUser();
        Table coinBox = new Table();
        coinBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_COIN))).size(28f).padRight(4f);
        coinBox.add(new Label(String.valueOf(user.getCoins()), skin)).padRight(16f);

        Table diamondBox = new Table();
        diamondBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_DIAMOND))).size(28f).padRight(4f);
        diamondBox.add(new Label(String.valueOf(user.getDiamonds()), skin)).padRight(16f);

        hudTable.setFillParent(true);
        hudTable.top();
        Table topRow = new Table();
        topRow.add(pauseButton).size(48f).padRight(16f);
        topRow.add(sunBox);
        topRow.add(foodBox);
        topRow.add(coinBox);
        topRow.add(diamondBox);

        // --- Debug functions ---
        if (user.isDebugMode()) {
            TextButton addSunCheat = new TextButton("+ Sun", skin);
            addSunCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    session.getSunManager().addSun(50);
                }
            });
            TextButton addFoodCheat = new TextButton("+ Plant Food", skin);
            addFoodCheat.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    session.addPlantFood();
                }
            });
            topRow.add(addSunCheat).padLeft(8f);
            topRow.add(addFoodCheat).padLeft(8f);
        }

        hudTable.add(topRow).padTop(8f).row();
        stage.addActor(hudTable);
    }

    private void buildZombieProgressBar() {
        zombieProgressBar.setFillParent(false);
        Table wrapper = new Table();
        wrapper.setFillParent(true);
        wrapper.top();
        wrapper.add(zombieProgressBar).width(500f).height(24f).padTop(8f);
        stage.addActor(wrapper);
    }

    private void refreshZombieProgressBar() {
        zombieProgressBar.clear();
        int totalWaves = session.getWaveManager().getTotalWaves();
        int currentWave = session.getWaveManager().getCurrentWave();
        float ratio = totalWaves <= 0 ? 0f : Math.min(1f, currentWave / (float) totalWaves);

        Table filled = new Table();
        filled.setBackground(skin.newDrawable("white", Color.RED));
        Table empty = new Table();
        empty.setBackground(skin.newDrawable("white", Color.DARK_GRAY));

        zombieProgressBar.add(filled).width(500f * ratio).height(24f);
        zombieProgressBar.add(empty).width(500f * (1 - ratio)).height(24f);
    }

    private void buildSidebar() {
        sidebarTable.setFillParent(true);
        sidebarTable.top().left();
        sidebarTable.padTop(60f).padLeft(8f);

        List<String> selectedPlants = game.getPlantSelectionController().getSelectedPlants();
        Table cardsTable = new Table();
        for (String plantName : selectedPlants) {
            cardsTable.add(buildPlantCard(plantName)).size(70f, 90f).pad(2f).row();
        }
        sidebarTable.add(cardsTable).row();

        // --- Shovel button ---
        TextButton shovelButton = new TextButton("Shovel", skin);
        shovelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playSound(AssetPaths.SFX_SHOVEL);
                shovelSelected = !shovelSelected;
                selectedPlantToPlant = null;
                plantFoodSelected = false;
            }
        });
        sidebarTable.add(shovelButton).padTop(10f).width(70f).row();

        // --- Plant food button ---
        TextButton foodButton = new TextButton("Plant Food", skin);
        foodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (session.getPlantFoodCount() > 0) {
                    SoundManager.playSound(AssetPaths.SFX_MINT);
                    plantFoodSelected = !plantFoodSelected;
                    selectedPlantToPlant = null;
                    shovelSelected = false;
                }
            }
        });
        sidebarTable.add(foodButton).padTop(6f).width(70f).row();

        stage.addActor(sidebarTable);
    }

    private Stack buildPlantCard(String plantName) {
        Stack stack = new Stack();
        Image bg = new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND));
        Image icon = new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(plantName)));
        Label nameLabel = new Label(plantName, skin);
        nameLabel.setFontScale(0.5f);

        stack.add(bg);
        stack.add(icon);
        Table overlay = new Table();
        overlay.bottom();
        overlay.add(nameLabel);
        stack.add(overlay);

        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (session.isPlantOnCooldown(plantName)) {
                    return; // On cooldown
                }
                selectedPlantToPlant = plantName;
                shovelSelected = false;
                plantFoodSelected = false;
            }
        });
        return stack;
    }

    // ==================== Pause Menu ====================

    private void openPauseMenu() {
        game.setScreen(new PauseScreen(game, this, this::restartLevel));
    }

    private void restartLevel() {
        GameSession fresh = new GameSession(session.getUser(), session.getWaveManager().getTotalWaves());
        game.setScreen(new GameScreen(game, fresh, chapter, level));
    }

    // ==================== Render Loop ====================

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!session.isGameOver()) {
            tickAccumulator += delta * session.getUser().getGameSpeed();
            while (tickAccumulator >= SECONDS_PER_TICK) {
                tickAccumulator -= SECONDS_PER_TICK;
                session.advanceOneTick();
                zombieVisuals.sync(session.getAliveZombies(), new ZombieVisualManager.TileMapper() {
                    public float x(double col) { return tileXFloat((float) col); }
                    public float y(int row) { return tileY(row); }
                });
                plantVisuals.sync(session.getBoard(), new PlantVisualManager.TileMapper() {
                    public float x(int col) { return tileX(col); }
                    public float y(int row) { return tileY(row); }
                });
                zombieVisuals.update(delta);
                plantVisuals.update(delta);
                PamAssets.get().update();

                FallingSun newSun = session.getSunManager().tick(session.getBoard());
                if (newSun != null) {
                    session.getFallingSuns().add(newSun);
                }
                for (FallingSun fs : session.getFallingSuns()) {
                    fs.tick();
                }
            }
        } else if (!announcedGameOver) {
            announcedGameOver = true;
            int score = session.getUser().getHighScore();
            game.setScreen(new WinLossScreen(game, session.isWon(), chapter, level, score, this::restartLevel));
            return;
        }

        // --- Background drawing ---
        stage.getBatch().begin();
        String bg = seasonBackground();
        if (!bg.isEmpty()) {
            stage.getBatch().draw(ImageUtils.loadRegion(bg), 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }
        drawBoard(stage.getBatch());
        drawGraves(stage.getBatch());
        drawLawnMowers(stage.getBatch());
        drawPlants(stage.getBatch());
        drawZombies(stage.getBatch());
        drawProjectiles(stage.getBatch());
        drawFallingSuns(stage.getBatch());
        stage.getBatch().end();

        // --- Update HUD labels ---
        sunLabel.setText(String.valueOf(session.getSunManager().getCurrentSun()));
        plantFoodLabel.setText(String.valueOf(session.getPlantFoodCount()));
        refreshZombieProgressBar();

        stage.act(delta);
        stage.draw();

        handleBoardClick();
    }

    private String seasonBackground() {
        // Simplified فصل (Chapter) fallback
        switch (chapter) {
            case 1: return AssetPaths.BG_LAWN_ANCIENT_EGYPT;
            case 2: return AssetPaths.BG_LAWN_FROSTBITE_CAVES;
            case 3: return AssetPaths.BG_LAWN_BIG_WAVE_BEACH;
            case 4: return AssetPaths.BG_LAWN_DARK_AGES;
            default: return AssetPaths.BG_LAWN_NORMAL;
        }
    }

    private String seasonMowerPath(boolean used) {
        switch (chapter) {
            case 1: return used ? AssetPaths.LAWN_MOWER_USED_EGYPT : AssetPaths.LAWN_MOWER_IDLE_EGYPT;
            case 2: return used ? AssetPaths.LAWN_MOWER_USED_ICEAGE : AssetPaths.LAWN_MOWER_IDLE_ICEAGE;
            case 3: return used ? AssetPaths.LAWN_MOWER_USED_BEACH : AssetPaths.LAWN_MOWER_IDLE_BEACH;
            case 4: return used ? AssetPaths.LAWN_MOWER_USED_DARK : AssetPaths.LAWN_MOWER_IDLE_DARK;
            default: return used ? AssetPaths.LAWN_MOWER_USED_NORMAL : AssetPaths.LAWN_MOWER_IDLE_NORMAL;
        }
    }

    // ==================== Entity Drawing ====================
    private float tileXFloat(float col) {
        return BOARD_LEFT + col * TILE_W;
    }
    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        // Implement special conveyor features here if needed
        if (!AssetPaths.CONVEYOR_BELT_BG.isEmpty()) {
            // TODO: Special conveyor logic
        }
    }

    private void drawGraves(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                if (tile.hasGrave()) {
                    String path = graveTexturePath(tile.getGrave());
                    batch.draw(ImageUtils.loadRegion(path), tileX(c), tileY(r), TILE_W, TILE_H);
                }
                if (tile.isNecromancyTile() && !AssetPaths.NECROMANCY_TILE_MARKER.isEmpty()) {
                    batch.draw(ImageUtils.loadRegion(AssetPaths.NECROMANCY_TILE_MARKER), tileX(c), tileY(r), TILE_W, TILE_H);
                }
            }
        }
    }

    private String graveTexturePath(Grave grave) {
        double ratio = grave.getHealth() / 700.0;
        if (ratio > 0.66) return AssetPaths.GRAVE_TYPE_1;
        if (ratio > 0.33) return AssetPaths.GRAVE_TYPE_2;
        return AssetPaths.GRAVE_TYPE_3;
    }

    private void drawLawnMowers(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            if (!board.isLawnMowerAvailable(r)) {
                continue; // Level without lawn mowers
            }
            String path = seasonMowerPath(board.isLawnMowerUsed(r));
            batch.draw(ImageUtils.loadRegion(path), BOARD_LEFT - 50f, tileY(r), 44f, TILE_H);
        }
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        plantVisuals.draw(batch);

        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (plant != null && plant.getHealth() < plant.getMaxHealth()) {
                    drawHealthBar(batch, tileX(c) + 8f, tileY(r) + TILE_H - 6f, TILE_W - 16f,
                            plant.getHealth() / (float) plant.getMaxHealth());
                }
            }
        }
    }
    private void drawZombies(com.badlogic.gdx.graphics.g2d.Batch batch) {
        zombieVisuals.draw(batch);

        // نوار سلامتی همچنان طبق منطق قدیمی رسم می‌شه (اختیاری، نگه‌داشتیم)
        for (Zombie z : session.getAliveZombies()) {
            float x = tileX(0) + (float) z.getXPosition() * TILE_W;
            float y = tileY(z.getRow());
            drawHealthBar(batch, x, y + TILE_H - 16f, TILE_W - 10f, z.getHealth() / (float) z.getMaxHealth());
        }
    }
    private void drawProjectiles(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Projectile p : session.getActiveProjectiles()) {
            if (p.isDead()) {
                continue;
            }
            String path = p.isFire() ? AssetPaths.PROJECTILE_FIRE
                    : (p.isIce() ? AssetPaths.PROJECTILE_ICE : AssetPaths.PROJECTILE_NORMAL);
            TextureRegion tex = ImageUtils.loadRegion(path);
            float x = tileX(0) + (float) p.getX() * TILE_W;
            float y = tileY(p.getRow()) + TILE_H / 2f - 8f;
            batch.draw(tex, x, y, 20f, 20f);
        }
    }

    private void drawFallingSuns(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (FallingSun fs : session.getFallingSuns()) {
            String path;
            switch (fs.getKind()) {
                case SPECIAL: path = AssetPaths.SUN_SPECIAL; break;
                case RADIOACTIVE: path = AssetPaths.SUN_RADIOACTIVE; break;
                default: path = AssetPaths.SUN_NORMAL; break;
            }
            TextureRegion tex = ImageUtils.loadRegion(path);
            float x = tileX(fs.getCol()) + TILE_W / 2f - 16f;
            float y = fs.isLanded() ? tileY(fs.getRow()) + TILE_H / 2f - 16f
                    : tileY(fs.getRow()) + TILE_H + 40f; // Before landing
            batch.draw(tex, x, y, 32f, 32f);
        }
    }

    private void drawHealthBar(com.badlogic.gdx.graphics.g2d.Batch batch, float x, float y, float width, float ratio) {
        // TODO: Advanced drawing (using simple white pixel texture colored)
    }

    // ==================== User Input Handler ====================

    private void handleBoardClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        com.badlogic.gdx.math.Vector2 touch = stage.screenToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));

        // Collect special falling suns
        for (int i = 0; i < session.getFallingSuns().size(); i++) {
            FallingSun fs = session.getFallingSuns().get(i);
            float x = tileX(fs.getCol()) + TILE_W / 2f - 16f;
            float y = fs.isLanded() ? tileY(fs.getRow()) + TILE_H / 2f - 16f : tileY(fs.getRow()) + TILE_H + 40f;
            if (touch.x >= x && touch.x <= x + 32f && touch.y >= y && touch.y <= y + 32f) {
                collectSunAt(fs.getRow(), fs.getCol());
                return;
            }
        }

        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H) ;
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return; // Clicked outside the grid
        }

        Tile tile = session.getBoard().getTile(row, col);

        if (shovelSelected) {
            if (tile.getPlant() != null) {
                tile.setPlant(null);
            }
            shovelSelected = false;
            return;
        }

        if (plantFoodSelected) {
            if (tile.getPlant() != null && session.getPlantFoodCount() > 0) {
                tile.getPlant().feed(session);
            }
            plantFoodSelected = false;
            return;
        }

        if (selectedPlantToPlant != null) {
            plantSelectedAt(row, col);
            return;
        }

        // Collect sun directly from producer plant
        collectSunAt(row, col);
    }

    private void collectSunAt(int row, int col) {
        List<FallingSun> suns = session.getFallingSuns();
        for (int i = 0; i < suns.size(); i++) {
            FallingSun fs = suns.get(i);
            if (fs.getRow() == row && fs.getCol() == col) {
                int amount = fs.getKind().getValue();
                suns.remove(i);
                session.getSunManager().addSun(amount);
                SoundManager.playSound(AssetPaths.SFX_SUN);
                return;
            }
        }
        Tile tile = session.getBoard().getTile(row, col);
        if (tile != null && tile.getPlant() instanceof model.plant.interfaces.ISunProducer) {
            model.plant.interfaces.ISunProducer producer = (model.plant.interfaces.ISunProducer) tile.getPlant();
            if (producer.isSunReady()) {
                session.getSunManager().addSun(producer.getReadySunAmount());
                producer.collectSun();
                SoundManager.playSound(AssetPaths.SFX_SUN);
            }
        }
    }

    private void plantSelectedAt(int row, int col) {
        String type = selectedPlantToPlant;
        if (session.isPlantOnCooldown(type)) {
            selectedPlantToPlant = null;
            return;
        }
        Tile tile = session.getBoard().getTile(row, col);
        // Simplified syntax check to ensure type exists. Actual logic in creating/validating plant.
        Plant testPlant = PlantFactory.create(type);
        if (tile == null || testPlant == null || !tile.canPlant(testPlant)) {
            return;
        }
        Plant plant = PlantFactory.create(type);
        User user = session.getUser();
        int currentPlantLevel = user.getPlantLevel(type);
        plant.applyUpgradeLevel(currentPlantLevel);

        if (user.hasGreenhouseBoost(type)) {
            plant.setGreenhouseBoosted(true);
            user.consumeGreenhouseBoost(type);
        }
        if (!session.getSunManager().spendSun(plant.getSunCost())) {
            return; // Not enough sun
        }
        plant.place(row, col);
        tile.setPlant(plant);
        session.startPlantCooldown(type, plant.getCooldownTicks());
        selectedPlantToPlant = null;
        SoundManager.playSound(AssetPaths.SFX_PLANT);
    }

    // ==================== Standard Screen Methods ====================

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}