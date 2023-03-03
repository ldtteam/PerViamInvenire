package com.ldtteam.perviaminvenire.api.adapters.boundingbox;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelReader;

import java.util.Optional;

/**
 * Callback used to create a bounding box for an entity which is standing on the given position and facing the given facing
 * in the given world.
 */
@FunctionalInterface
public interface IBoundingBoxProducer
{
    /**
     * The producer for the bounding box.
     *
     * @param entity The entity.
     *
     * @return The bounding box.
     */
    Optional<AABB> produce(final Entity entity);
}
