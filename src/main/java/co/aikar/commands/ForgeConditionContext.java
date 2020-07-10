package co.aikar.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class ForgeConditionContext extends ConditionContext<ForgeCommandIssuer> {
    ForgeConditionContext(ForgeCommandIssuer issuer, String config) {
        super(issuer, config);
    }


    public ICommandSender getSource() {
        return getIssuer().getIssuer();
    }

    public EntityPlayerMP getPlayer() {
        return getIssuer().getPlayer();
    }
}
