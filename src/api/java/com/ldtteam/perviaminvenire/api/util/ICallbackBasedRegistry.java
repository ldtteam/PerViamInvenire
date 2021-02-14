package com.ldtteam.perviaminvenire.api.util;

import java.util.Arrays;
import java.util.Collection;

public interface ICallbackBasedRegistry<R extends ICallbackBasedRegistry<R,T>, T> {

    R getThis();

    default R register(T... callbacks) {
        this.register(Arrays.asList(callbacks));
        return getThis();
    }

    R register(final Collection<T> callbacks);

    T getRunner();
}
