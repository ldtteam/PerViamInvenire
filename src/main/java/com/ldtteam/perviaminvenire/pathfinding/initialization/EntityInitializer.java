package com.ldtteam.perviaminvenire.pathfinding.initialization;

import com.ldtteam.perviaminvenire.api.util.ModEntities;
import com.ldtteam.perviaminvenire.entity.PVIMinecart;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;

@ObjectHolder("per-viam-invenire")
@Mod.EventBusSubscriber(modid = "per-viam-invenire", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EntityInitializer {

    private EntityInitializer() {
        throw new IllegalStateException("Tried to initialize: EntityInitializer but this is a Utility class.");
    }

    public static void setupEntities() {
        ModEntities.MINECART = (EntityType<AbstractMinecartEntity>) EntityType.Builder.create(PVIMinecart::new, EntityClassification.MISC)
                                                                                    .setTrackingRange(256)
                                                                                    .setUpdateInterval(2)
                                                                                    .size(0.98F, 0.7F)
                                                                                    .build("per-viam-invenire:minecart")
                                                                                    .setRegistryName("per-viam-invenire:minecart");
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry()
                        .registerAll(ModEntities.MINECART);
    }
}
