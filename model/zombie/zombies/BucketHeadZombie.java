package model.zombie.zombies;

public class BucketHeadZombie extends NormalZombie {

    // جان کل = جان بدن (۲۰۰) + جان سطل (۱۱۰۰) طبق داک
    public BucketHeadZombie() {
        super("buckethead", 200 + 1100, 200, 10);
    }
}
