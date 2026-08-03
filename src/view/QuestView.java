package view;

import controller.QuestController;
import model.quest.Quest;
import model.quest.QuestPage;
import model.quest.RewardType;
import model.user.User;
import util.CommandLine;
import java.util.List;

public class QuestView {

    private final QuestController controller;
    private final ConsoleView consoleView;

    public QuestView(QuestController controller, ConsoleView consoleView) {
        this.controller = controller;
        this.consoleView = consoleView;
    }

    public boolean checkCommand(User user, CommandLine cmd) {
        List<String> t = cmd.getTokens();
        if (t.isEmpty()) return false;
        
        String action = t.get(0);

        // دستور show quests
        if (t.size() >= 2 && action.equals("show") && t.get(1).equals("quests")) {
            showQuests(user);
            return true;
        }

        // دستور claim <quest_id>
        if (t.size() >= 2 && action.equals("claim")) {
            String questId = t.get(1);
            processClaim(user, questId);
            return true;
        }
        if (t.size() >= 4 && action.equals("travel")
                && t.get(1).equals("log")
                && t.get(2).equals("page")) {
            String pageName = t.get(3).toUpperCase(); // تا با enum ADVENTURE/CHALLENGE/... مچ بشه
            try {
                QuestPage page = QuestPage.valueOf(pageName);
                showQuestsByPage(user, page);
            } catch (IllegalArgumentException e) {
                consoleView.printError("نام صفحه نامعتبر است.");
            }
            return true;
        }

        return false;
    }
    private void showQuestsByPage(User user, QuestPage page) {
        List<Quest> quests = controller.getAllQuests(user);
        List<Quest> filtered = new java.util.ArrayList<>();
        for (Quest q : quests) {
            if (q.getPage() == page) {
                filtered.add(q);
            }
        }

        if (filtered.isEmpty()) {
            consoleView.printMessage("هیچ کوئستی در این صفحه وجود ندارد.");
            return;
        }

        consoleView.printMessage("--- Travel Log: " + page + " ---");
        for (Quest q : filtered) {
            String status = q.isClaimed() ? "[Claimed]" : (q.isCompleted() ? "[Ready to Claim]" : "[In Progress]");
            consoleView.printMessage(String.format("ID: %s | %s - %s %s",
                    q.getId(), q.getName(), q.getDescription(), status));
        }
        consoleView.printMessage("---------------------------");
    }
    private void showQuests(User user) {
        List<Quest> quests = controller.getAllQuests(user);
        if (quests.isEmpty()) {
            consoleView.printMessage("هیچ کوئستی وجود ندارد.");
            return;
        }
        
        consoleView.printMessage("--- Travel Log (Quests) ---");
        for (Quest q : quests) {
            String status = q.isClaimed() ? "[Claimed]" : (q.isCompleted() ? "[Ready to Claim]" : "[In Progress]");
            consoleView.printMessage(String.format("ID: %s | %s - %s %s",
                    q.getId(), q.getName(), q.getDescription(), status));
        }
        consoleView.printMessage("---------------------------");
    }

    private void processClaim(User user, String questId) {
        QuestController.ClaimResult result = controller.claimQuestReward(user, questId);
        
        switch (result) {
            case INVALID_ID:
                consoleView.printError("invalid quest id");
                break;
            case NOT_COMPLETED:
                consoleView.printError("این کوئست هنوز کامل نشده است.");
                break;
            case ALREADY_CLAIMED:
                consoleView.printError("پاداش این کوئست قبلاً دریافت شده است.");
                break;
            case SUCCESS:
                // برای چاپ دقیق پیام موفقیت، می‌توانستیم داده‌های پاداش را هم برگردانیم
                // اما به دلیل ساختار درخواستی در داک، یک پیام موفقیت کلی چاپ می‌کنیم
                consoleView.printMessage("پاداش کوئست با موفقیت دریافت شد!");
                break;
        }
    }
}