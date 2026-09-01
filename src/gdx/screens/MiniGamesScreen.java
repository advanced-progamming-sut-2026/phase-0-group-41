package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.ImageUtils;
import gdx.util.SoundManager;
import model.user.User;

/**
 * صفحه‌ی انتخاب مینی‌گیم: هر یک از چهار مینی‌گیم (کوزه‌شکنی، بولینگ گردویی،
 * من زامبی، ترکیب سه‌تایی) با یک آیکون واقعی (از دارایی‌های رسمی) و سه دکمه‌ی
 * سطح ۱، ۲ و ۳ نمایش داده می‌شود. هر مرحله از قبلی سخت‌تر است (طبق سند فاز یک).
 * سطح n>1 فقط پس از بردن سطح n-1 همان مینی‌گیم باز می‌شود.
 */
public class MiniGamesScreen extends BaseMenuScreen {

    private static final String[][] GAMES = {
            {"vasebreaker", "Vasebreaker", AssetPaths.VASE_NORMAL},
            {"wallnutbowling", "Wall-nut Bowling", AssetPaths.NUT_BOWLING_NORMAL},
            {"izombie", "I, Zombie", AssetPaths.zombieIcon("imp")},
            {"beghouled", "Beghouled", AssetPaths.plantSeedPacket("peashooter")}
    };

    public MiniGamesScreen(PvZGame game) {
        super(game);

        User user = game.getLoggedInUser();

        rootTable.add(title("Mini Games")).padBottom(16f).row();

        Table listTable = new Table();
        for (String[] entry : GAMES) {
            String id = entry[0];
            String displayName = entry[1];
            String iconPath = entry[2];

            Table row = new Table();

            Stack icon = new Stack();
            icon.add(new Image(ImageUtils.loadRegion(AssetPaths.CARD_BACKGROUND)));
            icon.add(new Image(ImageUtils.loadRegion(iconPath)));
            row.add(icon).size(80f, 80f).padRight(16f);

            Label nameLabel = new Label(displayName, skin);
            nameLabel.setFontScale(1.05f);
            row.add(nameLabel).width(240f).left().padRight(20f);

            for (int level = 1; level <= 3; level++) {
                final int lvl = level;
                boolean unlocked = (user == null) || user.isMiniGameLevelUnlocked(id, level);

                TextButton levelButton = new TextButton(unlocked ? "Level " + level : "Level " + level + " 🔒", skin);
                levelButton.setDisabled(!unlocked);
                levelButton.setTouchable(unlocked
                        ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled
                        : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                if (unlocked) {
                    levelButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            SoundManager.playSound(AssetPaths.SFX_CLICK);
                            game.startMiniGame(id, lvl);
                        }
                    });
                } else {
                    levelButton.getColor().a = 0.5f;
                }
                row.add(levelButton).size(120f, 56f).padRight(8f);

                if (id.equals("izombie") && unlocked) {
                    TextButton onlineButton = new TextButton("Online", skin);
                    onlineButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            SoundManager.playSound(AssetPaths.SFX_CLICK);
                            game.setScreen(new IZombieOpponentSelectScreen(game, lvl));
                        }
                    });
                    row.add(onlineButton).size(90f, 56f).padRight(8f);

                    // بخش امتیازی: بازی دونفره‌ی Couch Play روی یک دستگاه، بدون شبکه
                    TextButton couchButton = new TextButton("Couch", skin);
                    couchButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            SoundManager.playSound(AssetPaths.SFX_CLICK);
                            game.setScreen(new IZombieCouchScreen(game, lvl));
                        }
                    });
                    row.add(couchButton).size(90f, 56f).padRight(8f);
                }
            }

            listTable.add(row).padBottom(14f).row();
        }

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(950f).height(380f).padBottom(20f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private static final float CHALLENGE_POLL_INTERVAL = 2.0f;
    private float challengePollAccumulator = 0f;
    private boolean navigatedAway = false;

    @Override
    public void render(float delta) {
        super.render(delta);
        if (navigatedAway) {
            return;
        }
        User user = game.getLoggedInUser();
        if (user == null) {
            return;
        }
        challengePollAccumulator += delta;
        if (challengePollAccumulator < CHALLENGE_POLL_INTERVAL) {
            return;
        }
        challengePollAccumulator = 0f;

        network.izombie.IZombieNetworkClient.IncomingChallenge challenge =
                network.izombie.IZombieNetworkClient.pollIncomingChallenge(user.getUsername());
        if (challenge != null) {
            navigatedAway = true;
            game.setScreen(new IZombieIncomingChallengeScreen(game, challenge.fromUsername, challenge.level));
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
