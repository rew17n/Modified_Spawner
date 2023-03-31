package com.rew17n.modified_spawner.mob;

import com.rew17n.modified_spawner.ModifiedSpawner;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class MobProvider implements ICapabilitySerializable<CompoundTag> {
    MobInterface instance = new MobImpl();
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        return ModifiedSpawner.INSTANCE_MOB.orEmpty(cap, LazyOptional.of(() -> instance));
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("mob", instance.getSpawnerX());
        nbt.putInt("mob", instance.getSpawnerY());
        nbt.putInt("mob", instance.getSpawnerZ());
        return nbt;
    }
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setSpawnerX(nbt.getInt("mob"));
        instance.setSpawnerY(nbt.getInt("mob"));
        instance.setSpawnerZ(nbt.getInt("mob"));
    }
}