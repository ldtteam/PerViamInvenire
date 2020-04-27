package com.ldtteam.perviaminvenire.api.adapters.registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;


import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

/**
 * Registry used to register adapters for start positions.
 */
public interface IStartPositionAdapterRegistry {

    /**
     * Returns the current instance.
     */
    static IStartPositionAdapterRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getStartPositionAdapterRegistry();
    }

    /**
     * Registers a new adapter for when the start position is a given block.
     *
     * @param adapter The adapter.
     * @param predicates The predicates that determine if the adapter should be applied or not.
     * @return The registry.
     */
    default IStartPositionAdapterRegistry registerForBlocks(final IStartPositionAdapter adapter, final Predicate<Block>... predicates) {
        return this.registerForBlocks(adapter, Arrays.asList(predicates));
    }

    /**
     * Registers a new adapter for when the start position is a given block.
     *
     * @param adapter The adapter.
     * @param predicates The predicates that determine if the adapter should be applied or not.
     * @return The registry.
     */
    IStartPositionAdapterRegistry registerForBlocks(final IStartPositionAdapter adapter, final Collection<Predicate<Block>> predicates);

    /**
     * Gets the adapter for a given block.
     * If multiple are valid then the first one is returned.
     *
     * @param block The block.
     * @return The adapter.
     */
    Optional<IStartPositionAdapter> getForBlock(final Block block);

    /**
     * Registers a new adapter for when the start position matches an entities condition.
     * @param adapter The adapter.
     * @param predicates The predicates that determine if the adapter should be applied or not.
     * @return The registry.
     */
    default IStartPositionAdapterRegistry registerForEntity(final IStartPositionAdapter adapter, final Predicate<Entity>... predicates)
    {
        return this.registerForEntity(adapter, Arrays.asList(predicates));
    }

    /**
     * Registers a new adapter for when the start position matches an entities condition.
     * @param adapter The adapter.
     * @param predicates The predicates that determine if the adapter should be applied or not.
     * @return The registry.
     */
    IStartPositionAdapterRegistry registerForEntity(final IStartPositionAdapter adapter, final Collection<Predicate<Entity>> predicates);

    /**
     * Gets the adapter for a given entity.
     * If multiple are valid then the first one is returned.
     *
     * @param entity The entity.
     * @return The adapter.
     */
    Optional<IStartPositionAdapter> getForEntity(final Entity entity);
}
