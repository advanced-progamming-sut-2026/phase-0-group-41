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

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        int currentPlantCount = 0;
        Board board = session.getBoard();
        
        // شمارش تمام گیاهان زنده روی نقشه
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                if (tile.getPlant() != null && !tile.getPlant().isDead()) {
                    currentPlantCount++;
                }
            }
        }

        // بررسی نقض قانون بازی
        if (currentPlantCount > maxAllowedPlants) {
            System.out.println("خطا! شما " + currentPlantCount + " گیاه کاشتید که بیشتر از حد مجاز (" + maxAllowedPlants + ") است!");
            return false; // بازیکن باخت!
        }
        
        return true; // شرایط عادی است
    }
}