package com.ldtteam.perviaminvenire.api.pathfinding.registry;

import java.util.function.Function;
import java.util.function.Predicate;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedPathNavigate;

import net.minecraft.entity.MobEntity;

public interface IPathNavigateRegistry
{

    static IPathNavigateRegistry getInstance()
    {
        return IPerViamInvenireApi.getInstance().getPathNavigateRegistry();
    }

    IPathNavigateRegistry registerNewPathNavigate(Predicate<MobEntity> selectionPredicate, Function<MobEntity, AbstractAdvancedPathNavigate> navigateProducer);

    AbstractAdvancedPathNavigate getNavigateFor(MobEntity entityLiving);
}
