package gdx.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.NewsMessage;
import model.user.User;

/**
 * News screen.
 *
 * Behaviour:
 *  - Unread messages are marked with a small red dot to the left of the text.
 *  - By default only messages from the last 24 hours are shown, newest first,
 *    so the list can't grow into an endless wall of old messages.
 *  - A "Show all news" toggle lets the user switch to the full history; the
 *    button turns into "Show recent only" so they can switch back.
 *  - Messages are only marked as read once they've actually been displayed
 *    (i.e. once this screen is shown), same as before.
 */
public class NewsScreen extends BaseMenuScreen {

    private static final long ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L;

    private final Table list;
    private final ScrollPane scrollPane;
    private final User user;
    private boolean showAll = false;
    private com.badlogic.gdx.scenes.scene2d.ui.TextButton toggleButton;
    private Texture unreadDotTexture;

    public NewsScreen(PvZGame game) {
        super(game);

        rootTable.add(title("News")).padBottom(16f).row();

        this.user = game.getLoggedInUser();
        this.list = new Table();
        list.top();

        this.scrollPane = new ScrollPane(list, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(760f).height(420f).padBottom(12f).row();

        toggleButton = addButton(rootTable, "", this::toggleShowAll);
        rootTable.row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);

        rebuildList();
    }

    private void toggleShowAll() {
        showAll = !showAll;
        rebuildList();
    }

    private void rebuildList() {
        list.clearChildren();

        toggleButton.setText(showAll ? "Show recent only" : "Show all news");

        if (user == null) {
            list.add(new Label("No news available.", skin)).row();
            return;
        }

        List<NewsMessage> allNews = user.getNewsList();

        // Sort newest first so the most relevant messages are at the top.
        List<NewsMessage> sorted = new ArrayList<>(allNews);
        sorted.sort(Comparator.comparingLong(NewsMessage::getTimestamp).reversed());

        long cutoff = System.currentTimeMillis() - ONE_DAY_MILLIS;
        List<NewsMessage> visible = new ArrayList<>();
        for (NewsMessage msg : sorted) {
            if (showAll || msg.getTimestamp() >= cutoff) {
                visible.add(msg);
            }
        }

        if (visible.isEmpty()) {
            String message = showAll
                    ? "No news available."
                    : "No news in the last 24 hours.";
            list.add(new Label(message, skin)).row();
        } else {
            for (NewsMessage msg : visible) {
                Table row = new Table();

                if (!msg.isRead()) {
                    Image dot = new Image(getUnreadDotTexture());
                    row.add(dot).size(14f).padRight(8f).top().padTop(4f);
                } else {
                    // Keep the text column aligned whether or not there's a dot.
                    row.add().size(14f).padRight(8f);
                }

                Label item = new Label(msg.getContent(), skin);
                item.setWrap(true);
                row.add(item).width(660f).left();

                list.add(row).width(700f).padBottom(10f).left().row();
            }
        }

        // Entering this screen marks all currently-loaded news as read,
        // regardless of whether it's shown under the recent filter or not.
        for (NewsMessage msg : allNews) {
            msg.markAsRead();
        }

        scrollPane.layout();
    }

    private Texture getUnreadDotTexture() {
        if (unreadDotTexture == null) {
            int size = 32;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0.9f, 0.15f, 0.15f, 1f));
            pixmap.fillCircle(size / 2, size / 2, size / 2);
            unreadDotTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return unreadDotTexture;
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_NEWS;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (unreadDotTexture != null) {
            unreadDotTexture.dispose();
            unreadDotTexture = null;
        }
    }
}
