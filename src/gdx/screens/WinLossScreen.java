package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;
import model.user.User;

/**
 * منوی برد/باخت. بعد از برد یا باخت کاربر، وضعیت به او اعلام می‌شود.
 * گزینه‌ی خروج همیشه هست؛ در صورت باخت، گزینه‌ی "تلاش مجدد" هم نمایش
 * داده می‌شود (طبق سند).
 */
public class WinLossScreen extends BaseMenuScreen {

    public WinLossScreen(PvZGame game, boolean won, int chapter, int level, int scoreEarned, Runnable onRetry) {
        super(game);

        User user = game.getLoggedInUser();

        SoundManager.playMusic(won ? AssetPaths.MUSIC_WIN : AssetPaths.MUSIC_LOSE);

        // برد: تیتر معمولی (fbUsv8C5eI). باخت: فونت وحشت "House of Terror"
        // برای تأکید بصری زامبی‌محور روی بنر "You Lose!".
        String headline = won ? "You Win!" : "You Lose!";
        rootTable.add(won ? title(headline) : horrorTitle(headline)).padBottom(16f).row();
        rootTable.add(new Label("Chapter " + chapter + " - Level " + level, skin)).padBottom(10f).row();
        rootTable.add(new Label("Score earned: " + scoreEarned, skin)).padBottom(30f).row();

        if (won && user != null) {
            user.incrementLevelsCompleted();
            // طبق سند: وضعیت کوئست‌ها باید بعد از هر برد بررسی مجدد شود؛ در
            // نسخه‌ی کنسولی این کار در AppController.finishGame انجام می‌شد ولی
            // اینجا (WinLossScreen گرافیکی) فراموش شده بود.
            user.getQuestContext().setStagesCompleted(user.getLevelsCompleted());
            if (user.getQuestManager() != null) {
                user.getQuestManager().refreshCompletionStatus(user.getQuestContext());
            }
            // منطق پیشرفت باید دقیقاً مطابق AppController کنسولی باشد.
            if (chapter == model.game.ChapterPlan.BEGINNER_CHAPTER) {
                if (level > user.getBeginnerLastCompletedLevel()) {
                    user.setBeginnerLastCompletedLevel(level);
                    // اولین باری که این مرحله برده می‌شود یک گیاه جدید آنلاک می‌شود
                    // و به لیست گیاهان در دسترس (منوی انتخاب گیاه) اضافه می‌گردد.
                    // این بخش دقیقاً معادل نسخه‌ی کنسولی (AppController) است که اینجا
                    // فراموش شده بود.
                    String newlyUnlocked = user.unlockNextPlant();
                    if (newlyUnlocked != null) {
                        user.addNews("New plant unlocked: " + newlyUnlocked);
                    }
                }
            } else if (chapter > user.getLastCompletedChapter()
                    || (chapter == user.getLastCompletedChapter() && level > user.getLastCompletedLevel())) {
                user.setLastCompletedLevel(level);
                // اولین باری که این مرحله برده می‌شود یک گیاه جدید آنلاک می‌شود
                // و به لیست گیاهان در دسترس (منوی انتخاب گیاه) اضافه می‌گردد.
                String newlyUnlocked = user.unlockNextPlant();
                if (newlyUnlocked != null) {
                    user.addNews("New plant unlocked: " + newlyUnlocked);
                }
                if (level >= model.game.ChapterPlan.LEVELS_PER_CHAPTER) {
                    user.setLastCompletedChapter(chapter);
                    user.setLastCompletedLevel(0); // ریست برای شروع فصل جدید
                    if (chapter < model.game.ChapterPlan.LAST_CHAPTER) {
                        user.addNews("New chapter unlocked: " + model.game.ChapterPlan.displayName(chapter + 1));
                    }
                } else {
                    user.addNews("New level unlocked: " + model.game.ChapterPlan.displayName(chapter)
                            + " - Level " + (level + 1));
                }
            }
            if (scoreEarned > user.getHighScore()) {
                user.setHighScore(scoreEarned);
            }
            game.getUserManager().save();
        }

        Table buttons = new Table();
        if (!won) {
            addButton(buttons, "Retry", () -> {
                if (onRetry != null) {
                    onRetry.run();
                } else {
                    game.goToPlantSelection(chapter, level);
                }
            });
        } else {
            addButton(buttons, "Next Level", () -> game.goToChapterLevelSelect());
        }
        addButton(buttons, "Exit to Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}
