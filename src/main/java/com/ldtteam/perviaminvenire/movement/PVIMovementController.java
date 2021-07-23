package com.ldtteam.perviaminvenire.movement;

import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;


public class PVIMovementController extends MoveControl
{
    final AttributeInstance speedAtr;

    public PVIMovementController(Mob mob)
    {
        super(mob);
        this.speedAtr = this.mob.getAttribute(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void tick()
    {
        if (this.operation == net.minecraft.world.entity.ai.control.MoveControl.Operation.STRAFE)
        {
            final float speedAtt = (float) speedAtr.getValue();
            float speed = (float) this.speedModifier * speedAtt;
            float forward = this.strafeForwards;
            float strafe = this.strafeRight;
            float totalMovement = Mth.sqrt(forward * forward + strafe * strafe);
            if (totalMovement < 1.0F)
            {
                totalMovement = 1.0F;
            }

            totalMovement = speed / totalMovement;
            forward = forward * totalMovement;
            strafe = strafe * totalMovement;
            final float sinRotation = Mth.sin(this.mob.getYRot() * ((float) Math.PI / 180F));
            final float cosRotation = Mth.cos(this.mob.getYRot() * ((float) Math.PI / 180F));
            final float rot1 = forward * cosRotation - strafe * sinRotation;
            final float rot2 = strafe * cosRotation + forward * sinRotation;
            final PathNavigation pathnavigator = this.mob.getNavigation();

            final NodeEvaluator nodeprocessor = pathnavigator.getNodeEvaluator();
            if (nodeprocessor.getBlockPathType(this.mob.level,
              Mth.floor(this.mob.getX() + (double) rot1),
              Mth.floor(this.mob.getY()),
              Mth.floor(this.mob.getZ() + (double) rot2)) != BlockPathTypes.WALKABLE)
            {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
                speed = speedAtt;
            }

            this.mob.setSpeed(speed);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = net.minecraft.world.entity.ai.control.MoveControl.Operation.WAIT;
        }
        else if (this.operation == net.minecraft.world.entity.ai.control.MoveControl.Operation.MOVE_TO)
        {
            this.operation = net.minecraft.world.entity.ai.control.MoveControl.Operation.WAIT;
            final double xDif = this.wantedX - this.mob.getX();
            final double zDif = this.wantedZ - this.mob.getZ();
            final double yDif = this.wantedY - this.mob.getY();
            final double dist = xDif * xDif + yDif * yDif + zDif * zDif;
            if (dist < (double) 2.5000003E-7F)
            {
                this.mob.setZza(0.0F);
                return;
            }

            final float range = (float) (Mth.atan2(zDif, xDif) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), range, 90.0F));
            this.mob.setSpeed((float) (this.speedModifier * speedAtr.getValue()));
            final BlockPos blockpos = new BlockPos(this.mob.position());
            final BlockState blockstate = this.mob.level.getBlockState(blockpos);
            final Block block = blockstate.getBlock();
            final VoxelShape voxelshape = blockstate.getBlockSupportShape(this.mob.level, blockpos);
            if ((yDif > (double) this.mob.maxUpStep && xDif * xDif + zDif * zDif < (double) Math.max(1.0F, this.mob.getBbWidth()))
                  || (!voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(
              BlockTags.FENCES))
                       && !this.isLadder(blockstate, blockpos))
            {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        }
        else if (this.operation == net.minecraft.world.entity.ai.control.MoveControl.Operation.JUMPING)
        {
            this.mob.setSpeed((float) (this.speedModifier * speedAtr.getValue()));

            // Avoid beeing stuck in jumping while in liquids
            final BlockPos blockpos = new BlockPos(this.mob.position());
            final BlockState blockstate = this.mob.level.getBlockState(blockpos);
            if (this.mob.isOnGround() || blockstate.getMaterial().isLiquid())
            {
                this.operation = net.minecraft.world.entity.ai.control.MoveControl.Operation.WAIT;
            }
        }
        else
        {
            this.mob.setZza(0.0F);
        }
    }

    @Override
    public void setWantedPosition(final double x, final double y, final double z, final double speedIn)
    {
        super.setWantedPosition(x, y, z, speedIn);
        this.operation = Operation.MOVE_TO;
    }

    private boolean isLadder(final BlockState blockState, final BlockPos pos) {
        return IIsLadderBlockRegistry.getInstance()
                 .getRunner().isLadder(this.mob, blockState, this.mob.level, pos)
                 .orElseGet(() -> blockState.getBlock().isLadder(this.mob.level.getBlockState(pos), this.mob.level, pos, this.mob));
    }
}
