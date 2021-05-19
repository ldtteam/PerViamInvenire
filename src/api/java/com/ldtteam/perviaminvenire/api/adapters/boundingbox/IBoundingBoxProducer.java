package com.ldtteam.perviaminvenire.api.adapters.boundingbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

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
     * @param center The standing position of the entity.
     * @param facing The facing of the entity.
     * @param world The world.
     *
     * @return The bounding box.
     */
    Optional<AxisAlignedBB> produce(final Entity entity, final Vector3d center, final Vector3d facing, final IWorldReader world);
}
