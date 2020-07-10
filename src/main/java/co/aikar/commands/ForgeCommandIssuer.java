/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class ForgeCommandIssuer implements CommandIssuer {

    private final ForgeCommandManager manager;
    private final ICommandSender source;

    ForgeCommandIssuer(ForgeCommandManager manager, final ICommandSender source) {
        this.manager = manager;
        this.source = source;
    }

    @Override
    public boolean isPlayer() {
        return this.source instanceof EntityPlayerMP;
    }

    @Override
    public ICommandSender getIssuer() {
        return this.source;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        if(source instanceof EntityPlayerMP)
            return ((EntityPlayerMP) source).getUniqueID();

        //generate a unique id based of the name (like for the console command sender)
        return UUID.nameUUIDFromBytes(source.getName().getBytes(StandardCharsets.UTF_8));
    }

    public EntityPlayerMP getPlayer() {
        return isPlayer() ? (EntityPlayerMP) source : null;
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public void sendMessageInternal(String message) {
        this.source.sendMessage(new TextComponentString(message.replaceAll("&([0-9a-fk-or])", "\u00a7$1")));
    }

    @Override
    public boolean hasPermission(final String permission) {
        return this.source.canUseCommand(4, permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForgeCommandIssuer that = (ForgeCommandIssuer) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }
}
