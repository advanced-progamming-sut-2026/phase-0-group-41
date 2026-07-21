package model.zombie;

import model.zombie.zombies.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ZombieFactory {

    private static final List<String> BASIC_ZOMBIE_TYPES = Arrays.asList("normal", "conehead", "buckethead");
    private static final Random RANDOM = new Random();

    private ZombieFactory() {}

    public static Zombie create(String type) {
        switch (type.toLowerCase()) {
            // === زامبی‌های پایه و قدیمی ===
            case "normal":
                return new NormalZombie();
            case "conehead":
                // یک زامبی نرمال می‌سازیم، یک کلاه ۳۷۰تایی می‌ذاریم سرش
                return new ArmorDecorator(new NormalZombie(), "cone", 370, 150);
            case "buckethead":
                // یک زامبی نرمال می‌سازیم، یک سطل ۱۱۰۰تایی می‌ذاریم سرش
                return new ArmorDecorator(new NormalZombie(), "bucket", 1100, 200);
            case "blockhead":
            case "block":
                // سربلوکی: زرهی بسیار سنگین‌تر از سطل (مثلاً ۲۲۰۰ جان زره برای بلوک سنگی/آجری)
                return new ArmorDecorator(new NormalZombie(), "block", 2200, 250);

            case "knight":
                // شوالیه: قوی‌ترین زره پایه بازی با کلاه‌خود و شانه‌بند (مثلاً ۳۰۰۰ جان زره)
                return new ArmorDecorator(new NormalZombie(), "knight", 3000, 300);
            // === زامبی‌های جدید و تخصصی ===
            case "explorer":
                return new ExplorerZombie();
            case "hunter":
                return new HunterZombie();
            case "troglobite":
                return new TroglobiteZombie();
            case "snorkel":
                return new SnorkelZombie();
            case "octopus":
                return new OctopusZombie();
            case "jester":
            case "juggler": // پشتیبانی از هر دو نام برای دلقک
                return new JesterZombie();
            case "wizard":
                return new WizardZombie();
            case "king":
                return new KingZombie();
            case "imp_dragon":
                return new ImpDragonZombie();
            case "imp":
                return new ImpZombie();
            case "gargantuar":
                return new GargantuarZombie();
            case "newspaper":
                return new  NewspaperZombie();
            case"prospector":
                return new ProspectorZombie();
            case"ra":
                return new RaZombie();
            case"tombraiser":
                return new TombraiserZombie();
            case "turquoise":
                return new TurquoiseZombie();
            case "dodorider":
                return new DodoRiderZombie();
            case "pianist":
                return new PianistZombie();
                default:
                throw new IllegalArgumentException("زامبی ناشناخته: " + type);
        }
    }

    public static Zombie randomBasicZombie() {
        String type = BASIC_ZOMBIE_TYPES.get(RANDOM.nextInt(BASIC_ZOMBIE_TYPES.size()));
        return create(type);
    }
}