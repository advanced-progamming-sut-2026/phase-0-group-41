package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;
import model.zombie.ZombieFactory;

public class GargantuarZombie extends Zombie {

    private static final double SPEED = 0.005; // سرعت بسیار کم
    private final int initialHealth;
    private boolean impThrown = false;

    public GargantuarZombie() {
        // نام، جان اولیه (۳۰۰۰)، سرعت، هزینه موج (۳۰۰)، دمیج (۹۹۹۹ برای کشتن درجا)
        super("gargantuar", 3000, SPEED, 300, 9999);
        this.initialHealth = 3000;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // ۱. بررسی پرتاب ایمپ
        checkAndThrowImp(session);

        // ۲. پیدا کردن گیاه و خونه‌ی زیر پاش
        int col = (int) Math.floor(getXPosition());
        Board board = session.getBoard();
        Tile tile = board.getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // ۳. بررسی برخورد دقیق
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            crushPlant(target, tile); // 🌟 Tile رو هم پاس می‌دیم تا بتونه گیاه مرده رو پاک کنه
        } else {
            move();
        }
    }

    // متد کوچک ۱: بررسی وضعیت پرتاب Imp
    private void checkAndThrowImp(GameSession session) {
        if (!impThrown && getHealth() <= initialHealth / 2) {
            impThrown = true;
            Zombie imp = ZombieFactory.create("imp");

            // پرتاب به ستون سوم (ایندکس ۲) در همان سطر
            imp.spawn(getRow(), 2.0);
            session.getAliveZombies().add(imp);

            // اگر بعداً خواستی برای گرافیک پیغامی چاپ کنی، اینجا جای خوبیه
        }
    }

    // متد کوچک ۲: له کردن گیاه
    private void crushPlant(Plant target, Tile tile) {
        setEating(true);
        target.takeDamage(getDamagePerTick()); // دمیج 9999 وارد میشه

        // 🌟 حتماً باید گیاه رو از زمین برداریم، وگرنه غول‌پیکر تا ابد همونجا گیر می‌کنه!
        if (target.isDead()) {
            tile.setPlant(null);
            setEating(false);
        }
    }

    // متد کوچک ۳: حرکت
    private void move() {
        setEating(false);
        setXPosition(getXPosition() - SPEED);
    }
}