package com.ldtteam.perviaminvenire.util;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.data.worldgen.features.*;
import net.minecraft.data.worldgen.placement.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.flag.FeatureFlags.VANILLA_SET;

@SuppressWarnings("unchecked")
public class EntityTypeUtils {
    private static final String MINECRAFT_MOD_ID = "minecraft";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<EntityType<?>> VANILLA_BOSSES = Set.of(EntityType.ENDER_DRAGON, EntityType.WITHER);
    private static final Set<EntityType<?>> NOT_MOVING = Set.of(EntityType.SHULKER);
    private static final Set<EntityType<?>> NOT_SUPPORTED = Set.of(EntityType.GIANT);
    private EntityTypeUtils() {
        throw new IllegalStateException("Can not instantiate an instance of: DataGenUtils. This is a utility class");
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes(@Nullable HolderLookup.Provider provider) {
        return getCompatibleVanillaOverrideTypes(mob -> {
            return isMobEntityASupportedGroundEntity(mob) ||
                    isMobEntityASupportedSwimmingEntity(mob) ||
                    isMobEntityASupportedClimberEntity(mob) ||
                    isMobEntityASupportedFlyingEntity(mob);
        }, provider);
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes(final Predicate<Mob> filter, @Nullable HolderLookup.Provider provider) {
        return BuiltInRegistries.ENTITY_TYPE
                .stream()
                .filter(entityType -> !NOT_SUPPORTED.contains(entityType))
                .filter(entityType -> !VANILLA_BOSSES.contains(entityType))
                .filter(entityType -> !NOT_MOVING.contains(entityType))
                .filter(entityType -> MINECRAFT_MOD_ID.equals(Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)).getNamespace()))
                .filter(entityType -> {
                    try {
                        final Entity entity = createEntityType(entityType, provider);
                        if (!(entity instanceof final Mob mob))
                            return false;

                        return filter.test(mob);
                    } catch (Exception ex) {
                        LOGGER.error(String.format(
                                "Failed to create and validate entity, of type: '%s' for navigator replacement. Skipping!",
                                BuiltInRegistries.ENTITY_TYPE.getKey(entityType)), ex);
                        return false;
                    }
                }).toArray(EntityType<?>[]::new);
    }

    private static <T> Registry<T> createRegistry(ResourceKey<? extends Registry<T>> key, Consumer<BootstrapContext<T>> bootstrapContextConsumer, Registry<?>... registries) {
        return createRegistry(key, List.of(bootstrapContextConsumer), registries);
    }

    private static <T> Registry<T> createRegistry(ResourceKey<? extends Registry<T>> key, List<Consumer<BootstrapContext<T>>> bootstrapContextConsumer, Registry<?>... registries) {
        final MappedRegistry<T> registry = new MappedRegistry<>(key, Lifecycle.stable());
        final BootstrapContext<T> damageTypeBootstrapContext = new BootstrapContext<T>() {
            @Override
            public Holder.@NotNull Reference<T> register(@NotNull ResourceKey<T> resourceKey, @NotNull T damageType, @NotNull Lifecycle lifecycle) {
                return registry.register(resourceKey, damageType, new RegistrationInfo(Optional.empty(), lifecycle));
            }

            @Override
            public <S> @NotNull HolderGetter<S> lookup(@NotNull ResourceKey<? extends Registry<? extends S>> resourceKey) {
                if (resourceKey == key)
                    return new HolderGetter<>() {
                        @Override
                        public @NotNull Optional<Holder.Reference<S>> get(@NotNull ResourceKey<S> resourceKey) {
                            return registry.getHolder((ResourceKey<T>) resourceKey).map(holder -> (Holder.Reference<S>) holder);
                        }

                        @Override
                        public @NotNull Optional<HolderSet.Named<S>> get(@NotNull TagKey<S> tagKey) {
                            return registry.getTag((TagKey<T>) tagKey).map(tag -> (HolderSet.Named<S>) tag);
                        }
                    };

                return RegistryLayer.createRegistryAccess().replaceFrom(RegistryLayer.RELOADABLE, List.of(new RegistryAccess.ImmutableRegistryAccess(List.of(registries)).freeze()))
                        .compositeAccess()
                        .lookupOrThrow(resourceKey);

            }
        };
        try {
            bootstrapContextConsumer.forEach(consumer -> consumer.accept(damageTypeBootstrapContext));
        } catch (Exception ignored) {

        }
        return registry.freeze();
    }

