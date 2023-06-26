package com.ldtteam.perviaminvenire.compat.util;

import com.ldtteam.perviaminvenire.api.pathfinding.ExtendedNode;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

public class PathLengthAwarePath extends Path {

    private final Path delegate;

    public PathLengthAwarePath(Path delegate) {
        super(delegate.nodes, delegate.getTarget(), delegate.reached);
        this.delegate = delegate;
    }

    @Override
    public @NotNull Node getNode(int pIndex) {
        if (pIndex < 0 || pIndex >= this.nodes.size()) {
            if (this.nodes.size() == 0) {
                return new ExtendedNode(delegate.target);
            }

            if (pIndex < 0) {
                return new ExtendedNode(this.nodes.get(0).asBlockPos());
            }

            return new ExtendedNode(this.nodes.get(this.nodes.size() - 1).asBlockPos());
        }

        return super.getNode(pIndex);
    }
}
