package com.rew17n.modified_spawner.spawner;

public class SpawnerImpl implements SpawnerInterface {
    int spawned = 0;
    @Override
    public int getSpawned() {
        return spawned;
    }
    @Override
    public void setSpawned(int spawned) {
        this.spawned = spawned;
    }
    @Override
    public void increaseSpawned() {
        spawned++;
    }
}
