package com.cable.library.book

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.play.server.SPacketSetSlot
import net.minecraft.util.EnumHand
import net.minecraft.util.text.ITextComponent

fun of(vararg pages: ITextComponent): Book {
    return book(*pages)
}

fun book(vararg pages: ITextComponent): Book {
    val book = ItemStack(Items.WRITTEN_BOOK, 1)
    val nbt = NBTTagCompound()
    val pagesNBT = NBTTagList()
    for (page in pages) {
        pagesNBT.appendTag(NBTTagString(ITextComponent.Serializer.componentToJson(page)))
    }
    nbt.setTag("pages", pagesNBT)
    nbt.setTag("author", NBTTagString("Cable"))
    nbt.setTag("title", NBTTagString("Book"))
    book.tagCompound = nbt
    return Book(book)
}

fun sendBook(player: EntityPlayerMP, vararg pages: ITextComponent) = of(*pages).open(player)

class Book(val itemStack: ItemStack) {
    fun open(player: EntityPlayerMP) {
        val slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem)!!;
        player.connection.sendPacket(SPacketSetSlot(0, slot.slotNumber, itemStack))
        player.openBook(itemStack, EnumHand.MAIN_HAND)
        player.connection.sendPacket(SPacketSetSlot(0, slot.slotNumber, player.getHeldItem(EnumHand.MAIN_HAND)))
    }
}