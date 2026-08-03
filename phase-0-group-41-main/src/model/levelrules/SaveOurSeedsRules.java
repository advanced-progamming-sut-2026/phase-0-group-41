package model.levelrules;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantFactory;
import java.util.ArrayList;
import java.util.List;

public class SaveOurSeedsRules implements ILevelRules {

    private final List<Plant> endangeredPlants = new ArrayList<>();

    @Override
    public void setupLevel(GameSession session) {
        // کاشتن ۳ گل آفتابگردان در ستون ۳ به عنوان هدف محافظت
        for (int r = 1; r <= 3; r++) {
            Plant seed = PlantFactory.create("sunflower");
            seed.place(r, 3);
            session.getBoard().getTile(r, 3).setPlant(seed);
            endangeredPlants.add(seed);
        }
        System.out.println("مرحله محافظت از دانه‌ها آماده شد! گیاهان در زمین مستقر شدند.");
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // اتفاق خاصی در حین تیک نمی‌افتد
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        for (Plant p : endangeredPlants) {
            if (p.isDead()) {
                System.out.println("هشدار: زامبی‌ها یکی از گیاهان محافظت‌شده را خوردند!");
                return false; // بازیکن باخت!
            }
        }
        return true;
    }
}