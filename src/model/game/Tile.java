package model.game;

import model.plant.Plant;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final int row;
    private final int col;
    private TerrainType terrainType = TerrainType.NORMAL;
    private Plant plant;
    private final List<Zombie> zombies = new ArrayList<>();

    // === ویژگی‌های مربوط به غارهای یخی (Frostbite Caves) ===
    private boolean iceBlock = false;
    public enum SliderDirection { NONE, UP, DOWN } // جهت سر خوردن
    private SliderDirection sliderDirection = SliderDirection.NONE;

    // === ویژگی‌های مربوط به ساحل موج بزرگ (Big Wave Beach) ===
    private boolean isWater = false;
    private boolean hasLilyPad = false;
    private boolean isLowTideBeach = false; // خانه‌های ساحل پست (امکان خروج زامبی از زیر آب)

    // === ویژگی‌های مربوط به مصر باستان و قرون وسطی (Graves & Dark Ages) ===
    private Grave grave = null; // جایگزین graveHealth شد
    private boolean isNecromancyTile = false; // اگر true باشد، اول موج از زیر قبر زامبی درمی‌آید


    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // گترها و سترهای اصلی
    public int getRow() { return row; }
    public int getCol() { return col; }

    public TerrainType getTerrainType() { return terrainType; }
    public void setTerrainType(TerrainType terrainType) { this.terrainType = terrainType; }

    public Plant getPlant() { return plant; }
    public void setPlant(Plant plant) { this.plant = plant; }
    public boolean isEmpty() { return plant == null && grave == null; } // اگر قبر هم باشد خالی نیست

    public List<Zombie> getZombies() { return zombies; }
    public boolean hasZombie() { return !zombies.isEmpty(); }

    // === متدهای قبر (Grave) ===
    public Grave getGrave() { return grave; }
    public void setGrave(Grave grave) { this.grave = grave; }
    public boolean hasGrave() { return this.grave != null && !this.grave.isDestroyed(); }

    public void damageGrave(int amount) {
        if (grave != null) {
            grave.takeDamage(amount);
            if (grave.isDestroyed()) {
                this.grave = null; // قبر نابود شد و از روی خانه برداشته می‌شود
                this.terrainType = TerrainType.NORMAL;
            }
        }
    }

    public boolean isNecromancyTile() { return isNecromancyTile; }
    public void setNecromancyTile(boolean necromancyTile) { isNecromancyTile = necromancyTile; }

    // === متدهای آب و ساحل ===
    public boolean isWater() { return isWater; }
    public void setWater(boolean water) { isWater = water; }

    public boolean isHasLilyPad() { return hasLilyPad; }
    public void setHasLilyPad(boolean hasLilyPad) { this.hasLilyPad = hasLilyPad; }

    public boolean isLowTideBeach() { return isLowTideBeach; }
    public void setLowTideBeach(boolean lowTideBeach) { isLowTideBeach = lowTideBeach; }

    // === متدهای یخ و زمین لیز ===
    public boolean hasIceBlock() { return this.iceBlock; }
    public void setIceBlock(boolean hasIce) { this.iceBlock = hasIce; }
    public void removeIceBlock() { this.iceBlock = false; }

    public SliderDirection getSliderDirection() { return sliderDirection; }
    public void setSliderDirection(SliderDirection sliderDirection) { this.sliderDirection = sliderDirection; }

    // === منطق کاشت گیاه ===
    public boolean canPlantDirectly() {
        if (hasGrave()) return false; // روی قبر نمی‌توان کاشت
        if (isWater) return hasLilyPad; // در آب فقط روی لیلی‌پد می‌توان کاشت (مگر اینکه گیاه آبی باشد که جداگانه چک می‌شود)
        return terrainType == TerrainType.NORMAL;
    }
}