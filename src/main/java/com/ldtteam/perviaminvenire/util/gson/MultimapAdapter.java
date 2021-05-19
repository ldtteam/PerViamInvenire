package com.ldtteam.perviaminvenire.util.gson;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MultimapAdapter implements JsonDeserializer<Multimap<?, ?>>, JsonSerializer<Multimap<?, ?>>
{
    @Override
    public Multimap<?, ?> deserialize(
      JsonElement json, Type type,
      JsonDeserializationContext context) throws JsonParseException
    {
        final HashMultimap<Object, Object> result = HashMultimap.create();
        final Map<?, Collection<?>> map = context.deserialize(json, multimapTypeToMapType(type));
        for (final Map.Entry<?, ?> e : map.entrySet()) {
            final Collection<?> value = (Collection<?>) e.getValue();
            result.putAll(e.getKey(), value);
        }
        return result;
    }

    @Override
    public JsonElement serialize(Multimap<?, ?> src, Type type, JsonSerializationContext context) {
        final Map<?, ?> map = src.asMap();
        return context.serialize(map);
    }

    private <K, V> Type multimapTypeToMapType(Type type) {
        final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
        assert typeArguments.length == 2;
        @SuppressWarnings("unchecked")
        final TypeToken<Map<K, Collection<V>>> mapTypeToken = new TypeToken<Map<K, Collection<V>>>() {}
                 .where(new TypeParameter<K>() {}, (TypeToken<K>) TypeToken.of(typeArguments[0]))
                 .where(new TypeParameter<V>() {}, (TypeToken<V>) TypeToken.of(typeArguments[1]));

        return mapTypeToken.getType();
    }
}