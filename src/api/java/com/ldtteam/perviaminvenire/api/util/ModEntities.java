package com.ldtteam.perviaminvenire.api.util;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@ObjectHolder("per-viam-invenire")
public final class ModEntities {

    private ModEntities() {
        throw new IllegalStateException("Tried to initialize: ModEntities but this is a Utility class.");
    }


    @ObjectHolder("minecart")
    public static EntityType<AbstractMinecart> MINECART;
}
