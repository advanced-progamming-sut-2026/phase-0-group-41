package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.NewsMessage;
import model.user.User;

public class NewsScreen extends BaseMenuScreen {

    public NewsScreen(PvZGame game) {
        super(game);

        rootTable.add(title("News")).padBottom(16f).row();

        User user = game.getLoggedInUser();
        Table list = new Table();
        list.top();

        if (user != null) {
            // Per spec: entering this menu marks unread news as read.
            game.getMainController(); // (no effect, kept for readability of the flow)
            for (NewsMessage msg : user.getNewsList()) {
                String prefix = msg.isRead() ? "" : "\u2757 ";
                Label item = new Label(prefix + msg.getContent(), skin);
                item.setWrap(true);
                list.add(item).width(700f).padBottom(10f).left().row();
                msg.markAsRead();
            }
            if (user.getNewsList().isEmpty()) {
                list.add(new Label("No news available.", skin)).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(760f).height(420f).padBottom(20f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_NEWS;
    }
}
