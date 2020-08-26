package com.cable.library.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class TeleportUtils {

    public static void teleport(EntityPlayerMP player, World world, BlockPos pos) {
        int to = world.provider.getDimension();
        int from = player.dimension;

        if (to == from) {
            player.moveToBlockPosAndAngles(pos, player.rotationYaw, player.rotationPitch);
        } else {
            player.changeDimension(to, new SimpleTeleporter(pos));
        }

    }

    private static class SimpleTeleporter implements ITeleporter {

        private BlockPos pos;

        private SimpleTeleporter(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            entity.moveToBlockPosAndAngles(pos, yaw, entity.rotationPitch);
        }
    }
}
