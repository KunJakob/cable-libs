package co.aikar.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ACFForgeUtil {
    public static EntityPlayerMP findPlayerSmart(CommandIssuer issuer, String search) {
        ICommandSender requester = issuer.getIssuer();
        if (search == null) {
            return null;
        }
        String name = ACFUtil.replace(search, ":confirm", "");
        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<EntityPlayerMP> matches = matchPlayer(name);
        List<EntityPlayerMP> confirmList = new ArrayList<>();
        findMatches(search, requester, matches, confirmList);


        if (matches.size() > 1 || confirmList.size() > 1) {
            String allMatches = matches.stream().map(EntityPlayerMP::getName).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        if (matches.isEmpty()) {
            EntityPlayerMP player = ACFUtil.getFirstElement(confirmList);
            if (player == null) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", name);
                return null;
            } else {

                issuer.sendInfo(MinecraftMessageKeys.PLAYER_IS_VANISHED_CONFIRM, "{vanished}", player.getName());
                return null;
            }
        }

        return matches.get(0);
    }

    private static void findMatches(String search, ICommandSender requester, List<EntityPlayerMP> matches, List<EntityPlayerMP> confirmList) {
        // Remove vanished players from smart matching.
        Iterator<EntityPlayerMP> iter = matches.iterator();
        //noinspection Duplicates
        while (iter.hasNext()) {
            EntityPlayerMP player = iter.next();
            if (requester instanceof EntityPlayerMP && ((EntityPlayerMP) requester).isInvisibleToPlayer(player)) {
                if (requester.canUseCommand(4, "acf.seevanish")) {
                    if (!search.endsWith(":confirm")) {
                        confirmList.add(player);
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        }
    }

    public static List<EntityPlayerMP> matchPlayer(String partialName) {
        List<EntityPlayerMP> matchedPlayers = new ArrayList<>();

        for (EntityPlayerMP iterPlayer : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase(java.util.Locale.ENGLISH).contains(partialName.toLowerCase(java.util.Locale.ENGLISH))) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

}
