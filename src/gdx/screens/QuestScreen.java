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
        rootTable.add(scrollPane).width(900f).height(440f).padBottom(16f).row();

        rootTable.add(errorLabel).width(600f).padBottom(10f).row();

        Table buttons = new Table();
        // طبق سند: «همچنین از طریق این منو می‌توانیم به مینی‌گیم‌ها دسترسی داشته باشیم»
        addButton(buttons, "Mini Games", game::goToMiniGames);
        addButton(buttons, "Back to Main Menu", game::goToMainMenu);
        rootTable.add(buttons).row();
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
            nameLabel.setFontScale(1.05f);

            Label descLabel = new Label(quest.getDescription(), skin);
            descLabel.setWrap(true);
            descLabel.setFontScale(0.85f);

            Label rewardLabel = new Label("Reward: " + quest.getRewardsSummary(), skin);
            rewardLabel.setWrap(true);
            rewardLabel.setFontScale(0.8f);
            rewardLabel.setColor(0.85f, 0.75f, 0.2f, 1f);

            String status = quest.isRewardClaimed() ? "Claimed"
                    : quest.isCompleted() ? "Ready to claim!"
                    : "In progress";
            Label statusLabel = new Label(status, skin);
            if (quest.isRewardClaimed()) {
                statusLabel.setColor(0.6f, 0.6f, 0.6f, 1f);
            } else if (quest.isCompleted()) {
                statusLabel.setColor(0.3f, 0.9f, 0.3f, 1f);
            } else {
                statusLabel.setColor(1f, 1f, 1f, 1f);
            }

            Table row = new Table();
            row.add(nameLabel).width(340f).left().row();
            row.add(descLabel).width(340f).left().padTop(2f).row();
            row.add(rewardLabel).width(340f).left().padTop(2f).row();

            listTable.add(row).width(360f).padRight(12f).padBottom(14f).padTop(10f);
            listTable.add(statusLabel).width(140f).padBottom(14f).padTop(10f);

            TextButton claimButton = new TextButton("Claim Reward", skin);
            boolean canClaim = quest.isCompleted() && !quest.isRewardClaimed();
            claimButton.setDisabled(!canClaim);
            claimButton.setTouchable(canClaim
                    ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled
                    : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            claimButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                    doClaim(quest.getId());
                }
            });
            listTable.add(claimButton).width(160f).height(44f).padBottom(14f).padTop(10f).row();
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
