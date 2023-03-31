package com.rew17n.modified_spawner.spawner;

import com.rew17n.modified_spawner.ModifiedSpawner;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class SpawnerProvider implements ICapabilitySerializable<CompoundTag> {
    SpawnerInterface instance = new SpawnerImpl();
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        return ModifiedSpawner.INSTANCE_SPAWNER.orEmpty(cap, LazyOptional.of(() -> instance));
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("spawner", instance.getSpawned());
        return nbt;
    }
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setSpawned(nbt.getInt("spawner"));
    }
}