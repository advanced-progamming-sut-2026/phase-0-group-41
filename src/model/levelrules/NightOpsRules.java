package model.levelrules;

import model.game.GameSession;
import model.game.TerrainType;
import model.game.Tile;
import model.game.Grave;
import java.util.Random;

public class NightOpsRules implements ILevelRules {

    private int tickCounter = 0;
    private final Random random = new Random();

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("عملیات شبانه آغاز شد! سقوط خورشید از آسمان غیرفعال است.");
        // دادن مقداری خورشید اولیه به عنوان کمک
        session.getSunManager().addSun(100); 
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        tickCounter++;
        
        // هر ۲۰ ثانیه (۲۰۰ تیک) یک قبر جدید به صورت تصادفی در زمین ظاهر می‌شود
        if (tickCounter >= 200) {
            int r = random.nextInt(model.game.Board.ROWS);
            int c = random.nextInt(model.game.Board.COLS);
            Tile targetTile = session.getBoard().getTile(r, c);
            
            if (targetTile.isEmpty() && targetTile.getTerrainType() == TerrainType.NORMAL) {
                targetTile.setTerrainType(TerrainType.GRAVE);
                targetTile.setGrave(new Grave(false, false));
                System.out.println("به دلیل تاریکی شب، یک قبر جدید در مختصات (" + c + ", " + r + ") سبز شد!");
            }
            tickCounter = 0;
        }
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت همان رسیدن زامبی به انتهای نقشه است
    }
}