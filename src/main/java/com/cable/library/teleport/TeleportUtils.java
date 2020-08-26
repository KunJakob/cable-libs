package com.cable.library.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class TeleportUtils {
    public static void teleport(EntityPlayerMP player, World world, BlockPos pos) {
        teleport(player, world, pos, player.rotationYaw, player.rotationPitch);
    }

    public static void teleport(EntityPlayerMP player, World world, BlockPos pos, float yaw, float pitch) {
        int to = world.provider.getDimension();
        teleport(player, to, pos, yaw, pitch);
    }

    public static void teleport(EntityPlayerMP player, int dimension, BlockPos pos) {
        teleport(player, dimension, pos, player.rotationYaw, player.rotationPitch);
    }

    public static void teleport(EntityPlayerMP player, int dimension, BlockPos pos, float yaw, float pitch) {
        int from = player.getServerWorld().provider.getDimension();

        if (dimension == from) {
            player.moveToBlockPosAndAngles(pos, yaw, pitch);
        } else {
            player.changeDimension(dimension, new SimpleTeleporter(pos, yaw, pitch));
        }
    }

    private static class SimpleTeleporter implements ITeleporter {

        private BlockPos pos;
        private float yaw;
        private float pitch;

        private SimpleTeleporter(BlockPos pos, float yaw, float pitch) {
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            entity.moveToBlockPosAndAngles(this.pos, this.yaw, this.pitch);
        }
    }
}
