package co.aikar.commands;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ForgeMessageFormatter extends MessageFormatter<TextFormatting> {

    public ForgeMessageFormatter(TextFormatting... colors) {
        super(colors);
    }

    public String format(TextFormatting color, String message) {
        return new TextComponentString(message).setStyle(new Style().setColor(color)).getFormattedText();
    }


}
