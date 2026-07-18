package view;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class ConsoleView {

    // ساخت یک نمونه از ZombieView که نمایش زامبی‌ها رو بهش بسپریم
    private final ZombieView zombieView = new ZombieView();

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String message) {
        System.out.println("[error] " + message);
    }

    public void printMap(GameSession session) {
        // (کدهای چاپ مپ دقیقاً همون چیزی که خودت داشتی...)
        Board board = session.getBoard();
        System.out.println("Wave: " + session.getWaveManager().getCurrentWave() + "/" + session.getWaveManager().getTotalWaves()
                + " | Sun: " + session.getSunManager().getCurrentSun()
                + " | PlantFood: " + session.getPlantFoodCount());
        for (int r = 0; r < Board.ROWS; r++) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append(board.isLawnMowerUsed(r) ? "[X]" : "[M]").append(' ');
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                rowBuilder.append(renderTile(tile)).append(' ');
            }
            System.out.println(rowBuilder.toString());
        }
    }

    private String renderTile(Tile tile) {
        // (دقیقاً همون کد خودت برای کشیدن Tile ها)
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        switch (tile.getTerrainType()) {
            case GRAVE: sb.append('G'); break;
            case WATER: sb.append('~'); break;
            case ICE_SLIPPERY: sb.append('I'); break;
            default: sb.append('.');
        }
        Plant plant = tile.getPlant();
        if (plant != null) {
            sb.append(plant.getName().charAt(0));
        } else {
            sb.append('_');
        }
        if (tile.hasZombie()) {
            sb.append('Z').append(tile.getZombies().size());
        } else {
            sb.append("__");
        }
        sb.append(']');
        return sb.toString();
    }

    // ✨ اینجا کار رو می‌سپریم به کلاس ZombieView ✨
    public void printZombieInfo(Zombie z) {
        zombieView.displayInfo(z);
    }
}