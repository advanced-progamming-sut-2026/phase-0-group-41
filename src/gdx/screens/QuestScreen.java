package gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import controller.QuestController;
import gdx.PvZGame;
import gdx.assets.AssetPaths;
import model.quest.Quest;
import model.user.User;

import java.util.List;

public class QuestScreen extends BaseMenuScreen {

    private final Table listTable = new Table();

    public QuestScreen(PvZGame game) {
        super(game);

        rootTable.add(title("Quests (Travel Log)")).padBottom(16f).row();

        listTable.top();
        refreshList();

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        rootTable.add(scrollPane).width(800f).height(420f).padBottom(16f).row();

        rootTable.add(errorLabel).width(600f).padBottom(10f).row();
        addButton(rootTable, "Back to Main Menu", game::goToMainMenu);
    }

    private void refreshList() {
        listTable.clear();
        User user = game.getLoggedInUser();
        if (user == null) {
            return;
        }
        QuestController controller = game.getQuestController();
        List<Quest> quests = controller.getAllQuests(user);

        for (Quest quest : quests) {
            Label nameLabel = new Label(quest.getTitle(), skin);
            nameLabel.setWrap(true);
            Label descLabel = new Label(quest.getDescription(), skin);
            descLabel.setWrap(true);
            descLabel.setFontScale(0.85f);

            String status = quest.isClaimed() ? "Claimed"
                    : quest.isCompleted() ? "Ready to claim"
                    : "In progress";
            Label statusLabel = new Label(status, skin);

            Table row = new Table();
            row.add(nameLabel).width(280f).left().row();
            row.add(descLabel).width(280f).left().row();

            listTable.add(row).width(300f).padRight(10f).padBottom(10f);
            listTable.add(statusLabel).width(140f).padBottom(10f);

            TextButton claimButton = new TextButton("Claim Reward", skin);
            claimButton.setDisabled(!quest.isCompleted() || quest.isClaimed());
            claimButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    doClaim(quest.getId());
                }
            });
            listTable.add(claimButton).width(160f).height(44f).padBottom(10f).row();
        }

        if (quests.isEmpty()) {
            listTable.add(new Label("No quests available.", skin)).row();
        }
    }

    private void doClaim(String questId) {
        clearError();
        User user = game.getLoggedInUser();
        QuestController.ClaimResult result = game.getQuestController().claimQuestReward(user, questId);
        switch (result) {
            case SUCCESS:
                refreshList();
                break;
            case NOT_COMPLETED:
                showError("This quest is not completed yet.");
                break;
            case ALREADY_CLAIMED:
                showError("Reward for this quest was already claimed.");
                break;
            default:
                showError("Error claiming reward.");
        }
    }

    @Override
    protected String backgroundPath() {
        return AssetPaths.BG_QUEST;
    }
}
