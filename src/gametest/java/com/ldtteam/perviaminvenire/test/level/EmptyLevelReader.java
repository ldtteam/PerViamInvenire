package com.ldtteam.perviaminvenire.test.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("NullableProblems")
public enum EmptyLevelReader implements LevelReader {
    INSTANCE;

    @Nullable
    @Override
    public ChunkAccess getChunk(int p_46823_, int p_46824_, @NotNull ChunkStatus p_46825_, boolean p_46826_) {
        return null;
    }

    @Override
    public boolean hasChunk(int p_46838_, int p_46839_) {
        return false;
    }

    @Override
    public int getHeight(Heightmap.@NotNull Types p_46827_, int p_46828_, int p_46829_) {
        return 0;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public @NotNull BiomeManager getBiomeManager() {
        return new BiomeManager((p_204218_, p_204219_, p_204220_) -> BuiltinRegistries.BIOME.getHolderOrThrow(Biomes.PLAINS), 0);
    }

    @Override
    public @NotNull Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        return BuiltinRegistries.BIOME.getHolderOrThrow(Biomes.PLAINS);
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        return 64;
    }

    @Override
    public @NotNull DimensionType dimensionType() {
        return Objects.requireNonNull(BuiltinRegistries.DIMENSION_TYPE.get(BuiltinDimensionTypes.OVERWORLD));
    }

    @Override
    public float getShade(@NotNull Direction p_45522_, boolean p_45523_) {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return null;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return new WorldBorder();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity p_186427_, @NotNull AABB p_186428_) {
        return List.of();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos p_45570_) {
        return null;
    }

    @Override
    public BlockState getBlockState(@NotNull BlockPos p_45571_) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(@NotNull BlockPos p_45569_) {
        return Fluids.EMPTY.defaultFluidState();
    }
}
