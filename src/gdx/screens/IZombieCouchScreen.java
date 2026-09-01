package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.ImageUtils;
import gdx.util.SoundManager;

import model.game.Board;
import model.game.Tile;
import model.minigame.IZombieSession;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.zombie.Zombie;

/**
 * بخش امتیازی: بازی دونفره‌ی «من، زامبی» به‌صورت Couch Play روی یک دستگاه،
 * بدون نیاز به شبکه (طبق سند فاز ۳، بخش امتیازی).
 *
 * بازیکن نقش گیاهان با ماوس (کلیک روی خانه‌ها) بازی می‌کند و بازیکن نقش
 * زامبی‌ها با صفحه‌کلید: کلیدهای جهت‌دار برای حرکت مکان‌نما، کلیدهای ۱ تا ۴
 * برای انتخاب نوع زامبی، و Space/Enter برای قرار دادن زامبی در مکان‌نما.
 */
public class IZombieCouchScreen implements Screen {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float BOARD_LEFT = 260f;
    private static final float BOARD_TOP = 640f;
    private static final float TILE_W = 100f;
    private static final float TILE_H = 96f;
    private static final float SECONDS_PER_TICK = 0.1f;

    private static final String[] ZOMBIE_TYPES = {"normal", "conehead", "buckethead", "imp"};
    private static final String[] PLANT_TYPES = {"peashooter", "sunflower", "wallnut", "potatomine", "squash"};

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final IZombieSession session;
    private final int level;

    private final Label sunLabel;
    private final Label statusLabel;
    private final Image cursorImage;

    private float tickAccumulator = 0f;
    private boolean announcedGameOver = false;

    private String selectedPlantType = null;
    private String selectedZombieType = ZOMBIE_TYPES[0];
    private int cursorRow = 0;
    private int cursorCol = Board.COLS - 1;

    public IZombieCouchScreen(PvZGame game, int level) {
        this.game = game;
        this.level = level;
        this.skin = game.getSkin();
        this.session = new IZombieSession(game.getLoggedInUser(), level);

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        sunLabel = new Label("0", skin);
        statusLabel = new Label("", skin);
        cursorImage = new Image(skin.newDrawable("white", new Color(1f, 1f, 0f, 0.35f)));
        cursorImage.setSize(TILE_W - 6f, TILE_H - 6f);
        stage.addActor(cursorImage);

        buildHud();
        buildSidebars();

        SoundManager.playMusic(AssetPaths.MUSIC_MINIGAME);
        Gdx.input.setInputProcessor(stage);
    }

