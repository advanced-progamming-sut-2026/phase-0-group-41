package model.projectile;

import model.game.GameSession;

public abstract class Projectile {
    protected int row;
    protected double x;
    protected int damage;
    protected double speed;
    protected boolean isDead = false;

    protected boolean isFire = false;
    protected boolean isIce = false;

    public Projectile(int row, double startX, int damage, double speed) {
        this.row = row;
        this.x = startX;
        this.damage = damage;
        this.speed = speed;
    }

    public int getRow() { return row; }
    public double getX() { return x; }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public boolean isDead() { return isDead; }
    public void setDead(boolean dead) { this.isDead = dead; }

    public boolean isFire() { return isFire; }
    public void setFire(boolean fire) { isFire = fire; }

    public boolean isIce() { return isIce; }
    public void setIce(boolean ice) { isIce = ice; }
    
    // هر پرتابه منطق حرکت و برخورد خودش را اینجا پیاده می‌کند
    public abstract void onTick(GameSession session);
}