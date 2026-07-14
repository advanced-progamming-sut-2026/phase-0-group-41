package model.zombie.zombies;

public class ConeHeadZombie extends NormalZombie {

    // جان کل = جان بدن (۲۰۰) + جان مخروط (۳۷۰) طبق داک
    public ConeHeadZombie() {
        super("conehead", 200 + 370, 150, 10);
    }
}
