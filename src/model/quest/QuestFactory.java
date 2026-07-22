package model.quest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class QuestFactory {

    public static Quest createUnlockPlantQuest() {
        return new Quest(
                "critical_unlock_peashooter",
                "Meet the Peashooter",
                "اولین گیاه خود، نخودانداز را باز کنید",
                QuestPriority.CRITICAL,
                QuestPage.ADVENTURE,
                false,
                context -> context.getStagesCompleted() >= 1,
                Collections.singletonList(QuestReward.unlockable("peashooter"))
        );
    }
    public static List<Quest> createDefaultQuests() {
        List<Quest> quests = new ArrayList<>();
        quests.add(createUnlockPlantQuest());
        quests.add(createEpicChallengeQuest());
        quests.add(createDailyLoginQuest());
        return quests;
    }
    public static Quest createEpicChallengeQuest() {
        return new Quest(
                "high_kill_500_zombies",
                "Zombie Slayer",
                "500 زامبی را در طول بازی از بین ببرید",
                QuestPriority.HIGH,
                QuestPage.CHALLENGE,
                false,
                context -> context.getZombiesKilled() >= 500,
                Collections.singletonList(QuestReward.currency(QuestReward.CurrencyType.GEM, 15))
        );
    }

    public static Quest createDailyLoginQuest() {
        return new Quest(
                "daily_login",
                "Daily Visit",
                "امروز وارد بازی شوید",
                QuestPriority.DAILY,
                QuestPage.ADVENTURE,
                true,
                QuestContext::isLoggedInToday,
                Arrays.asList(
                        QuestReward.currency(QuestReward.CurrencyType.COIN, 200),
                        QuestReward.inventory("seed_packet_random", 1)
                )
        );
    }
}