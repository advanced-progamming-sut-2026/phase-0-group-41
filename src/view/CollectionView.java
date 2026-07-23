package view;

import model.plant.Plant;
import model.zombie.Zombie;

public class CollectionView {

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String message) {
        System.out.println("[error] " + message);
    }

    public void printList(Iterable<String> items, String title) {
        System.out.println("=== " + title + " ===");
        for (String item : items) {
            System.out.println("- " + item);
        }
    }

    public void printPlantDetails(Plant plant, int level, int seedPackets) {
        System.out.println("--- مشخصات گیاه ---");
        System.out.println("نام: " + plant.getName());
        System.out.println("سطح (Level): " + level);
        System.out.println("تعداد Seed Packet ها: " + seedPackets);
        System.out.println("میزان سلامتی: " + plant.getMaxHealth());
        System.out.println("هزینه خورشید: " + plant.getSunCost());
    }

    public void printZombieDetails(Zombie zombie) {
        System.out.println("--- مشخصات زامبی ---");
        System.out.println("نام: " + zombie.getTypeName());
        System.out.println("میزان سلامتی: " + zombie.getMaxHealth());
        System.out.println("سرعت: " + zombie.getSpeed());
    }
}