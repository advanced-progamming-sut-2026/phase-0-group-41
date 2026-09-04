package controller;

import model.quest.Quest;
import model.quest.QuestManager;
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
        if (quest.isRewardClaimed() && !quest.isRepeatable()) return ClaimResult.ALREADY_CLAIMED;

        // این متد rewards واقعی کوئست (QuestReward) را روی پروفایل بازیکن اعمال می‌کند
        // و در صورت تکرارپذیر بودن، کوئست را برای دور بعد ریست می‌کند.
        qm.claimReward(questId, user);

        userManager.save();
        return ClaimResult.SUCCESS;
    }

    public enum ClaimResult {
        SUCCESS, INVALID_ID, NOT_COMPLETED, ALREADY_CLAIMED
    }
}
