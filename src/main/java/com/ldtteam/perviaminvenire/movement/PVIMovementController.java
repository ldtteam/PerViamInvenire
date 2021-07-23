package com.ldtteam.perviaminvenire.movement;

import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;

import net.minecraft.entity.ai.controller.MovementController.Action;

public class PVIMovementController extends MovementController
{
    final ModifiableAttributeInstance speedAtr;

    public PVIMovementController(MobEntity mob)
    {
        super(mob);
        this.speedAtr = this.mob.getAttribute(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void tick()
    {
        if (this.operation == net.minecraft.entity.ai.controller.MovementController.Action.STRAFE)
        {
            final float speedAtt = (float) speedAtr.getValue();
            float speed = (float) this.speedModifier * speedAtt;
            float forward = this.strafeForwards;
            float strafe = this.strafeRight;
            float totalMovement = MathHelper.sqrt(forward * forward + strafe * strafe);
            if (totalMovement < 1.0F)
            {
                totalMovement = 1.0F;
            }

            totalMovement = speed / totalMovement;
            forward = forward * totalMovement;
            strafe = strafe * totalMovement;
            final float sinRotation = MathHelper.sin(this.mob.yRot * ((float) Math.PI / 180F));
            final float cosRotation = MathHelper.cos(this.mob.yRot * ((float) Math.PI / 180F));
            final float rot1 = forward * cosRotation - strafe * sinRotation;
            final float rot2 = strafe * cosRotation + forward * sinRotation;
            final PathNavigator pathnavigator = this.mob.getNavigation();

            final NodeProcessor nodeprocessor = pathnavigator.getNodeEvaluator();
            if (nodeprocessor.getBlockPathType(this.mob.level,
              MathHelper.floor(this.mob.getX() + (double) rot1),
              MathHelper.floor(this.mob.getY()),
              MathHelper.floor(this.mob.getZ() + (double) rot2)) != PathNodeType.WALKABLE)
            {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
                speed = speedAtt;
            }

            this.mob.setSpeed(speed);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = net.minecraft.entity.ai.controller.MovementController.Action.WAIT;
        }
        else if (this.operation == net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO)
        {
            this.operation = net.minecraft.entity.ai.controller.MovementController.Action.WAIT;
            final double xDif = this.wantedX - this.mob.getX();
            final double zDif = this.wantedZ - this.mob.getZ();
            final double yDif = this.wantedY - this.mob.getY();
            final double dist = xDif * xDif + yDif * yDif + zDif * zDif;
            if (dist < (double) 2.5000003E-7F)
            {
                this.mob.setZza(0.0F);
                return;
            }

            final float range = (float) (MathHelper.atan2(zDif, xDif) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.mob.yRot = this.rotlerp(this.mob.yRot, range, 90.0F);
            this.mob.setSpeed((float) (this.speedModifier * speedAtr.getValue()));
            final BlockPos blockpos = new BlockPos(this.mob.position());
            final BlockState blockstate = this.mob.level.getBlockState(blockpos);
            final Block block = blockstate.getBlock();
            final VoxelShape voxelshape = blockstate.getBlockSupportShape(this.mob.level, blockpos);
            if ((yDif > (double) this.mob.maxUpStep && xDif * xDif + zDif * zDif < (double) Math.max(1.0F, this.mob.getBbWidth()))
                  || (!voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double) blockpos.getY() && !block.is(BlockTags.DOORS) && !block.is(
              BlockTags.FENCES))
                       && !this.isLadder(blockstate, blockpos))
            {
                this.mob.getJumpControl().jump();
                this.operation = Action.JUMPING;
            }
        }
        else if (this.operation == net.minecraft.entity.ai.controller.MovementController.Action.JUMPING)
        {
            this.mob.setSpeed((float) (this.speedModifier * speedAtr.getValue()));

            // Avoid beeing stuck in jumping while in liquids
            final BlockPos blockpos = new BlockPos(this.mob.position());
            final BlockState blockstate = this.mob.level.getBlockState(blockpos);
            if (this.mob.isOnGround() || blockstate.getMaterial().isLiquid())
            {
                this.operation = net.minecraft.entity.ai.controller.MovementController.Action.WAIT;
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
        this.operation = Action.MOVE_TO;
    }

    private boolean isLadder(final BlockState blockState, final BlockPos pos) {
        return IIsLadderBlockRegistry.getInstance()
                 .getRunner().isLadder(this.mob, blockState, this.mob.level, pos)
                 .orElseGet(() -> blockState.getBlock().isLadder(this.mob.level.getBlockState(pos), this.mob.level, pos, this.mob));
    }
}
