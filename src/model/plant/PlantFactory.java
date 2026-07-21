package model.plant;

import model.plant.plants.CherryBomb;
import model.plant.plants.Peashooter;
import model.plant.plants.PotatoMine;
import model.plant.plants.Sunflower;
import model.plant.plants.WallNut;

import java.util.Arrays;
import java.util.List;

/**
 * برای افزودن گیاه جدید: کلاس آن را در پکیج plants بسازید (extend از Plant) و
 * یک case به این factory اضافه کنید. الگوی این ۵ گیاه نمونه، برای بقیه‌ی
 * دسته‌بندی‌های گفته‌شده در داک (Lobber، Melee، Modifier، Homing، Mint و ...) قابل تکرار است.
 */
public final class PlantFactory {

    private static final List<String> KNOWN_PLANTS =
            Arrays.asList("peashooter", "sunflower", "wallnut", "cherrybomb", "potatomine");

    private PlantFactory() {
    }

    public static boolean isKnown(String plantName) {
        return KNOWN_PLANTS.contains(plantName);
    }

    public static List<String> allPlantNames() {
        return KNOWN_PLANTS;
    }

    public static Plant create(String plantName) {
        switch (plantName) {
            case "peashooter":
                return new Peashooter();
            case "sunflower":
                return new Sunflower();
            case "wallnut":
                return new WallNut();
            case "cherrybomb":
                return new CherryBomb();
            case "potatomine":
                return new PotatoMine();
            default:
                throw new IllegalArgumentException("گیاه ناشناخته: " + plantName);
        }
    }
}
