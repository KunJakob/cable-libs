package com.cable.library.text

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.HoverEvent
import java.util.*
import java.util.function.Consumer

class Text internal constructor() {

    companion object {
        internal fun resolveComponent(text: Any): ITextComponent {
            return TextComponentString(text.toString().replace("&[A-Fa-f0-9k-oK-oRr]".toRegex()) { "§${it.value.substring(1)}" })
        }
    }

    private var style = Style()
    private var head: ITextComponent? = null

    fun parse(vararg components: Any): ITextComponent {
        components.forEach {
            when (it) {
                is ITextComponent -> {
                    addComponent(it)
                    //it.style.setParentStyle(style)
                    style = getBlankStyle()
                }
                is ClickEvent -> style.setClickEvent(it)
                is HoverEvent -> style.setHoverEvent(it)
                is TextFormatting -> {
                    when {
                        it.isColor -> style.color = it
                        it == TextFormatting.UNDERLINE || it == UNDERLINED -> style.underlined = true
                        it == TextFormatting.BOLD || it == BOLD -> style.bold = true
                        it == TextFormatting.ITALIC || it == ITALIC -> style.italic = true
                        it == TextFormatting.OBFUSCATED || it == OBFUSCATED -> style.obfuscated = true
                        it == TextFormatting.RESET || it == RESET -> style = Style()
                    }
                }
                else -> addComponent(resolveComponent(it).also {it.style.setParentStyle(style)})
            }
        }

        return head?: TextComponentString("Empty!")
    }

    private fun addComponent(component: ITextComponent) {
        if (head == null) {
            head = component
            component.style.assimilate(style)
            style = getBlankStyle()
            // Removed because of(hover(), blue()) would remove the hover
//            component.style.setParentStyle(style.createDeepCopy())
//            style = getBlankStyle()
        } else {
            head?.appendSibling(component.also{it.style.assimilate(style)})
        }
    }

    private fun getBlankStyle() = Style().setBold(false).setItalic(false).setUnderlined(false).setObfuscated(false).setColor(TextFormatting.WHITE).setClickEvent(null).setHoverEvent(null)
}

fun of(vararg components: Any) = Text().parse(*components)

val handlers = hashMapOf<UUID, Consumer<EntityPlayerMP>>()

fun click(action: Consumer<EntityPlayerMP>): ClickEvent {
    val uuid = UUID.randomUUID()
    handlers[uuid] = action
    return ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clicktext $uuid")
}

fun click(action: (p: EntityPlayerMP) -> Any) = click(Consumer { action.invoke(it) })

fun hover(text: ITextComponent) = HoverEvent(HoverEvent.Action.SHOW_TEXT, text)

fun hover(text: String) = hover(Text.resolveComponent(text))

fun hover(item: ItemStack) = HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponentString(item.writeToNBT(NBTTagCompound()).toString()))

fun hover(entity: EntityLivingBase) = HoverEvent(HoverEvent.Action.SHOW_ENTITY, TextComponentString(entity.writeToNBT(NBTTagCompound()).toString()))

val BOLD = Object()
val ITALIC = Object()
val UNDERLINED = Object()
val OBFUSCATED = Object()
val RESET = Object()

fun String.red() = TextComponentString(this).also { it.style.color = TextFormatting.RED }
fun String.black() = TextComponentString(this).also { it.style.color = TextFormatting.BLACK }
fun String.darkBlue() = TextComponentString(this).also { it.style.color = TextFormatting.DARK_BLUE }
fun String.darkGreen() = TextComponentString(this).also { it.style.color = TextFormatting.DARK_GREEN }
fun String.darkAqua() = TextComponentString(this).also { it.style.color = TextFormatting.DARK_AQUA }
fun String.darkRed() = TextComponentString(this).also { it.style.color = TextFormatting.DARK_RED }
fun String.darkPurple() = TextComponentString(this).also { it.style.color = TextFormatting.DARK_PURPLE }
fun String.gold() = TextComponentString(this).also { it.style.color = TextFormatting.GOLD }
fun String.gray() = TextComponentString(this).also { it.style.color = TextFormatting.GRAY }
fun String.darkGray() = TextComponentString(this).also { it.style.color = TextFormatting.DARK_GRAY }
fun String.blue() = TextComponentString(this).also { it.style.color = TextFormatting.BLUE }
fun String.green() = TextComponentString(this).also { it.style.color = TextFormatting.GREEN }
fun String.aqua() = TextComponentString(this).also { it.style.color = TextFormatting.AQUA }
fun String.lightPurple() = TextComponentString(this).also { it.style.color = TextFormatting.LIGHT_PURPLE }
fun String.yellow() = TextComponentString(this).also { it.style.color = TextFormatting.YELLOW }
fun String.white() = TextComponentString(this).also { it.style.color = TextFormatting.WHITE }

fun String.text() = TextComponentString(this)
fun String.stripCodes(): String = this.replace("[&§][A-Ea-e0-9K-Ok-oRr]".toRegex(), "")

fun ITextComponent.onClick(action: (p: EntityPlayerMP) -> Unit): ITextComponent = onClick(Consumer { action.invoke(it) })
fun ITextComponent.onClick(action: Consumer<EntityPlayerMP>) = also { it.style.clickEvent = click(action) }
fun ITextComponent.onHover(string: String) = also { it.style.hoverEvent = hover(string) }
fun ITextComponent.onHover(text: ITextComponent) = also { it.style.hoverEvent = hover(text) }
fun ITextComponent.underline() = also { it.style.underlined = true }
fun ITextComponent.bold() = also { it.style.bold = true }
fun ITextComponent.italicise() = also { it.style.italic = true }
fun ITextComponent.strikethrough() = also { it.style.strikethrough = true }
fun ITextComponent.obfuscate() = also { it.style.obfuscated = true }

public fun ITextComponent.add(other: ITextComponent): ITextComponent = this.appendSibling(other)
public fun ITextComponent.add(string: String): ITextComponent = this.appendSibling(of(string))

fun Style.assimilate(other: Style): Style {
    hoverEvent = hoverEvent ?: other.hoverEvent
    clickEvent = clickEvent ?: other.clickEvent
    color = color ?: other.color
    return this
}
