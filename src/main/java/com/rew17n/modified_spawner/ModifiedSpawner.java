package com.rew17n.modified_spawner;

import com.rew17n.modified_spawner.mob.MobInterface;
import com.rew17n.modified_spawner.mob.MobProvider;
import com.rew17n.modified_spawner.spawner.SpawnerInterface;
import com.rew17n.modified_spawner.spawner.SpawnerProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static net.minecraftforge.eventbus.api.Event.Result.DENY;

@Mod(com.rew17n.modified_spawner.ModifiedSpawner.MODID)
public class ModifiedSpawner {
    public static final String MODID = "modified_spawner";
    private static final ResourceLocation CAP_SPAWNER = new ResourceLocation(MODID, "spawner");
    public static Capability<SpawnerInterface> INSTANCE_SPAWNER = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation CAP_MOB = new ResourceLocation(MODID, "mob");
    public static Capability<MobInterface> INSTANCE_MOB = CapabilityManager.get(new CapabilityToken<>() {});
    public static ForgeConfigSpec.ConfigValue<Integer> LIMIT;
    public static ForgeConfigSpec.ConfigValue<Integer> EXP_BREAK_PLAYER;
    public static ForgeConfigSpec.ConfigValue<Integer> EXP_BREAK_LIMIT;
    public static ForgeConfigSpec.ConfigValue<Boolean> BREAK_IN_LIMIT;
    public static ForgeConfigSpec CONFIG;

    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        BUILDER.push(MODID);
        LIMIT = BUILDER.comment("Limit of mobs that can be killed\n-->Default 20").define("limit", 20);
        EXP_BREAK_PLAYER = BUILDER.comment("Experience extra that a spawner drop when is break by player\n-->Default 30 (Around 18 levels)").define("xp_break_player", 30);
        EXP_BREAK_LIMIT = BUILDER.comment("Experience extra that a spawner drop when is break by limit\n-->Default 10 (Around 10 levels)").define("xp_break_limit", 10);
        BREAK_IN_LIMIT = BUILDER.comment("Break when spawner reach limit\n-->Default true (true or false)").define("break_in_limit", true);

        BUILDER.pop();
        CONFIG = BUILDER.build();
    }
    public ModifiedSpawner(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
    }
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(SpawnerInterface.class);
        event.register(MobInterface.class);
    }
    @Mod.EventBusSubscriber()
    public static class Event {
        @SubscribeEvent
        public static void addCapSpawner(AttachCapabilitiesEvent<BlockEntity> event) {
            if (event.getObject() instanceof SpawnerBlockEntity) {
                event.addCapability(CAP_SPAWNER, new SpawnerProvider());
            }
        }
        @SubscribeEvent
        public static void addCapMob(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity && !(event.getObject() instanceof Player)) {
                event.addCapability(CAP_MOB, new MobProvider());
            }

        }

        @SubscribeEvent
        public static void checkMobSpawn(LivingSpawnEvent.CheckSpawn event) {
            if (!event.getLevel().isClientSide() && event.getSpawnReason() == MobSpawnType.SPAWNER) {
                BaseSpawner spawner = event.getSpawner();
                if (spawner.getSpawnerBlockEntity() instanceof SpawnerBlockEntity spawnerBlockEntity) {
                    spawnerBlockEntity.getCapability(INSTANCE_SPAWNER).ifPresent(cap -> {
                        if (cap.getSpawned() >= LIMIT.get()) {
                            event.setResult(DENY);
                        }else{
                            event.getEntity().getCapability(INSTANCE_MOB).ifPresent(cap_mob -> {
                                BlockPos position = spawnerBlockEntity.getBlockPos();
                                cap_mob.setSpawnerX(position.getX());
                                cap_mob.setSpawnerY(position.getY());
                                cap_mob.setSpawnerZ(position.getZ());
                            });
                        }
                    });

                }
            }
        }

        @SubscribeEvent
        public static void checkDeathMob(LivingDeathEvent event) {
            DamageSource source = event.getSource();
            Level level = event.getEntity().getLevel();
            if(!level.isClientSide() && (source.getEntity() instanceof Player)){ //Verifica si se ejecuto en el servidor y Verifica si es un jugador el que mato
                event.getEntity().getCapability(INSTANCE_MOB).ifPresent(cap -> {
                    BlockPos blockPos = new BlockPos(cap.getSpawnerX(),cap.getSpawnerY(),cap.getSpawnerZ());
                    if(level.getBlockEntity(blockPos) instanceof SpawnerBlockEntity block) {
                        block.getCapability(INSTANCE_SPAWNER).ifPresent(cap_spawner -> {
                            cap_spawner.increaseSpawned();
                            if (cap_spawner.getSpawned() >= LIMIT.get()) {
                                if(BREAK_IN_LIMIT.get()){
                                    level.removeBlock(blockPos,false);
                                    dropExperience((ServerLevel) event.getEntity().getLevel(), EXP_BREAK_LIMIT.get(), blockPos);
                                }

                            }
                        });
                    }
                });
            }
        }

        @SubscribeEvent
        public static void checkBreakSpawner(BlockEvent.BreakEvent event){
            if(!event.getLevel().isClientSide() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, event.getPlayer()) <= 0){
                if (event.getState().getBlock().toString().equals("Block{minecraft:spawner}")) {
                    int amount = EXP_BREAK_PLAYER.get();
                    if (amount > 0){
                        dropExperience((ServerLevel) event.getLevel(), amount, event.getPos());
                    }
                }
            }

        }

        private static void dropExperience(ServerLevel level, int amount, BlockPos position) {
            for (int i = 0; i < 25; i++) {
                level.addFreshEntity(new ExperienceOrb(
                    level,
                    (double)position.getX() + 0.5D,
                    (double)position.getY() + 0.5D,
                    (double)position.getZ() + 0.5D,
                    ExperienceOrb.getExperienceValue(amount)
                ));
            }


        }

    }
}
