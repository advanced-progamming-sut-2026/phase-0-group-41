package model.levelrules;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * محافظ دانه‌ها (Save Our Seeds): چند گیاه از قبل روی زمین کاشته شده‌اند که
 * بازیکن باید تا آخر مرحله از آن‌ها محافظت کند؛ اگر حتی یکی از بین برود،
 * بازیکن بلافاصله می‌بازد.
 */
public class SaveOurSeedsRules implements ILevelRules {

    /** موقعیت یک گیاه محافظت‌شده، برای استفاده‌ی لایه‌ی گرافیکی جهت رسم نشانگر. */
    public static final class ProtectedTile {
        public final int row;
        public final int col;
        public ProtectedTile(int row, int col) { this.row = row; this.col = col; }
    }

    private final List<Plant> endangeredPlants = new ArrayList<>();
    private final List<ProtectedTile> protectedTiles = new ArrayList<>();

    @Override
    public void setupLevel(GameSession session) {
        // کاشتن ۳ گل آفتابگردان در ستون ۳ به عنوان هدف محافظت
        for (int r = 1; r <= 3; r++) {
            Plant seed = PlantFactory.create("sunflower");
            seed.place(r, 3);
            session.getBoard().getTile(r, 3).setPlant(seed);
            endangeredPlants.add(seed);
            protectedTiles.add(new ProtectedTile(r, 3));
        }
        System.out.println("مرحله محافظت از دانه‌ها آماده شد! گیاهان در زمین مستقر شدند.");
    }

    /** لیست خانه‌هایی که باید محافظت شوند؛ لایه‌ی گرافیکی روی این خانه‌ها نشانگر می‌کشد. */
    public List<ProtectedTile> getProtectedTiles() {
        return protectedTiles;
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

    @Override
    public String getHudStatusText(GameSession session) {
        return "محافظ دانه‌ها: از گیاهان مشخص‌شده محافظت کنید";
    }
}