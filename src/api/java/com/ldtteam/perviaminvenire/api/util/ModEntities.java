package com.ldtteam.perviaminvenire.api.util;

import net.minecraftforge.registries.ObjectHolder;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;

@ObjectHolder("per-viam-invenire")
public final class ModEntities {

    private ModEntities() {
        throw new IllegalStateException("Tried to initialize: ModEntities but this is a Utility class.");
    }


    @ObjectHolder("minecart")
    public static EntityType<AbstractMinecartEntity> MINECART;
}
