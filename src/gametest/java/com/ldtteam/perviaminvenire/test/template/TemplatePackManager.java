package com.ldtteam.perviaminvenire.test.template;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class TemplatePackManager implements RepositorySource {

    public static final int SIMPLE_WALK_PATH_LENGTH = 12;
    private static final TemplatePackManager INSTANCE = new TemplatePackManager();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private final FileSystem templateResourceFileSystems = Jimfs.newFileSystem("pvi-templates", Configuration.unix());

    private TemplatePackManager() {
        final Path rootPath = templateResourceFileSystems.getPath("/");
        try {
            for (PackType packType : PackType.values()) {
                final Path packPath = rootPath.resolve(packType.getDirectory());
                final Path pviPackPath = packPath.resolve(ModConstants.MOD_ID);
                Files.createDirectories(pviPackPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create in memory Pack FS", e);
        }
    }

    public static TemplatePackManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void loadPacks(@NotNull Consumer<Pack> packConsumer, @NotNull Pack.PackConstructor packConstructor) {
        packConsumer.accept(
                packConstructor.create(
                        "pvi_templates",
                        Component.literal("PVI GameTest Templates"),
                        true,
                        this::buildPackResources,
                        new PackMetadataSection(Component.literal("Contains the PVI GameTest Templates."), 0),
                        Pack.Position.TOP,
                        PackSource.BUILT_IN,
                        true
                )
        );
    }

    private PackResources buildPackResources() {
        final Path rootTemplateResourcesPath = templateResourceFileSystems.getPath("/");
        return new PathPackResources("pvi_templates", rootTemplateResourcesPath);
    }

    public void createWadeThroughTemplateFor(final ResourceLocation name, final Block baseWalkBlock, final BlockState wadeState, final boolean startOnSolid, final int width, final int height) {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        //Creates the end caps on both ends along the X axis.
        for (int z = -1; z < width + 1; z++) {
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(0, y, z), GLASS);
                blocks.put(new BlockPos(SIMPLE_WALK_PATH_LENGTH, y, z), GLASS);
            }
        }

        //For each slice along X between the end caps:
        for (int x = 1; x < SIMPLE_WALK_PATH_LENGTH; x++) {
            //Create walls upwards.
            for (int y = 0; y < height + 3; y++) {
                blocks.put(new BlockPos(x, y, -1), GLASS);
                blocks.put(new BlockPos(x, y, width), GLASS);
            }

            //Create a floor and a ceiling
            for (int z = -1; z < width + 1; z++) {
                blocks.put(new BlockPos(x, 0, z), baseWalkBlock.defaultBlockState());
                blocks.put(new BlockPos(x, height + 2, z), Blocks.GLOWSTONE.defaultBlockState());
            }

            final int y = 1;
            for (int z = 0; z < width; z++) {
                if (startOnSolid & x <= width) {
                    blocks.put(new BlockPos(x, y, z), baseWalkBlock.defaultBlockState());
                } else if (SIMPLE_WALK_PATH_LENGTH - x <= width) {
                    blocks.put(new BlockPos(x, y, z), baseWalkBlock.defaultBlockState());
                } else {
                    blocks.put(new BlockPos(x, y, z), wadeState);
                }
            }
        }

        final Map<BlockPos, BlockEntity> blockEntities = Map.of();

        createTemplateFor(
                name,
                blocks,
                blockEntities,
                new BlockPos(0, 0, -1),
                new BlockPos(SIMPLE_WALK_PATH_LENGTH, height + 2, width)
        );
    }

    public void createDeepWadeThroughTemplateFor(final ResourceLocation name, final Block baseWalkBlock, final BlockState wadeState, final boolean startOnSolid, final int width, final int entityHeight, final int weightDepth) {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        final int height = entityHeight + weightDepth;

        //Creates the end caps on both ends along the X axis.
        for (int z = -1; z < width + 1; z++) {
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(0, y, z), GLASS);
                blocks.put(new BlockPos(SIMPLE_WALK_PATH_LENGTH, y, z), GLASS);
            }
        }

        //For each slice along X between the end caps:
        for (int x = 1; x < SIMPLE_WALK_PATH_LENGTH; x++) {
            //Create walls upwards.
            for (int y = 0; y < height + 3; y++) {
                blocks.put(new BlockPos(x, y, -1), GLASS);
                blocks.put(new BlockPos(x, y, width), GLASS);
            }

            //Create a floor and a ceiling
            for (int z = -1; z < width + 1; z++) {
                blocks.put(new BlockPos(x, 0, z), baseWalkBlock.defaultBlockState());
                blocks.put(new BlockPos(x, height + 2, z), Blocks.GLOWSTONE.defaultBlockState());
            }


            //Fill in the content
            for (int y = 1; y < height + 2; y++) {
                if ((startOnSolid && x == 1) || x == (SIMPLE_WALK_PATH_LENGTH - 1)) {
                    for (int z = 0; z < width; z++) {
                        if (y <= (height - entityHeight)) {
                            blocks.put(new BlockPos(x, y, z), baseWalkBlock.defaultBlockState());
                        }
                    }
                }
                else {
                    for (int z = 0; z < width; z++) {
                        if (y <= (height - entityHeight)) {
                            blocks.put(new BlockPos(x, y, z), wadeState);
                        }
                    }
                }
            }
        }

        final Map<BlockPos, BlockEntity> blockEntities = Map.of();

        createTemplateFor(
                name,
                blocks,
                blockEntities,
                new BlockPos(0, 0, -1),
                new BlockPos(SIMPLE_WALK_PATH_LENGTH, height + 2, width)
        );
    }

    public void createWalkThroughTemplateFor(final ResourceLocation name, final Block baseWalkBlock, final Block airBlock, final BlockState groundCollisionBlock, final BlockState noneGroundCollisionBlock, final BlockState soilBlock, final int width, final int height) {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        //Creates the end caps on both ends along the X axis.
        for (int z = -1; z < width + 1; z++) {
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(0, y, z), GLASS);
                blocks.put(new BlockPos(SIMPLE_WALK_PATH_LENGTH, y, z), GLASS);
            }
        }

        //For each slice along X between the end caps:
        for (int x = 1; x < SIMPLE_WALK_PATH_LENGTH; x++) {
            //Create walls upwards.
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(x, y, -1), GLASS);
                blocks.put(new BlockPos(x, y, width), GLASS);
            }

            //Create a floor and a ceiling
            for (int z = -1; z < width + 1; z++) {
                blocks.put(new BlockPos(x, 0, z), baseWalkBlock.defaultBlockState());
                blocks.put(new BlockPos(x, height + 1, z), GLASS);
            }

            //Fill in the content
            for (int y = 1; y < height + 1; y++) {
                for (int z = 0; z < width; z++) {
                    if (x != (SIMPLE_WALK_PATH_LENGTH / 2)) {
                        blocks.put(new BlockPos(x, y, z), airBlock.defaultBlockState());
                    } else {
                        if (y == 1) {
                            blocks.put(new BlockPos(x, y, z), groundCollisionBlock);
                            blocks.put(new BlockPos(x, 0, z), soilBlock);
                        } else {
                            blocks.put(new BlockPos(x, y, z), noneGroundCollisionBlock);
                        }
                    }
                }
            }
        }

        final Map<BlockPos, BlockEntity> blockEntities = Map.of();

        createTemplateFor(
                name,
                blocks,
                blockEntities,
                new BlockPos(0, 0, -1),
                new BlockPos(SIMPLE_WALK_PATH_LENGTH, height + 1, width)
        );
    }

    public void createVerticalOffsetTemplateFor(final ResourceLocation name, final Block baseWalkBlock, final Block airBlock, final Direction offSetDirection, final int stepLength, final int stepCount, final BlockState stair, final int width, final int height) {
        if (offSetDirection.getAxis() != Direction.Axis.Y) {
            throw new IllegalArgumentException("The offset direction must be vertical");
        }

        final int totalHeight = height + stepCount;
        final int totalLength = stepLength * stepCount + SIMPLE_WALK_PATH_LENGTH;

        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        //Creates the end caps on both ends along the X axis.
        for (int z = -1; z < width + 2; z++) {
            for (int y = 0; y < totalHeight + 2; y++) {
                blocks.put(new BlockPos(0, y, z), GLASS);
                blocks.put(new BlockPos(totalLength, y, z), GLASS);
            }
        }

        //For each slice along X between the end caps:
        for (int x = 1; x < totalLength; x++) {
            //Create walls upwards.
            for (int y = 0; y < totalHeight + 2; y++) {
                blocks.put(new BlockPos(x, y, -1), GLASS);
                blocks.put(new BlockPos(x, y, width), GLASS);
            }

            //Create a floor and a ceiling
            for (int z = -1; z < width + 1; z++) {
                blocks.put(new BlockPos(x, 0, z), baseWalkBlock.defaultBlockState());
                blocks.put(new BlockPos(x, totalHeight + 1, z), GLASS);
            }

            //Fill in the content
            for (int y = 1; y < totalHeight + 1; y++) {
                for (int z = 0; z < width; z++) {
                    blocks.put(new BlockPos(x, y, z), airBlock.defaultBlockState());
                }
            }
        }

        final int xOffSet = SIMPLE_WALK_PATH_LENGTH / 2;
        if (offSetDirection == Direction.UP) {
            //Now create the steps
            for (int y = 1; y <= stepCount; y++) {
                for (int z = 0; z < width; z++) {
                    final int currentStepXOffset = xOffSet + (y - 1) * stepLength;
                    for (int x = currentStepXOffset; x < totalLength; x++) {
                        blocks.put(new BlockPos(x, y, z), baseWalkBlock.defaultBlockState());
                    }
                    blocks.put(new BlockPos(currentStepXOffset - 1, y, z), stair);
                }
            }
        } else {
            //Now create the steps
            for (int y = 1; y <= stepCount; y++) {
                for (int z = 0; z < width; z++) {
                    final int currentStepXOffset = xOffSet + (stepCount - y - 1) * stepLength;
                    for (int x = 1; x <= currentStepXOffset; x++) {
                        blocks.put(new BlockPos(x, y, z), baseWalkBlock.defaultBlockState());
                    }
                    blocks.put(new BlockPos(currentStepXOffset + 1, y, z), stair);
                }
            }
        }

        final Map<BlockPos, BlockEntity> blockEntities = Map.of();

        createTemplateFor(
                name,
                blocks,
                blockEntities,
                new BlockPos(0, 0, -1),
                new BlockPos(totalLength, totalHeight + 1, width)
        );
    }


    public void createSimpleWalkTemplateFor(final ResourceLocation name, final Block baseWalkBlock, final Block airBlock, final int width, final int height) {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        //Creates the end caps on both ends along the X axis.
        for (int z = -1; z < width + 1; z++) {
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(0, y, z), GLASS);
                blocks.put(new BlockPos(SIMPLE_WALK_PATH_LENGTH, y, z), GLASS);
            }
        }

        //For each slice along X between the end caps:
        for (int x = 1; x < SIMPLE_WALK_PATH_LENGTH; x++) {
            //Create walls upwards.
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(x, y, -1), GLASS);
                blocks.put(new BlockPos(x, y, width), GLASS);
            }

            //Create a floor and a ceiling
            for (int z = -1; z < width + 1; z++) {
                blocks.put(new BlockPos(x, 0, z), baseWalkBlock.defaultBlockState());
                blocks.put(new BlockPos(x, height + 1, z), GLASS);
            }

            //Fill in the content, but only if it is not air.
            if (!airBlock.defaultBlockState().isAir()) {
                for (int y = 1; y < height + 1; y++) {
                    for (int z = 0; z < width; z++) {
                        blocks.put(new BlockPos(x, y, z), airBlock.defaultBlockState());
                    }
                }
            }
        }

        final Map<BlockPos, BlockEntity> blockEntities = Map.of();

        createTemplateFor(
                name,
                blocks,
                blockEntities,
                new BlockPos(0, 0, -1),
                new BlockPos(SIMPLE_WALK_PATH_LENGTH, height + 1, width)
        );
    }

    private void createTemplateFor(ResourceLocation name, Map<BlockPos, BlockState> blocks, Map<BlockPos, BlockEntity> blockEntities, BlockPos startPosition, BlockPos endPosition) {
        final Path outputPath = templateResourceFileSystems.getPath("/", "data", name.getNamespace(), "structures", name.getPath() + ".nbt");
        final Path parentDirectory = outputPath.getParent();
        try {
            Files.createDirectories(parentDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create in-memory directories!", e);
        }
        if (Files.exists(outputPath)) {
            return;
        }

        try (final TemplateConstructionWorld level = new TemplateConstructionWorld(blocks, blockEntities);
             final OutputStream outputStream = Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW);
             final DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            final StructureTemplate template = new StructureTemplate();
            template.setAuthor("PVI");

            final Vec3i size = endPosition.offset(1, 1, 1).subtract(startPosition);
            template.fillFromWorld(level, startPosition, size, false, null);

            final CompoundTag tag = new CompoundTag();
            template.save(tag);

            NbtIo.writeCompressed(tag, dataOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create template construction level.", e);
        }
    }
}
