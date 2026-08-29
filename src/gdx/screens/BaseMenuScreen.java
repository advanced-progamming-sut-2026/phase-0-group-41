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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gdx.PvZGame;
import gdx.util.ImageUtils;

/**
 * کلاس پایه‌ی مشترک برای همه‌ی صفحات منو.
 * هر Screen گرافیکی، به‌جای صدا زدن مستقیم متدهای Controller با رشته‌های دستور،
 * مستقیماً همان متدهای Controller فاز اول (که خروجی رشته‌ای/enum برمی‌گردانند) را صدا می‌زند.
 */
public abstract class BaseMenuScreen implements Screen {

    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    protected final PvZGame game;
    protected final Stage stage;
    protected final Skin skin;
    protected final Table rootTable;
    protected final Label errorLabel;

    protected BaseMenuScreen(PvZGame game) {
        this.game = game;
        Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = game.getSkin();

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        errorLabel = new Label("", skin, "error");
        errorLabel.setWrap(true);
    }

    /** مسیر تصویر پس‌زمینه‌ی این صفحه؛ اگر خالی باشد رنگ ساده رسم می‌شود. */
    protected String backgroundPath() {
        return "";
    }

    protected void showError(String message) {
        errorLabel.setText(message);
    }

    protected void clearError() {
        errorLabel.setText("");
    }

    protected TextButton addButton(Table table, String text, Runnable onClick) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
        });
        table.add(button).pad(6f).width(280f).height(56f);
        return button;
    }

    protected Label title(String text) {
        Label label = new Label(text, skin, "title");
        label.setFontScale(1.4f);
        return label;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        String bg = backgroundPath();
        if (!bg.isEmpty()) {
            stage.getBatch().begin();
            Texture texture = ImageUtils.load(bg);
            stage.getBatch().draw(texture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            stage.getBatch().end();
        }

        stage.act(delta);
        stage.draw();
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
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
