package model.greenhouse;

import java.io.Serializable;

public final class Pot implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean unlocked = false;
    private String plantName;
    private long plantedAtMillis;
    private long growDurationMillis;

    public boolean isEmpty() {
        return plantName == null;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }
    public long getPlantedAtMillis() {
        return plantedAtMillis;
    }
    public void setPlantedAtMillis(long plantedAtMillis) {
        this.plantedAtMillis = plantedAtMillis;
    }
    public long getGrowDurationMillis() {
        return growDurationMillis;
    }
    public void setGrowDurationMillis(long growDurationMillis) {
        this.growDurationMillis = growDurationMillis;
    }

    public void clear() {
        plantName = null;
        plantedAtMillis = 0;
        growDurationMillis = 0;
    }
}

