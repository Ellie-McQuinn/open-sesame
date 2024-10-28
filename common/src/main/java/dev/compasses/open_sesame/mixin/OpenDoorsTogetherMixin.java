package dev.compasses.open_sesame.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

@Mixin(DoorBlock.class)
public abstract class OpenDoorsTogetherMixin {
    @Inject(
            method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/ScheduledTickAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void openSesame$updateDoorsTogether(BlockState state, LevelReader level, ScheduledTickAccess tickScheduler, BlockPos pos, Direction offset, BlockPos facingPos, BlockState facingState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (!(facingState.getBlock() instanceof DoorBlock)) {
            return;
        }

        boolean ownIsWood = DoorBlock.isWoodenDoor(state);
        boolean facingIsWood = DoorBlock.isWoodenDoor(facingState);
        DoubleBlockHalf ownHalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        DoubleBlockHalf facingHalf = facingState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        boolean ownPowered = state.getValue(BlockStateProperties.POWERED);
        boolean facingPowered = facingState.getValue(BlockStateProperties.POWERED);
        DoorHingeSide ownHinge = state.getValue(BlockStateProperties.DOOR_HINGE);
        DoorHingeSide facingHinge = facingState.getValue(BlockStateProperties.DOOR_HINGE);
        boolean ownOpen = state.getValue(BlockStateProperties.OPEN);
        boolean facingOpen = facingState.getValue(BlockStateProperties.OPEN);

        if (facingHalf == ownHalf && facingHinge != ownHinge) { // if we've been updated by an opposite door
            if (!ownPowered) {
                if (facingPowered) {
                    cir.setReturnValue(cir.getReturnValue().setValue(BlockStateProperties.OPEN, facingOpen));
                } else if (facingOpen != ownOpen) {
                    if (!facingIsWood && ownIsWood || facingIsWood == ownIsWood) { // if the doors are the same, or we're wooden and the other is iron.
                        cir.setReturnValue(cir.getReturnValue().setValue(BlockStateProperties.OPEN, !ownOpen));
                    } else if (!ownIsWood && !ownPowered) {
                        cir.setReturnValue(cir.getReturnValue().setValue(BlockStateProperties.OPEN, false));
                    }
                }

            }
        }
    }

    @Inject(
            method = "onExplosionHit(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/DoorBlock;setOpen(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Z)V"
            ),
            cancellable = true
    )
    private void openSesame$jankDontOpenDoorOnWindCharge(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer, CallbackInfo ci) {
        DoorHingeSide doorHinge = state.getValue(BlockStateProperties.DOOR_HINGE);
        boolean hasUpdatingNeighbour = Stream.of(pos.east(), pos.west(), pos.north(), pos.south())
                .map(level::getBlockState)
                .filter(DoorBlock::isWoodenDoor)
                .anyMatch(neighbour -> doorHinge != neighbour.getValue(BlockStateProperties.DOOR_HINGE));

        if (hasUpdatingNeighbour && doorHinge == DoorHingeSide.LEFT) {
            ci.cancel();
        }
    }
}
