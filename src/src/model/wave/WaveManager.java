package model.wave;

public class WaveManager {

    private final int totalWaves;
    private int currentWave = 0;
    private boolean waveActive = false;
    private double baseCost;

    public WaveManager(int totalWaves, double firstWaveCost) {
        this.totalWaves = totalWaves;
        this.baseCost = firstWaveCost;
    }

    public int getTotalWaves() {
        return totalWaves;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public boolean isFinalWave() {
        return currentWave == totalWaves;
    }

    public boolean allWavesDone() {
        return currentWave >= totalWaves && !waveActive;
    }

    public boolean isWaveActive() {
        return waveActive;
    }

    public void setWaveActive(boolean waveActive) {
        this.waveActive = waveActive;
    }

    /** هزینه هر موج ۲۵٪ سخت‌تر از موج قبل است؛ موج آخر (flag) دو برابر موج قبلی است. */
    public double startNextWaveAndGetCost() {
        currentWave++;
        double cost;
        if (currentWave == 1) {
            cost = baseCost;
        } else if (isFinalWave()) {
            cost = baseCost * 2;
        } else {
            cost = baseCost * 1.25;
        }
        baseCost = cost;
        waveActive = true;
        return cost;
    }
}
