package view;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.greenhouse.Greenhouse;
import model.plant.Plant;
import model.user.User;
import model.zombie.Zombie;

import java.util.Map;

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
    public void printGreenhouse(User user) {
        Greenhouse greenhouse = user.getGreenhouse();
        System.out.println("---- Greenhouse ----");
        System.out.println("   Col 1   Col 2   Col 3   Col 4   Col 5");
        for (int r = 0; r < Greenhouse.ROWS; r++) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("Row ").append(r + 1).append(" ");
            for (int c = 0; c < Greenhouse.COLS; c++) {
                String status;
                if (greenhouse.isLocked(r, c)) {
                    status = "LOCKED";
                } else if (greenhouse.isEmpty(r, c)) {
                    status = "EMPTY";
                } else if (greenhouse.isReady(r, c)) {
                    status = "READY:" + greenhouse.getPlantName(r, c);
                } else {
                    status = greenhouse.getPlantName(r, c) + "(" + Greenhouse.formatDuration(greenhouse.getRemainingMillis(r, c)) + ")";
                }
                rowBuilder.append(String.format("%-8s", status)).append(" ");
            }
            System.out.println(rowBuilder.toString().trim());
        }
        System.out.println("---------------------");
    }

    public void printZombieInfo(Zombie z) {
        zombieView.displayInfo(z);
    }
}