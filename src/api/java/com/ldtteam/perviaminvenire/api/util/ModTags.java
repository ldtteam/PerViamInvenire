package com.ldtteam.perviaminvenire.api.util;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.block.Block;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

public final class ModTags
{

    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }

    public static final Tags.IOptionalNamedTag<EntityType<?>> REPLACE_VANILLA_NAVIGATOR = tag("replace_vanilla_navigator");

    private static Tags.IOptionalNamedTag<EntityType<?>> tag(String name)
    {
        return EntityTypeTags.createOptional(new ResourceLocation(ModConstants.MOD_ID, name));
    }
}
