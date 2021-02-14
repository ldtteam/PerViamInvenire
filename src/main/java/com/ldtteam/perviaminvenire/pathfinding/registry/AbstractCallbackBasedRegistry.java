package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public abstract class AbstractCallbackBasedRegistry<R extends ICallbackBasedRegistry<R, T>, T> implements ICallbackBasedRegistry<R, T> {

    protected final LinkedHashSet<T> registeredCallbacks = new LinkedHashSet<>();

    private boolean frozen          = false;
    private List<T>       frozenCallbacks = Lists.newArrayList();

    @Override
    public abstract R getThis();

    @Override
    public final R register(final Collection<T> callbacks) {
        if (this.frozen)
            throw new IllegalStateException("Registry is frozen");

        synchronized (this.registeredCallbacks) {
            this.registeredCallbacks.addAll(callbacks);
        }
        return getThis();
    }

    @Override
    public final T getRunner() {
        this.freeze();
        return this.getRunnerInternal(this.frozenCallbacks);
    }

    protected abstract T getRunnerInternal(final List<T> callbacks);

    private void freeze() {
        if (this.frozen)
            return;
        this.frozen = true;

        this.frozenCallbacks = new LinkedList<>(this.registeredCallbacks);
        Collections.reverse(this.frozenCallbacks);
    }
}