    private void buildHud() {
        Table top = new Table();
        top.setFillParent(true);
        top.top();

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMiniGames();
            }
        });

        Table sunBox = new Table();
        sunBox.add(new Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(32f).padRight(4f);
        sunBox.add(sunLabel).padRight(20f);

        Label title = new Label("I, Zombie - Couch Play (Level " + level + ")", skin, "title");

        Table row = new Table();
        row.add(exitButton).size(70f, 44f).padRight(16f);
        row.add(sunBox);
        row.add(title).padLeft(20f);

        top.add(row).padTop(8f).row();
        top.add(new Label("Plants: mouse click   |   Zombies: arrows to move, 1-4 to pick, Space to place", skin))
                .padTop(2f).row();
        top.add(statusLabel).padTop(4f).row();
        stage.addActor(top);
    }

    private void buildSidebars() {
        Table plantSidebar = new Table();
        plantSidebar.setFillParent(true);
        plantSidebar.top().left();
        plantSidebar.padTop(110f).padLeft(8f);
        plantSidebar.add(new Label("Plants:", skin)).left().row();
        for (String p : PLANT_TYPES) {
            plantSidebar.add(buildPlantCard(p)).size(64f, 64f).padBottom(4f).row();
        }
        stage.addActor(plantSidebar);

        Table zombieSidebar = new Table();
        zombieSidebar.setFillParent(true);
        zombieSidebar.top().right();
        zombieSidebar.padTop(110f).padRight(8f);
        zombieSidebar.add(new Label("Zombies (1-4):", skin)).right().row();
        for (int i = 0; i < ZOMBIE_TYPES.length; i++) {
            Label l = new Label((i + 1) + ". " + ZOMBIE_TYPES[i], skin);
            zombieSidebar.add(l).right().padBottom(4f).row();
        }
        stage.addActor(zombieSidebar);
    }

    private Stack buildPlantCard(String plantName) {
        Stack stack = new Stack();
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
        stack.add(new Image(ImageUtils.loadRegion(AssetPaths.plantSeedPacket(plantName))));
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedPlantType = plantName;
                SoundManager.playSound(AssetPaths.SFX_CLICK);
            }
        });
        return stack;
    }

    // ==================== حلقه‌ی اصلی ====================

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!session.isGameOver()) {
            tickAccumulator += delta;
            while (tickAccumulator >= SECONDS_PER_TICK) {
                tickAccumulator -= SECONDS_PER_TICK;
                session.advanceOneTick();
            }
            handleZombieKeyboardInput();
        } else if (!announcedGameOver) {
            announcedGameOver = true;
            game.setScreen(new IZombieMultiplayerResultScreen(game, level, !session.isWon()));
            // نکته: در Couch Play کاربر واحدی که وارد سیستم است هر دو نقش را بازی می‌کند،
            // پس این صفحه‌ی نتیجه صرفاً برد/باخت کلی مینی‌گیم را نشان می‌دهد.
            return;
        }

        for (String event : session.pollRecentEvents()) {
            statusLabel.setText(event);
        }

        stage.getBatch().begin();
        drawBoard(stage.getBatch());
        drawPlants(stage.getBatch());
        drawZombies(stage.getBatch());
        stage.getBatch().end();

        sunLabel.setText(String.valueOf(session.getSunManager().getCurrentSun()));
        cursorImage.setPosition(tileX(cursorCol) + 3f, tileY(cursorRow) + 3f);

        stage.act(delta);
        stage.draw();

        handlePlantMouseInput();
    }

    private void handleZombieKeyboardInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && cursorCol > 0) cursorCol--;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && cursorCol < Board.COLS - 1) cursorCol++;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && cursorRow > 0) cursorRow--;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && cursorRow < Board.ROWS - 1) cursorRow++;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedZombieType = ZOMBIE_TYPES[0];
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedZombieType = ZOMBIE_TYPES[1];
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectedZombieType = ZOMBIE_TYPES[2];
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) selectedZombieType = ZOMBIE_TYPES[3];

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            IZombieSession.PlaceZombieResult result = session.placeZombie(selectedZombieType, cursorRow, cursorCol);
            switch (result) {
                case SUCCESS:
                    statusLabel.setText(selectedZombieType + " placed!");
                    break;
                case BEYOND_RED_LINE:
                    statusLabel.setText("Zombies can only go right of the red line.");
                    break;
                case NOT_ENOUGH_SUN:
                    statusLabel.setText("Not enough sun for " + selectedZombieType + ".");
                    break;
                default:
                    statusLabel.setText("Invalid zombie placement.");
            }
            SoundManager.playSound(AssetPaths.SFX_CLICK);
        }
    }

    private void handlePlantMouseInput() {
        if (!Gdx.input.justTouched() || selectedPlantType == null) {
            return;
        }
        Vector2 touch = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H) - 1;
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return;
        }

        Tile tile = session.getBoard().getTile(row, col);
        if (session.isPlantOnCooldown(selectedPlantType)) {
            statusLabel.setText(selectedPlantType + " is on cooldown.");
            return;
        }
        Plant plant = PlantFactory.create(selectedPlantType);
        if (tile == null || !tile.canPlant(plant)) {
            statusLabel.setText("Can't plant there.");
            return;
        }
        if (!session.getSunManager().spendSun(plant.getSunCost())) {
            statusLabel.setText("Not enough sun for " + selectedPlantType + ".");
            return;
        }
        plant.place(row, col);
        tile.setPlant(plant);
        session.startPlantCooldown(selectedPlantType, plant.getCooldownTicks());
        statusLabel.setText(selectedPlantType + " planted!");
        SoundManager.playSound(AssetPaths.SFX_PLANT);
    }

    // ==================== رسم ====================

    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        TextureRegion bg = ImageUtils.loadRegion(AssetPaths.BG_MINIGAME_IZOMBIE);
        batch.draw(bg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (plant == null) continue;
                TextureRegion tex = ImageUtils.loadRegion(AssetPaths.plantIcon(plant.getName()));
                batch.draw(tex, tileX(c) + 8f, tileY(r) + 8f, TILE_W - 16f, TILE_H - 16f);
            }
        }
    }

    private void drawZombies(com.badlogic.gdx.graphics.g2d.Batch batch) {
        for (Zombie z : session.getAliveZombies()) {
            TextureRegion tex = ImageUtils.loadRegion(AssetPaths.zombieIcon(z.getTypeName()));
            float x = tileX(0) + (float) z.getXPosition() * TILE_W;
            float y = tileY(z.getRow());
            batch.draw(tex, x, y, TILE_W - 10f, TILE_H - 10f);
        }
    }

    // ==================== متدهای استاندارد Screen ====================

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
