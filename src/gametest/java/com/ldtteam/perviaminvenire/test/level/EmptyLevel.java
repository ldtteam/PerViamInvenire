package com.ldtteam.perviaminvenire.test.level;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.LevelTicks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class EmptyLevel extends Level {

    public EmptyLevel() {
        super(new ClientLevel.ClientLevelData(Difficulty.PEACEFUL, true, true),
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(ModConstants.MOD_ID, "pvi_template")),
                BuiltinRegistries.DIMENSION_TYPE.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> null,
                false,
                false,
                0,
                0);
    }

    @Override
    public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {

    }

    @Override
    public void playSeededSound(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSoundSource, float pVolume, float pPitch, long pSeed) {

    }

    @Override
    public void playSeededSound(@Nullable Player pPlayer, Entity pEntity, SoundEvent pSoundEvent, SoundSource pSoundSource, float pVolume, float pPitch, long pSeed) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Nullable
    @Override
    public Entity getEntity(int pId) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String pMapName) {
        return null;
    }

    @Override
    public void setMapData(String pMapId, MapItemSavedData pData) {

    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return new RecipeManager();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return new LevelEntityGetterAdapter<>(
                new EntityLookup<>(),
                new EntitySectionStorage<>(Entity.class, l -> Visibility.TRACKED)
        );
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return new LevelTicks<>(
             l -> true,
            () -> InactiveProfiler.INSTANCE
        );
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return new LevelTicks<>(
                l -> true,
                () -> InactiveProfiler.INSTANCE
        );
    }

    @Override
    public ChunkSource getChunkSource() {
        return null;
    }

    @Override
    public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData) {

    }

    @Override
    public void gameEvent(GameEvent pEvent, Vec3 pPosition, GameEvent.Context pContext) {

    }

    @Override
    public RegistryAccess registryAccess() {
        return null;
    }

    @Override
    public float getShade(Direction pDirection, boolean pShade) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return null;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
        return null;
    }
}
