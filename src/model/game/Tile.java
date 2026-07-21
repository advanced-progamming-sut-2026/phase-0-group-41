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
    private int graveHealth = 700;
    private boolean hasLilyPad = false;
    private boolean iceBlock = false;

    public boolean hasIceBlock() {
        return this.iceBlock;
    }

    public void setIceBlock(boolean hasIce) {
        this.iceBlock = hasIce;
    }

    public void removeIceBlock() {
        this.iceBlock = false;
    }
    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public boolean isEmpty() {
        return plant == null;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public boolean hasZombie() {
        return !zombies.isEmpty();
    }

    public int getGraveHealth() {
        return graveHealth;
    }

    public void damageGrave(int amount) {
        graveHealth -= amount;
        if (graveHealth <= 0) {
            terrainType = TerrainType.NORMAL;
        }
    }

    public boolean isHasLilyPad() {
        return hasLilyPad;
    }

    public void setHasLilyPad(boolean hasLilyPad) {
        this.hasLilyPad = hasLilyPad;
    }

    public boolean canPlantDirectly() {
        return terrainType == TerrainType.NORMAL || hasLilyPad;
    }
}
