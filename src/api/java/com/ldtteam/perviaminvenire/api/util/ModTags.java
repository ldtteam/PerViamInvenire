package com.ldtteam.perviaminvenire.api.util;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModTags
{

    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }

    public static final TagKey<EntityType<?>> REPLACE_VANILLA_NAVIGATOR = tag("replace_vanilla_navigator");

    private static TagKey<EntityType<?>> tag(String name)
    {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(ModConstants.MOD_ID, name));
    }
}
