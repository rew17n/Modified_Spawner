package com.rew17n.modified_spawner.mob;

public class MobImpl implements MobInterface {
    int spawnerX = 0;
    int spawnerY = 0;
    int spawnerZ = 0;

    @Override
    public int getSpawnerX() {
        return spawnerX;
    }
    @Override
    public int getSpawnerY() {
        return spawnerY;
    }
    @Override
    public int getSpawnerZ() {
        return spawnerZ;
    }
    @Override
    public void setSpawnerX(int x){
        this.spawnerX = x;
    }
    @Override
    public void setSpawnerY(int y){
        this.spawnerY = y;
    }
    @Override
    public void setSpawnerZ(int z){
        this.spawnerZ = z;
    }

}
