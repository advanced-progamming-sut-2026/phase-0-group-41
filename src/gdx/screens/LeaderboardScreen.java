package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

import java.util.List;

public class LeaderboardScreen extends BaseMenuScreen {

    private final SelectBox<String> sortBox;
    private final Table listTable = new Table();

    public LeaderboardScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Leaderboard")).padBottom(16f).row();

        sortBox = new SelectBox<>(skin);
        sortBox.setItems("score", "username", "chapter", "minigame", "daily", "nondaily");
        sortBox.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                refreshList();
            }
        });
        rootTable.add(sortBox).width(200f).padBottom(10f).row();

        listTable.top();
        refreshList();

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(700f).height(400f).padBottom(16f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private void refreshList() {
        listTable.clear();
        listTable.add(new Label("Rank", skin)).width(60f);
        listTable.add(new Label("Username", skin)).width(200f);
        listTable.add(new Label("Score", skin)).width(120f);
        listTable.add(new Label("Chapter/Level", skin)).width(140f).row();

        List<User> sorted = game.getLeaderboardController().getSortedLeaderboard(sortBox.getSelected(), false);
        int rank = 1;
        for (User u : sorted) {
            listTable.add(new Label(String.valueOf(rank), skin)).padBottom(6f);
            listTable.add(new Label(u.getUsername(), skin)).padBottom(6f);
            listTable.add(new Label(String.valueOf(u.getHighScore()), skin)).padBottom(6f);
            listTable.add(new Label(u.getLastCompletedChapter() + "-" + u.getLastCompletedLevel(), skin)).padBottom(6f).row();
            rank++;
        }
        if (sorted.isEmpty()) {
            listTable.add(new Label("No users found.", skin)).colspan(4).row();
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEADERBOARD;
    }
}
