package model.levelrules;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;

public class LoveYourPlantsRules implements ILevelRules {

    private final int maxAllowedPlants;

    public LoveYourPlantsRules(int maxAllowedPlants) {
        this.maxAllowedPlants = maxAllowedPlants;
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("چالش از دست نده: شما همزمان نمی‌توانید بیشتر از " + maxAllowedPlants + " گیاه در زمین داشته باشید!");
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // عملیات خاصی در هر تیک نیاز ندارد
    }

    private int countLivingPlants(GameSession session) {
        int count = 0;
        Board board = session.getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                if (tile.getPlant() != null && !tile.getPlant().isDead()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        int currentPlantCount = countLivingPlants(session);

        // بررسی نقض قانون بازی
        if (currentPlantCount > maxAllowedPlants) {
            System.out.println("خطا! شما " + currentPlantCount + " گیاه کاشتید که بیشتر از حد مجاز (" + maxAllowedPlants + ") است!");
            return false; // بازیکن باخت!
        }

        return true; // شرایط عادی است
    }

    @Override
    public String getHudStatusText(GameSession session) {
        // طبق سند: «تعداد گیاهان باقی‌مانده را نمایش دهید»
        int remaining = maxAllowedPlants - countLivingPlants(session);
        return "از دست نده: " + Math.max(0, remaining) + " گیاه دیگر می‌توانید بکارید (حداکثر " + maxAllowedPlants + ")";
    }
}