package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.user.User;

import java.util.List;

/**
 * صفحه‌ی لیدربورد طبق سند فاز ۱ (تصویر مرجع):
 *  ۱) آخرین مرحله و فصلی که بازیکن موفق به انجام آن شده (مثال: مرحله ۱ فصل ۳)
 *  ۲) تعداد مینی‌گیم‌هایی که بازیکن با موفقیت انجام داده است
 *  ۳) تعداد کوئست‌های روزانه و غیر روزانه (به‌طور مجزا) که بازیکن انجام داده
 *  ۴) بالاترین امتیازی که بازیکن در بازی امتیازی کسب کرده (ستون «My Point»)
 *
 * کاربر باید بتواند با کلیک بر روی هر یک از این ستون‌ها، بازیکن‌ها را به‌صورت
 * افزایشی و کاهشی مرتب کند؛ به همین دلیل هر هدر ستون خودش یک دکمه است که با
 * هر بار کلیک، جهت مرتب‌سازی (صعودی/نزولی) را برای همان ستون toggle می‌کند.
 */
public class LeaderboardScreen extends BaseMenuScreen {

    private enum SortColumn { STAGE, MINIGAMES, QUESTS, MY_POINT }

    private final Table listTable = new Table();

    private SortColumn currentSort = SortColumn.STAGE;
    private boolean ascending = false;

    public LeaderboardScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Leaderboard")).padBottom(16f).row();

        listTable.top();
        refreshList();

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(820f).height(420f).padBottom(16f).row();

        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private void sortBy(SortColumn column) {
        if (currentSort == column) {
            ascending = !ascending; // کلیک دوباره روی همان ستون: جهت را برعکس کن
        } else {
            currentSort = column;
            ascending = false; // با تعویض ستون، پیش‌فرض نزولی (بهترین‌ها بالا)
        }
        refreshList();
    }

    private TextButton headerButton(String text, SortColumn column) {
        String arrow = (currentSort == column) ? (ascending ? " ^" : " v") : "";
        TextButton button = new TextButton(text + arrow, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sortBy(column);
            }
        });
        return button;
    }

    private void refreshList() {
        listTable.clear();

        listTable.add(new Label("Rank", skin)).width(60f);
        listTable.add(new Label("Username", skin)).width(180f);
        listTable.add(headerButton("Last Stage", SortColumn.STAGE)).width(150f);
        listTable.add(headerButton("Mini-Games Won", SortColumn.MINIGAMES)).width(150f);
        listTable.add(headerButton("Quests (Daily/Other)", SortColumn.QUESTS)).width(180f);
        listTable.add(headerButton("My Point", SortColumn.MY_POINT)).width(120f).row();

        List<User> sorted = game.getLeaderboardController()
                .getSortedLeaderboard(sortKey(currentSort), ascending);

        int rank = 1;
        for (User u : sorted) {
            listTable.add(new Label(String.valueOf(rank), skin)).padBottom(6f);
            listTable.add(new Label(u.getUsername(), skin)).padBottom(6f);
            listTable.add(new Label(u.getLastCompletedChapter() + "-" + u.getLastCompletedLevel(), skin)).padBottom(6f);
            listTable.add(new Label(String.valueOf(u.getMiniGamesCompleted()), skin)).padBottom(6f);
            listTable.add(new Label(u.getDailyQuestsCompleted() + " / " + u.getNonDailyQuestsCompleted(), skin)).padBottom(6f);

            // طبق سند فاز ۳ (بخش امتیازی): کاربری که هنوز بازی امتیازی تحت شبکه را
            // انجام نداده نباید امتیاز قبلی یا ساختگی در ستون «My Point» داشته باشد
            String myPointText = u.hasPlayedScoreGame() ? String.valueOf(u.getMaxMowPoints()) : "-";
            listTable.add(new Label(myPointText, skin)).padBottom(6f).row();
            rank++;
        }
        if (sorted.isEmpty()) {
            listTable.add(new Label("No users found.", skin)).colspan(6).row();
        }
    }

    private String sortKey(SortColumn column) {
        switch (column) {
            case MINIGAMES: return "minigame";
            case QUESTS: return "quest";
            case MY_POINT: return "mypoint";
            case STAGE: default: return "chapter";
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_LEADERBOARD;
    }
}
