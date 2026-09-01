package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;

/**
 * دیالوگ ساده‌ی ان‌پی‌سی قبل از شروع برخی مراحل (طبق سند: «در ابتدای برخی
 * مراحل، شخصیت‌های اصلی بازی چند دیالوگ رد و بدل می‌کنند»). طبق توضیح سند،
 * نیازی نیست حتماً شخصیت‌های خود بازی باشند؛ اینجا از دو شخصیت نمونه استفاده
 * شده و صرفاً چند خط متن پشت سر هم با دکمه‌ی Continue نمایش داده می‌شود.
 */
public class DialogueScreen extends BaseMenuScreen {

    public static final class Line {
        public final String speaker;
        public final String text;
        public Line(String speaker, String text) {
            this.speaker = speaker;
            this.text = text;
        }
    }

    private final Line[] lines;
    private int currentIndex = 0;
    private final Label speakerLabel;
    private final Label textLabel;
    private final Runnable onFinished;

    public DialogueScreen(PvZGame game, Line[] lines, Runnable onFinished) {
        super(game);
        this.lines = lines;
        this.onFinished = onFinished;

        rootTable.add(title("Story")).padBottom(20f).row();

        speakerLabel = new Label("", skin);
        speakerLabel.setFontScale(1.2f);
        textLabel = new Label("", skin);
        textLabel.setWrap(true);

        Table box = new Table();
        box.add(speakerLabel).padBottom(10f).row();
        box.add(textLabel).width(700f).row();
        rootTable.add(box).padBottom(30f).row();

        addButton(rootTable, "Continue", this::advance);

        showCurrentLine();
    }

    private void showCurrentLine() {
        if (currentIndex >= lines.length) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        Line line = lines[currentIndex];
        speakerLabel.setText(line.speaker + ":");
        textLabel.setText(line.text);
    }

    private void advance() {
        currentIndex++;
        showCurrentLine();
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEVEL_SELECT;
    }
}