    public static RegistryAccess createInstantiationRegistries(@Nullable HolderLookup.Provider provider) {
        LayeredRegistryAccess<RegistryLayer> registryAccess = RegistryLayer.createRegistryAccess();

        final Registry<DamageType> damageTypes = createRegistry(Registries.DAMAGE_TYPE, DamageTypes::bootstrap);
        final Registry<PaintingVariant> paintingVariants = createRegistry(Registries.PAINTING_VARIANT, PaintingVariants::bootstrap);
        final Registry<StructureProcessorList> structureProcessorLists = createRegistry(Registries.PROCESSOR_LIST, ProcessorLists::bootstrap);
        final Registry<ConfiguredFeature<?,?>> configuredFeatures = createRegistry(Registries.CONFIGURED_FEATURE,
                List.of(
                        AquaticFeatures::bootstrap,
                        CaveFeatures::bootstrap,
                        EndFeatures::bootstrap,
                        MiscOverworldFeatures::bootstrap,
                        NetherFeatures::bootstrap,
                        OreFeatures::bootstrap,
                        PileFeatures::bootstrap,
                        TreeFeatures::bootstrap,
                        VegetationFeatures::bootstrap
                ),
                structureProcessorLists);
        final Registry<PlacedFeature> placedFeatures = createRegistry(Registries.PLACED_FEATURE,
                List.of(AquaticPlacements::bootstrap,
                        CavePlacements::bootstrap,
                        EndPlacements::bootstrap,
                        MiscOverworldPlacements::bootstrap,
                        NetherPlacements::bootstrap,
                        OrePlacements::bootstrap,
                        TreePlacements::bootstrap,
                        VegetationPlacements::bootstrap,
                        VillagePlacements::bootstrap
                ),
                configuredFeatures);
        final Registry<Biome> biomes = createRegistry(Registries.BIOME, BiomeData::bootstrap, placedFeatures);
        final Registry<WolfVariant> wolfVariants = createRegistry(Registries.WOLF_VARIANT, WolfVariants::bootstrap, biomes);
        final RegistryAccess.ImmutableRegistryAccess lastLayer = new RegistryAccess.ImmutableRegistryAccess(List.of(damageTypes, paintingVariants, wolfVariants, biomes));

        registryAccess = registryAccess.replaceFrom(RegistryLayer.RELOADABLE, lastLayer.freeze());

        return registryAccess.compositeAccess();

/*
        final RegistryAccess.Frozen currentRegistries = registryAccess.compositeAccess();
        final RegistryBuilder builder = new RegistryBuilder(provider);
        final RegistryAccess.ImmutableRegistryAccess lastLayer = new RegistryAccess.ImmutableRegistryAccess(
                provider == null ?
                        Stream.of():
                provider.listRegistries()
                        .filter(registry -> currentRegistries.registry(registry).isEmpty())
                        .map(builder::createRegistry)
        );

        registryAccess = registryAccess.replaceFrom(RegistryLayer.RELOADABLE, lastLayer.freeze());

        return registryAccess.compositeAccess();*/
    }

    private record RegistryBuilder(HolderLookup.Provider provider) {
        public <T, R extends Registry<T>> RegistryAccess.RegistryEntry<T> createRegistryInternal(ResourceKey<R> key) {
                final MappedRegistry<T> registry = new MappedRegistry<>(key, Lifecycle.stable());
                final HolderLookup.RegistryLookup<T> lookup = provider.lookupOrThrow(key);

                lookup.listElements().forEach(
                        holder -> registry.register(Objects.requireNonNull(holder.getKey()), holder.value(), new RegistrationInfo(Optional.empty(), Lifecycle.stable()))
                );

                return new RegistryAccess.RegistryEntry<>(key, registry.freeze());
            }

        @SuppressWarnings("rawtypes")
        public RegistryAccess.RegistryEntry<?> createRegistry(ResourceKey resourceKey) {
            return createRegistryInternal(resourceKey);
        }
    }

