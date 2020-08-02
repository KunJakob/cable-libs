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

import co.aikar.commands.contexts.OnlinePlayer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class ForgeCommandContexts extends CommandContexts<ForgeCommandExecutionContext> {

    public ForgeCommandContexts(final ForgeCommandManager manager) {
        super(manager);

        registerContext(OnlinePlayer.class, c -> {
            OnlinePlayer onlinePlayer = getOnlinePlayer(c.getIssuer(), c.popFirstArg(), c.isOptional());
            return onlinePlayer != null ? new OnlinePlayer(onlinePlayer.getPlayer()) : null;
        });
        registerContext(TextFormatting.class, c -> {
            String first = c.popFirstArg();
            Stream<TextFormatting> colours = Arrays.stream(TextFormatting.values());
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                colours = colours.filter(colour -> finalFilter.equals(ACFUtil.simplifyString(colour.getFriendlyName())));
            }
            Stream<TextFormatting> finalColours = colours;
            TextFormatting col = TextFormatting.getValueByName(ACFUtil.simplifyString(first));
            if(col == null) {
                String valid = finalColours
                        .map(colour -> "<c2>" + ACFUtil.simplifyString(colour.getFriendlyName()) + "</c2>")
                        .collect(Collectors.joining("<c1>,</c1> "));
                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", valid);
            }
            return col;
        });

        registerIssuerAwareContext(ICommandSender.class, ForgeCommandExecutionContext::getSource);
        registerIssuerAwareContext(EntityPlayerMP.class, (c) -> {
            EntityPlayerMP player = c.getSource() instanceof EntityPlayerMP ? (EntityPlayerMP) c.getSource() : null;
            if (player == null && !c.isOptional()) {
                throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
            }
            return player;
        });
        registerContext(OnlinePlayer[].class, (c) -> {
            ForgeCommandIssuer issuer = c.getIssuer();
            final String search = c.popFirstArg();
            boolean allowMissing = c.hasFlag("allowmissing");
            Set<OnlinePlayer> players = new HashSet<>();
            Pattern split = ACFPatterns.COMMA;
            String splitter = c.getFlagValue("splitter", (String) null);
            if (splitter != null) {
                split = Pattern.compile(Pattern.quote(splitter));
            }
            for (String lookup : split.split(search)) {
                OnlinePlayer player = getOnlinePlayer(issuer, lookup, allowMissing);
                if (player != null) {
                    players.add(player);
                }
            }
            if (players.isEmpty() && !c.hasFlag("allowempty")) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER,
                        "{search}", search);

                throw new InvalidCommandArgument(false);
            }
            return players.toArray(new OnlinePlayer[players.size()]);
        });
        registerIssuerAwareContext(World.class, (c) -> {
            String firstArg = c.getFirstArg();
            //java.util.Optional<World> world = firstArg != null ? Sponge.getServer().getWorld(firstArg) : java.util.Optional.empty();
            Optional<WorldServer> world = firstArg != null ? Arrays.stream(FMLCommonHandler.instance().getMinecraftServerInstance().worlds).filter(e -> e.getWorldInfo().getWorldName().equalsIgnoreCase(firstArg)).findFirst()
                    : Optional.empty();
            if (world.isPresent()) {
                c.popFirstArg();
            }
            if (!world.isPresent() && c.getSource() instanceof EntityPlayerMP) {
                world = Optional.of(((EntityPlayerMP) c.getSource()).getServerWorld());
            }
            if (!world.isPresent()) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.INVALID_WORLD);
            }
            return world.get();
        });
    }

    @Nullable
    OnlinePlayer getOnlinePlayer(ForgeCommandIssuer issuer, String lookup, boolean allowMissing) throws InvalidCommandArgument {
        EntityPlayerMP player = ACFForgeUtil.findPlayerSmart(issuer, lookup);
        if (player == null) {
            if (allowMissing) {
                return null;
            }
            throw new InvalidCommandArgument(false);
        }
        return new OnlinePlayer(player);
    }
}
