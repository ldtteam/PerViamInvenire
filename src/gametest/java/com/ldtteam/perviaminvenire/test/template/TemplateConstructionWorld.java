package com.ldtteam.perviaminvenire.test.template;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
final class TemplateConstructionWorld extends Level {

    private final Map<BlockPos, BlockState> blocks;
    private final Map<BlockPos, BlockEntity> blockEntities;

    TemplateConstructionWorld(Map<BlockPos, BlockState> blocks, Map<BlockPos, BlockEntity> blockEntities) {
        super(new ClientLevel.ClientLevelData(Difficulty.PEACEFUL, true, true),
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(ModConstants.MOD_ID, "pvi_template")),
                BuiltinRegistries.DIMENSION_TYPE.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> null,
                false,
                false,
                0,
                0);

        this.blocks = blocks;
        this.blockEntities = blockEntities;
    }

    @Override
    public void sendBlockUpdated(@NotNull BlockPos p_46612_, @NotNull BlockState p_46613_, @NotNull BlockState p_46614_, int p_46615_) {

    }

    @Override
    public void playSeededSound(@Nullable Player p_220363_, double p_220364_, double p_220365_, double p_220366_, @NotNull SoundEvent p_220367_, @NotNull SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {

    }

    @Override
    public void playSeededSound(@Nullable Player p_220372_, @NotNull Entity p_220373_, @NotNull SoundEvent p_220374_, @NotNull SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {

    }

    @Override
    public @NotNull String gatherChunkSourceStats() {
        return null;
    }

    @Nullable
    @Override
    public Entity getEntity(int p_46492_) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(@NotNull String p_46650_) {
        return null;
    }

    @Override
    public void setMapData(@NotNull String p_151533_, @NotNull MapItemSavedData p_151534_) {

    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int p_46506_, @NotNull BlockPos p_46507_, int p_46508_) {

    }

    @Override
    public @NotNull Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public @NotNull RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    protected @NotNull LevelEntityGetter<Entity> getEntities() {
        return null;
    }

    @Override
    public @NotNull LevelTickAccess<Block> getBlockTicks() {
        return null;
    }

    @Override
    public @NotNull LevelTickAccess<Fluid> getFluidTicks() {
        return null;
    }

    @Override
    public @NotNull ChunkSource getChunkSource() {
        return null;
    }

    @Override
    public void levelEvent(@Nullable Player p_46771_, int p_46772_, @NotNull BlockPos p_46773_, int p_46774_) {

    }

    @Override
    public void gameEvent(@NotNull GameEvent p_220404_, @NotNull Vec3 p_220405_, GameEvent.@NotNull Context p_220406_) {

    }

    @Override
    public @NotNull RegistryAccess registryAccess() {
        return null;
    }

    @Override
    public float getShade(@NotNull Direction p_45522_, boolean p_45523_) {
        return 0;
    }

    @Override
    public @NotNull List<? extends Player> players() {
        return null;
    }

    @Override
    public @NotNull Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        return null;
    }

    @Override
    public void close() throws IOException {
        //NOOP
    }

    @Override
    public @NotNull BlockState getBlockState(@NotNull BlockPos position) {
        return this.blocks.getOrDefault(position, Blocks.AIR.defaultBlockState());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos position) {
        return this.blockEntities.get(position);
    }

    @Override
    public <T extends Entity> @NotNull List<T> getEntitiesOfClass(@NotNull Class<T> entityClass, @NotNull AABB box, @NotNull Predicate<? super T> predicate) {
        return List.of();
    }
}
