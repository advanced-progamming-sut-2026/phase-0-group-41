package model.levelrules;

import model.game.GameSession;
import model.game.Tile;
import model.game.TerrainType;
import model.plant.PlantFactory;
import java.util.Random;

public class PlantWhatYouGetRules implements ILevelRules {

    private int tickCounter = 0;
    private final int SPAWN_INTERVAL = 150; // هر ۱۵ ثانیه (۱۵۰ تیک)
    
    // استخری از گیاهان ممکن برای این مرحله
    private final String[] randomPlantPool = {
        "peashooter", "sunflower", "wallnut", "cherrybomb", "snowpea", "cabbagepult"
    };
    private final Random random = new Random();

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("چالش هرچه رسد بکار! گیاهان تصادفی هر ۱۵ ثانیه خودبه‌خود در زمین سبز می‌شوند.");
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        tickCounter++;
        
        if (tickCounter >= SPAWN_INTERVAL) {
            int r = random.nextInt(model.game.Board.ROWS);
            int c = random.nextInt(model.game.Board.COLS);
            Tile targetTile = session.getBoard().getTile(r, c);
            
            // چک می‌کنیم که خانه کاملاً خالی و قابل کشت باشد
            if (targetTile.isEmpty() && targetTile.getTerrainType() == TerrainType.NORMAL) {
                String randomPlant = randomPlantPool[random.nextInt(randomPlantPool.length)];
                
                // استفاده از فکتوری برای ساخت و کاشت گیاه
                targetTile.setPlant(PlantFactory.create(randomPlant));
                
                System.out.println("یک [" + randomPlant + "] تصادفی در مختصات (" + c + ", " + r + ") سبز شد!");
            }
            
            tickCounter = 0; // ریست کردن تایمر
        }
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت کلاسیک برقرار است
    }
}