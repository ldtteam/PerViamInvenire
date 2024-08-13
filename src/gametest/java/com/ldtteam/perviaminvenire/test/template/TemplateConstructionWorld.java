package com.ldtteam.perviaminvenire.test.template;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.util.EntityTypeUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
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
import net.minecraft.world.level.saveddata.maps.MapId;
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

    private static RegistryAccess createRegistryAccess() {
        return EntityTypeUtils.createInstantiationRegistries(null);
    }

    private final Map<BlockPos, BlockState> blocks;
    private final Map<BlockPos, BlockEntity> blockEntities;

    TemplateConstructionWorld(Map<BlockPos, BlockState> blocks, Map<BlockPos, BlockEntity> blockEntities) {
        super(new ClientLevel.ClientLevelData(Difficulty.PEACEFUL, true, true),
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "pvi_template")),
                createRegistryAccess(),
                EntityTypeUtils.createDimensionType(),
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
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {

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

    @Override
    public TickRateManager tickRateManager() {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public void setMapData(MapId mapId, MapItemSavedData mapData) {

    }

    @Override
    public MapId getFreeMapId() {
        return null;
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
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {

    }

    @Override
    public @NotNull RegistryAccess registryAccess() {
        return null;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return null;
    }

    @Override
    public PotionBrewing potionBrewing() {
        return null;
    }

    @Override
    public void setDayTimeFraction(float v) {

    }

    @Override
    public float getDayTimeFraction() {
        return 0;
    }

    @Override
    public float getDayTimePerTick() {
        return 0;
    }

    @Override
    public void setDayTimePerTick(float v) {

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

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {

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
