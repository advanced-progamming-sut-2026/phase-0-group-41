package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import gdx.util.SoundManager;

import model.game.ChapterPlan;
import model.levelrules.LevelMode;
import model.user.User;

/**
 * صفحه‌ی انتخاب فصل و مرحله برای بخش ماجراجویی؛ از دکمه‌ی «Play» در منوی
 * اصلی قابل‌دسترسی است. لیست فصل‌ها (Beginner + ۴ فصل اصلی) و برای هر فصل،
 * ۴ دکمه‌ی مرحله را نشان می‌دهد. مرحله‌های قفل‌شده (طبق پیشرفت واقعی کاربر
 * در ChapterPlan.isLevelUnlocked) غیرفعال و کم‌رنگ‌اند.
 */
public class ChapterLevelSelectScreen extends BaseMenuScreen {

    public ChapterLevelSelectScreen(PvZGame game) {
        super(game);

        User user = game.getLoggedInUser();

        rootTable.add(title("Select Chapter & Level")).padBottom(16f).row();

        Table listTable = new Table();
        for (int chapter = ChapterPlan.BEGINNER_CHAPTER; chapter <= ChapterPlan.LAST_CHAPTER; chapter++) {
            boolean chapterUnlocked = (user == null) || ChapterPlan.isChapterUnlocked(user, chapter);

            Table row = new Table();
            Label nameLabel = new Label(ChapterPlan.displayName(chapter)
                    + (chapterUnlocked ? "" : "  (Locked)"), skin);
            nameLabel.setFontScale(1.05f);
            row.add(nameLabel).width(260f).left().padRight(16f);

            for (int level = 1; level <= ChapterPlan.LEVELS_PER_CHAPTER; level++) {
                final int ch = chapter;
                final int lvl = level;
                boolean levelUnlocked = chapterUnlocked && (user == null || ChapterPlan.isLevelUnlocked(user, ch, lvl));
                LevelMode mode = ChapterPlan.levelModeFor(chapter, level);
                String label = "L" + level + (mode != LevelMode.NORMAL ? " *" : "");

                TextButton levelButton = new TextButton(levelUnlocked ? label : label + " \uD83D\uDD12", skin);
                levelButton.setDisabled(!levelUnlocked);
                levelButton.setTouchable(levelUnlocked
                        ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled
                        : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                if (levelUnlocked) {
                    levelButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            SoundManager.playSound(AssetPaths.SFX_CLICK);
                            game.goToPlantSelection(ch, lvl);
                        }
                    });
                } else {
                    levelButton.getColor().a = 0.5f;
                }
                row.add(levelButton).size(90f, 56f).padRight(6f);
            }

            listTable.add(row).padBottom(12f).row();
        }

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(950f).height(380f).padBottom(16f).row();

        rootTable.add(new Label("* = special level", skin)).padBottom(16f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
