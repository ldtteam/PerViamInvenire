package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.compat.vanilla.ICompatibilityPath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class VanillaCompatibilityJobBasedPath extends Path implements ICompatibilityPath {

    public VanillaCompatibilityJobBasedPath(final Path delegate) {
        super(delegate.nodes, delegate.target, delegate.reached);
    }

    @Override
    public @NotNull Vec3 getEntityPosAtNode(@NotNull Entity pEntity, int pIndex) {
        if (true)
            return super.getEntityPosAtNode(pEntity, pIndex);

        Node node = this.nodes.get(pIndex);

        double d0 = node.x + 0.5d;
        double d1 = node.y;
        double d2 = node.z + 0.5d;

        return new Vec3(d0, d1, d2);
    }
}
