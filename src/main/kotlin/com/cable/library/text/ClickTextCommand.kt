package com.cable.library.text

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import java.util.*

public class ClickTextCommand : CommandBase() {
    override fun getName(): String = "clicktext"

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean = sender is EntityPlayerMP

    override fun getUsage(sender: ICommandSender): String = ""

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>): Unit = handlers[UUID.fromString(args[0])]?.accept(sender as EntityPlayerMP)?: Unit
}
