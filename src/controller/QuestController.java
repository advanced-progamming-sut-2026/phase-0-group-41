package controller;

import model.quest.Quest;
import model.quest.QuestManager;
import model.quest.RewardType;
import model.user.User;
import model.user.UserManager;
import java.util.List;

public class QuestController {

    private final UserManager userManager;

    public QuestController(UserManager userManager) {
        this.userManager = userManager;
    }

    public List<Quest> getAllQuests(User user) {
        return user.getQuestManager().getAllQuests();
    }

    public ClaimResult claimQuestReward(User user, String questId) {
        QuestManager qm = user.getQuestManager();
        Quest quest = qm.getQuest(questId);

        if (quest == null) return ClaimResult.INVALID_ID;
        if (!quest.isCompleted()) return ClaimResult.NOT_COMPLETED;
        if (quest.isClaimed()) return ClaimResult.ALREADY_CLAIMED;

        RewardType type = quest.getRewardType();
        if (type == RewardType.CURRENCY) {
            user.addCoins(quest.getRewardAmount());
        } else if (type == RewardType.UNLOCKABLE) {
            user.unlock(quest.getRewardItemId());
        } else if (type == RewardType.INVENTORY) {
            user.addItemToInventory(quest.getRewardItemId(), quest.getRewardAmount());
        }

        quest.setClaimed(true);
        userManager.save();
        return ClaimResult.SUCCESS;
    }

    public enum ClaimResult {
        SUCCESS, INVALID_ID, NOT_COMPLETED, ALREADY_CLAIMED
    }
}