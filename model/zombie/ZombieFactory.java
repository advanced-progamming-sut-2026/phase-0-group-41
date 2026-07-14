package zombie;

import model.zombie.zombies.BucketHeadZombie;
import model.zombie.zombies.ConeHeadZombie;
import model.zombie.zombies.NormalZombie;

import java.util.Arrays;ssss
import java.util.List;
import java.util.Random;

/**
 * برای افزودن زامبی جدید: کلاس آن را در پکیج zombies بسازید (extend از Zombie یا NormalZombie)
 * و به این factory اضافه کنید. الگوی زیر برای بقیه‌ی زامبی‌های داک (شوالیه، سربلوکی، Gargantuar،
 * Imp و زامبی‌های مخصوص هر فصل) قابل تکرار است.
 */
public final class ZombieFactory {

    private static final List<String> BASIC_ZOMBIE_TYPES = Arrays.asList("normal", "conehead", "buckethead");
    private static final Random RANDOM = new Random();

    private ZombieFactory() {
    }

    public static Zombie create(String type) {
        switch (type) {
            case "normal":
                return new NormalZombie();
            case "conehead":
                return new ConeHeadZombie();
            case "buckethead":
                return new BucketHeadZombie();
            default:
                throw new IllegalArgumentException("زامبی ناشناخته: " + type);
        }
    }

    public static Zombie randomBasicZombie() {
        String type = BASIC_ZOMBIE_TYPES.get(RANDOM.nextInt(BASIC_ZOMBIE_TYPES.size()));
        return create(type);
    }
}
