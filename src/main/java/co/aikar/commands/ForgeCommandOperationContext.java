package co.aikar.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class ForgeCommandOperationContext extends CommandOperationContext {

    ForgeCommandOperationContext(CommandManager manager, CommandIssuer issuer, BaseCommand command, String commandLabel, String[] args, boolean isAsync) {
        super(manager, issuer, command, commandLabel, args, isAsync);
    }


    public ICommandSender getSource() {
        return getCommandIssuer().getIssuer();
    }

}
