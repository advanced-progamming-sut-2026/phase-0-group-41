package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;

public class WallNut extends Plant {

    public WallNut() {
        super("wallnut", PlantType.WALL_NUT, 50, 300, 4000);
    }

    @Override
    public void onTick(GameSession session) {
        // گردو صرفا سد راه است؛ رفتار فعالی ندارد.
    }
}