    @NotNull
    private static Level createInstantiationWorld(@Nullable HolderLookup.Provider provider) {
        final RegistryAccess dynamicRegistries = createInstantiationRegistries(provider);
        final Holder<DimensionType> overworldDimension = createDimensionType();

        final WritableLevelData noopWritableLevelData = new WritableLevelData() {
            @Override
            public void setSpawn(@NotNull BlockPos blockPos, float v) {

            }

            @Override
            public BlockPos getSpawnPos() {
                return null;
            }

            @Override
            public float getSpawnAngle() {
                return 0;
            }

            @Override
            public long getGameTime() {
                return 0;
            }

            @Override
            public long getDayTime() {
                return 0;
            }

            @Override
            public boolean isThundering() {
                return false;
            }

            @Override
            public boolean isRaining() {
                return false;
            }

            @Override
            public void setRaining(boolean b) {

            }

            @Override
            public boolean isHardcore() {
                return false;
            }

            @Override
            public GameRules getGameRules() {
                return null;
            }

            @Override
            public Difficulty getDifficulty() {
                return null;
            }

            @Override
            public boolean isDifficultyLocked() {
                return false;
            }
        };

        @SuppressWarnings("ConstantConditions") //We are creating a dummy world here.
        final Level clientWorld = new Level(
                new ClientLevel.ClientLevelData(Difficulty.HARD, false, true),
                Level.OVERWORLD,
                dynamicRegistries,
                overworldDimension,
                () -> InactiveProfiler.INSTANCE,
                false,
                true,
                0,
                0
        ) {
            @Override
            public @NotNull List<? extends Player> players() {
                return List.of();
            }

            @Override
            public float getShade(@NotNull Direction direction, boolean b) {
                return 0;
            }

            @Override
            public LevelTickAccess<Block> getBlockTicks() {
                return null;
            }

            @Override
            public LevelTickAccess<Fluid> getFluidTicks() {
                return null;
            }

            @Override
            public ChunkSource getChunkSource() {
                return null;
            }

            @Override
            public void levelEvent(@Nullable Player player, int i, @NotNull BlockPos blockPos, int i1) {

            }

            @Override
            public void gameEvent(@NotNull Holder<GameEvent> holder, @NotNull Vec3 vec3, GameEvent.@NotNull Context context) {

            }

            @Override
            public void sendBlockUpdated(@NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull BlockState blockState1, int i) {

            }

            @Override
            public void playSeededSound(@Nullable Player player, double v, double v1, double v2, @NotNull Holder<SoundEvent> holder, @NotNull SoundSource soundSource, float v3, float v4, long l) {

            }

            @Override
            public void playSeededSound(@Nullable Player player, @NotNull Entity entity, @NotNull Holder<SoundEvent> holder, @NotNull SoundSource soundSource, float v, float v1, long l) {

            }

            @Override
            public @NotNull String gatherChunkSourceStats() {
                return "";
            }

            @Nullable
            @Override
            public Entity getEntity(int i) {
                return null;
            }

            @Override
            public TickRateManager tickRateManager() {
                return null;
            }

            @Nullable
            @Override
            public MapItemSavedData getMapData(@NotNull MapId mapId) {
                return null;
            }

            @Override
            public void setMapData(@NotNull MapId mapId, @NotNull MapItemSavedData mapItemSavedData) {

            }

            @Override
            public MapId getFreeMapId() {
                return null;
            }

            @Override
            public void destroyBlockProgress(int i, @NotNull BlockPos blockPos, int i1) {

            }

            @Override
            public Scoreboard getScoreboard() {
                return null;
            }

            @Override
            public RecipeManager getRecipeManager() {
                return null;
            }

            @Override
            protected LevelEntityGetter<Entity> getEntities() {
                return null;
            }

            @Override
            public Holder<Biome> getUncachedNoiseBiome(int i, int i1, int i2) {
                return null;
            }

            @Override
            public @NotNull RegistryAccess registryAccess() {
                return dynamicRegistries;
            }

            @Override
            public @NotNull FeatureFlagSet enabledFeatures() {
                return VANILLA_SET;
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
        };
        return clientWorld;
    }

    public static @NotNull Holder<DimensionType> createDimensionType() {
        final DimensionType type = new DimensionType(
                OptionalLong.of(1),
                false,
                false,
                false,
                false,
                1,
                false,
                false,
                0,
                16,
                1,
                BlockTags.AIR,
                ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                16f,
                new DimensionType.MonsterSettings(false, false, new IntProvider() {
                    @Override
                    public int sample(@NotNull RandomSource randomSource) {
                        return 0;
                    }

                    @Override
                    public int getMinValue() {
                        return 0;
                    }

                    @Override
                    public int getMaxValue() {
                        return 0;
                    }

                    @Override
                    public @NotNull IntProviderType<?> getType() {
                        return IntProviderType.CONSTANT;
                    }
                },
                        1)
        );
        final Holder<DimensionType> overworldDimension = new Holder.Direct<>(type);
        return overworldDimension;
    }

    @Nullable
    public static <V extends Entity> V createEntityType(EntityType<V> entityType, @Nullable HolderLookup.Provider provider) {
        return entityType.create(createInstantiationWorld(provider));
    }

    private static Class<?> getMovementControllerClass(final Mob mobEntity) {
        return mobEntity.getMoveControl().getClass();
    }

    public static boolean isMobEntityASupportedGroundEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == GroundPathNavigation.class &&
                getMovementControllerClass(mobEntity) == MoveControl.class
                && !(mobEntity instanceof WaterAnimal);

    }

    public static boolean isMobEntityASupportedSwimmingEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == GroundPathNavigation.class &&
                getMovementControllerClass(mobEntity) == MoveControl.class &&
                mobEntity instanceof WaterAnimal;

    }

    public static boolean isMobEntityASupportedClimberEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == WallClimberNavigation.class &&
                getMovementControllerClass(mobEntity) == MoveControl.class;

    }

    public static boolean isMobEntityASupportedFlyingEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == FlyingPathNavigation.class &&
                getMovementControllerClass(mobEntity) == FlyingMoveControl.class;

    }
}
