package gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
import model.minigame.BeghouledSession;
import model.minigame.IZombieSession;
import model.minigame.MiniGameSession;
import model.minigame.VasebreakerSession;
import model.minigame.WallnutBowlingSession;
import model.plant.Plant;
import model.zombie.Zombie;

/**
 * صفحه‌ی گرافیکی مشترک هر چهار مینی‌گیم. چون هر مینی‌گیم مدل نمونه‌سازی و روش
 * تعامل متفاوتی دارد (شکستن کوزه / کاشتن گردو / گذاشتن زامبی / جابجایی گیاه)،
 * این کلاس صرفاً روی همان تخته‌ی ۵×۹ استاندارد (که MiniGameSession از
 * GameSession به ارث می‌برد) کلیک کاربر را به متد مناسب مدل مربوطه وصل می‌کند.
 */
public class MiniGameScreen implements Screen {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float BOARD_LEFT = 260f;
    private static final float BOARD_TOP = 640f;
    private static final float TILE_W = 100f;
    private static final float TILE_H = 96f;
    private static final float SECONDS_PER_TICK = 0.1f;

    private final PvZGame game;
    private final Stage stage;
    private final Skin skin;
    private final MiniGameSession session;
    private final String gameName;
    private final int level;

    private final Label sunLabel;
    private final Label statusLabel;
    private final Table sidebarTable = new Table();

    private float tickAccumulator = 0f;
    private boolean announcedGameOver = false;

    // برای بولینگ گردویی: نوع گردوی انتخاب‌شده از نوار نقاله برای کاشت بعدی
    private WallnutBowlingSession.NutType selectedNutType = null;
    // برای من زامبی: نوع زامبی انتخاب‌شده برای قرار دادن
    private String selectedZombieType = null;
    // برای Beghouled: خانه‌ی اول انتخاب‌شده برای جابجایی
    private int[] firstSwapTile = null;

    public MiniGameScreen(PvZGame game, MiniGameSession session, String gameName, int level) {
        this.game = game;
        this.session = session;
        this.gameName = gameName;
        this.level = level;
        this.skin = game.getSkin();

        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);

        sunLabel = new Label("0", skin);
        statusLabel = new Label("", skin);

        buildHud();
        buildSidebar();

