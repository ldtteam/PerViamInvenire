package com.ldtteam.perviaminvenire.collisions;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

public class FilterableBlockCollisions extends AbstractIterator<VoxelShape> {
   private final AABB box;
   private final CollisionContext context;
   private final Cursor3D cursor;
   private final BlockPos.MutableBlockPos pos;
   private final VoxelShape entityShape;
   private final CollisionGetter collisionGetter;

   private final BiPredicate<BlockState, BlockPos> filter;
   @Nullable
   private BlockGetter cachedBlockGetter;
   private long cachedBlockGetterPos;

   public FilterableBlockCollisions(CollisionGetter collisionGetter, @Nullable Entity entity, AABB area, BiPredicate<BlockState, BlockPos> filter) {
      this.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
      this.pos = new BlockPos.MutableBlockPos();
      this.entityShape = Shapes.create(area);
      this.collisionGetter = collisionGetter;
      this.box = area;
      this.filter = filter;
      int minX = Mth.floor(area.minX - 1.0E-7D) - 1;
      int maxX = Mth.floor(area.maxX + 1.0E-7D) + 1;
      int minY = Mth.floor(area.minY - 1.0E-7D) - 1;
      int maxY = Mth.floor(area.maxY + 1.0E-7D) + 1;
      int minZ = Mth.floor(area.minZ - 1.0E-7D) - 1;
      int maxZ = Mth.floor(area.maxZ + 1.0E-7D) + 1;
      this.cursor = new Cursor3D(minX, minY, minZ, maxX, maxY, maxZ);
   }

   @Nullable
   private BlockGetter getChunk(int x, int y) {
      int chunkX = SectionPos.blockToSectionCoord(x);
      int chunkY = SectionPos.blockToSectionCoord(y);
      long chunkPosId = ChunkPos.asLong(chunkX, chunkY);
      if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == chunkPosId) {
         return this.cachedBlockGetter;
      } else {
         BlockGetter blockgetter = this.collisionGetter.getChunkForCollisions(chunkX, chunkY);
         this.cachedBlockGetter = blockgetter;
         this.cachedBlockGetterPos = chunkPosId;
         return blockgetter;
      }
   }

   protected VoxelShape computeNext() {
      while(true) {
         if (this.cursor.advance()) {
            int x = this.cursor.nextX();
            int y = this.cursor.nextY();
            int z = this.cursor.nextZ();
            int type = this.cursor.getNextType();
            if (type == 3) {
               continue;
            }

            BlockGetter blockgetter = this.getChunk(x, z);
            if (blockgetter == null) {
               continue;
            }

            this.pos.set(x, y, z);
            BlockState blockstate = blockgetter.getBlockState(this.pos);
            if (!this.filter.test(blockstate, this.pos.immutable()) || type == 1 && !blockstate.hasLargeCollisionShape() || type == 2 && !blockstate.is(Blocks.MOVING_PISTON)) {
               continue;
            }

            VoxelShape collisionShape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
            if (collisionShape == Shapes.block()) {
               if (!this.box.intersects(x, y, z, (double)x + 1.0D, (double)y + 1.0D, (double)z + 1.0D)) {
                  continue;
               }

               return collisionShape.move(x, y, z);
            }

            VoxelShape shiftedShape = collisionShape.move(x, y, z);
            if (!Shapes.joinIsNotEmpty(shiftedShape, this.entityShape, BooleanOp.AND)) {
               continue;
            }

            return shiftedShape;
         }

         return this.endOfData();
      }
   }
}