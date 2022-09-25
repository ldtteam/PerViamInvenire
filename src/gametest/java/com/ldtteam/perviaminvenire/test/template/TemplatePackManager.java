package com.ldtteam.perviaminvenire.test.template;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.core.BlockPos;
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

    private static final int SIMPLE_WALK_PATH_LENGTH = 12;
    private static final TemplatePackManager INSTANCE = new TemplatePackManager();

    public static TemplatePackManager getInstance() {
        return INSTANCE;
    }

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

    public void createSimpleWalkTemplateFor(final ResourceLocation name, final Block baseWalkBlock, final Block airBlock, final int width, final int height) {
        final Map<BlockPos, Block> blocks = new HashMap<>();

        //Creates the end caps on both ends along the X axis.
        for (int z = -1 ; z < width + 1; z++) {
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(0, y, z), Blocks.GLASS);
                blocks.put(new BlockPos(SIMPLE_WALK_PATH_LENGTH, y, z), Blocks.GLASS);
            }
        }

        //For each slice along X between the end caps:
        for (int x = 1; x < SIMPLE_WALK_PATH_LENGTH; x++) {
            //Create walls upwards.
            for (int y = 0; y < height + 2; y++) {
                blocks.put(new BlockPos(x, y, -1), Blocks.GLASS);
                blocks.put(new BlockPos(x, y, width+1), Blocks.GLASS);
            }

            //Create a floor and a ceiling
            for (int z = -1; z < width + 1; z++) {
                blocks.put(new BlockPos(x, 0, z), baseWalkBlock);
                blocks.put(new BlockPos(x, height + 1, z), Blocks.GLASS);
            }

            //Fill in the content, but only if it is not air.
            if (!airBlock.defaultBlockState().isAir()) {
                for (int y = 1; y < height + 1; y++) {
                    for (int z = 0; z < width; z++) {
                        blocks.put(new BlockPos(x, y, z), airBlock);
                    }
                }
            }
        }

        final Map<BlockPos, BlockEntity> blockEntities = Map.of();

        createTemplateFor(
                name,
                blocks,
                blockEntities,
                new BlockPos(0,0,-1),
                new BlockPos(SIMPLE_WALK_PATH_LENGTH, height + 1, width + 1)
        );
    }

    private void createTemplateFor(ResourceLocation name, Map<BlockPos, Block> blocks, Map<BlockPos, BlockEntity> blockEntities, BlockPos startPosition, BlockPos endPosition) {
        final Path outputPath = templateResourceFileSystems.getPath( "/","data", name.getNamespace(), "structures", name.getPath() + ".nbt");
        final Path parentDirectory = outputPath.getParent();
        try {
            Files.createDirectories(parentDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create in-memory directories!", e);
        }
        try (final TemplateConstructionWorld level = new TemplateConstructionWorld(blocks, blockEntities);
             final OutputStream outputStream = Files.newOutputStream(outputPath, StandardOpenOption.CREATE_NEW);
             final DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            final StructureTemplate template = new StructureTemplate();
            template.setAuthor("PVI");

            final Vec3i size = endPosition.offset(1,1,1).subtract(startPosition);
            template.fillFromWorld(level, startPosition, size, false, null);

            final CompoundTag tag = new CompoundTag();
            template.save(tag);

            NbtIo.writeCompressed(tag, dataOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create template construction level.", e);
        }
    }
}