        SoundManager.playMusic(AssetPaths.MUSIC_MENU);
        Gdx.input.setInputProcessor(stage);
    }

    private void buildHud() {
        Table top = new Table();
        top.setFillParent(true);
        top.top();

        TextButton pauseButton = new TextButton("Exit", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToMiniGames();
            }
        });

        Table sunBox = new Table();
        sunBox.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(ImageUtils.loadRegion(AssetPaths.ICON_SUN))).size(32f).padRight(4f);
        sunBox.add(sunLabel).padRight(20f);

        Label title = new Label(gameName + " - Level " + level, skin, "title");

        Table row = new Table();
        row.add(pauseButton).size(70f, 44f).padRight(16f);
        row.add(sunBox);
        row.add(title).padLeft(20f);

        top.add(row).padTop(8f).row();
        top.add(statusLabel).padTop(4f).row();
        stage.addActor(top);
    }

    private void buildSidebar() {
        sidebarTable.setFillParent(true);
        sidebarTable.top().left();
        sidebarTable.padTop(80f).padLeft(8f);
        stage.addActor(sidebarTable);
        refreshSidebar();
    }

    private void refreshSidebar() {
        sidebarTable.clear();

        if (session instanceof WallnutBowlingSession) {
            WallnutBowlingSession bowling = (WallnutBowlingSession) session;
            sidebarTable.add(new Label("Conveyor:", skin)).left().row();
            for (WallnutBowlingSession.NutType type : bowling.getConveyorBelt()) {
                TextButton b = new TextButton(type.name(), skin);
                final WallnutBowlingSession.NutType chosen = type;
                b.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectedNutType = chosen;
                        selectedZombieType = null;
                    }
                });
                sidebarTable.add(b).width(140f).padBottom(4f).row();
            }
        } else if (session instanceof IZombieSession) {
            sidebarTable.add(new Label("Choose zombie:", skin)).left().row();
            String[] types = {"normal", "conehead", "buckethead", "imp"};
            for (String t : types) {
                TextButton b = new TextButton(t, skin);
                b.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectedZombieType = t;
                        selectedNutType = null;
                    }
                });
                sidebarTable.add(b).width(140f).padBottom(4f).row();
            }
        } else if (session instanceof BeghouledSession) {
            sidebarTable.add(new Label("Tap two adjacent", skin)).left().row();
            sidebarTable.add(new Label("plants to swap.", skin)).left().padBottom(10f).row();
            addUpgradeButton("peashooter", 500);
            addUpgradeButton("wallnut", 500);
            addUpgradeButton("puffshroom", 250);
            addUpgradeButton("cabbagepult", 1000);
        } else if (session instanceof VasebreakerSession) {
            sidebarTable.add(new Label("Tap a vase to", skin)).left().row();
            sidebarTable.add(new Label("break it.", skin)).left().row();
        }
    }

    private void addUpgradeButton(String plantName, int cost) {
        TextButton b = new TextButton("Upgrade " + plantName + " (" + cost + ")", skin);
        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BeghouledSession.UpgradeResult result = ((BeghouledSession) session).upgradePlant(plantName);
                if (result == BeghouledSession.UpgradeResult.NOT_ENOUGH_SUN) {
                    statusLabel.setText("Not enough sun to upgrade " + plantName + ".");
                } else if (result == BeghouledSession.UpgradeResult.INVALID_UPGRADE) {
                    statusLabel.setText(plantName + " is not on the field.");
                } else {
                    statusLabel.setText(plantName + " upgraded!");
                }
            }
        });
        sidebarTable.add(b).width(220f).padBottom(4f).row();
    }

    // ==================== حلقه‌ی اصلی رندر ====================

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
        } else if (!announcedGameOver) {
            announcedGameOver = true;
            game.setScreen(new MiniGameResultScreen(game, gameName, level, session.isWon()));
            return;
        }

        stage.getBatch().begin();
        drawBoard(stage.getBatch());
        drawPlants(stage.getBatch());
        drawZombies(stage.getBatch());
        stage.getBatch().end();

        sunLabel.setText(String.valueOf(session.getSunManager().getCurrentSun()));
        refreshSidebar();

        stage.act(delta);
        stage.draw();

        handleBoardClick();
    }

    private float tileX(int col) {
        return BOARD_LEFT + col * TILE_W;
    }

    private float tileY(int row) {
        return BOARD_TOP - (row + 1) * TILE_H;
    }

    private void drawBoard(com.badlogic.gdx.graphics.g2d.Batch batch) {
        // زمینه‌ی ساده‌ی چمن سبز برای تخته‌ی مینی‌گیم (مینی‌گیم‌ها پس‌زمینه‌ی فصلی ندارند)
        batch.setColor(0.2f, 0.5f, 0.2f, 1f);
        batch.draw(skin.getRegion("white"), BOARD_LEFT, tileY(Board.ROWS - 1), TILE_W * Board.COLS, TILE_H * Board.ROWS);
        batch.setColor(Color.WHITE);
    }

    private void drawPlants(com.badlogic.gdx.graphics.g2d.Batch batch) {
        Board board = session.getBoard();
        for (int r = Board.ROWS - 1; r >= 0; r--) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant plant = board.getTile(r, c).getPlant();
                if (plant == null) {
                    continue;
                }
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

    // ==================== ورودی کاربر ====================

    private void handleBoardClick() {
        if (!Gdx.input.justTouched()) {
            return;
        }
        Vector2 touch = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        int col = (int) ((touch.x - BOARD_LEFT) / TILE_W);
        int row = (int) ((BOARD_TOP - touch.y) / TILE_H) - 1;
        if (col < 0 || col >= Board.COLS || row < 0 || row >= Board.ROWS) {
            return;
        }

        if (session instanceof VasebreakerSession) {
            handleVasebreakerClick(row, col);
        } else if (session instanceof WallnutBowlingSession) {
            handleBowlingClick(row, col);
        } else if (session instanceof IZombieSession) {
            handleIZombieClick(row, col);
        } else if (session instanceof BeghouledSession) {
            handleBeghouledClick(row, col);
        }
    }

    private void handleVasebreakerClick(int row, int col) {
        VasebreakerSession vb = (VasebreakerSession) session;
        VasebreakerSession.VaseBreakResult result = vb.breakVase(row, col);
        switch (result.status) {
            case NO_VASE:
                // اگر کوزه نبود، شاید بذر افتاده‌ای اینجا باشد که کاربر می‌خواهد بکارد؛
                // برای سادگی در این پیاده‌سازی گرافیکی، تلاش برای کاشت بذر افتاده در همین خانه انجام می‌شود.
                vb.plantDroppedSeed(row, col, row, col);
                break;
            case GREEN_SEED:
                statusLabel.setText("Green vase broke! Seed packet: " + result.contentName);
                break;
            case PURPLE_GARGANTUAR:
                statusLabel.setText("Purple vase broke! A Gargantuar appeared!");
                break;
            case NORMAL_ZOMBIE:
                statusLabel.setText("Vase broke! Zombie: " + result.contentName);
                break;
            case NORMAL_SEED:
                statusLabel.setText("Vase broke! Seed: " + result.contentName);
                break;
            case NORMAL_EMPTY:
                statusLabel.setText("Vase was empty.");
                break;
            default:
                break;
        }
        SoundManager.playSound(AssetPaths.SFX_CLICK);
    }

    private void handleBowlingClick(int row, int col) {
        if (selectedNutType == null) {
            return;
        }
        WallnutBowlingSession bowling = (WallnutBowlingSession) session;
        String typeStr;
        switch (selectedNutType) {
            case EXPLOSIVE: typeStr = "explodeonut"; break;
            case GIANT: typeStr = "giantwallnut"; break;
            default: typeStr = "bowlingwallnut"; break;
        }
        WallnutBowlingSession.PlantNutResult result = bowling.plantNut(typeStr, row, col);
        if (result == WallnutBowlingSession.PlantNutResult.SUCCESS) {
            statusLabel.setText("Nut planted and rolling!");
            selectedNutType = null;
        } else if (result == WallnutBowlingSession.PlantNutResult.BEYOND_RED_LINE) {
            statusLabel.setText("You can only plant behind the red line.");
        } else {
            statusLabel.setText("That nut is not on the conveyor belt.");
        }
    }

    private void handleIZombieClick(int row, int col) {
        if (selectedZombieType == null) {
            return;
        }
        IZombieSession iz = (IZombieSession) session;
        IZombieSession.PlaceZombieResult result = iz.placeZombie(selectedZombieType, row, col);
        switch (result) {
            case SUCCESS:
                statusLabel.setText(selectedZombieType + " placed!");
                break;
            case BEYOND_RED_LINE:
                statusLabel.setText("You can only place zombies right of the red line.");
                break;
            case NOT_ENOUGH_SUN:
                statusLabel.setText("Not enough sun for " + selectedZombieType + ".");
                break;
            default:
                statusLabel.setText("Invalid zombie type.");
        }
    }

    private void handleBeghouledClick(int row, int col) {
        BeghouledSession beghouled = (BeghouledSession) session;
        if (firstSwapTile == null) {
            firstSwapTile = new int[]{row, col};
            statusLabel.setText("Tile selected. Tap an adjacent tile to swap.");
            return;
        }
        BeghouledSession.SwapResult result = beghouled.swapPlants(firstSwapTile[0], firstSwapTile[1], row, col);
        firstSwapTile = null;
        switch (result) {
            case SUCCESS:
                statusLabel.setText("Match made!");
                break;
            case NOT_ADJACENT:
                statusLabel.setText("Tiles must be adjacent.");
                break;
            case NO_MATCH:
                statusLabel.setText("No match — swap undone.");
                break;
            case EMPTY_TILE:
                statusLabel.setText("One of the tiles is a crater.");
                break;
            default:
                statusLabel.setText("Invalid tiles.");
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
