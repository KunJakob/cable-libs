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

import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import com.cable.library.CableLibs;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class ForgeCommandManager extends CommandManager<
        ICommandSender,
        ForgeCommandIssuer,
        TextFormatting,
        ForgeMessageFormatter,
        ForgeCommandExecutionContext,
        ForgeConditionContext
    > {

    protected final CableLibs mod;
    protected Map<String, ForgeRootCommand> registeredCommands = new HashMap<>();
    protected ForgeCommandContexts contexts;
    protected ForgeCommandCompletions completions;
    protected ForgeLocales locales;

    public ForgeCommandManager(CableLibs mod) {
        this.mod = mod;
        String pluginName = "acf-" + CableLibs.MOD_ID;
        getLocales().addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase(Locale.ENGLISH));

        this.formatters.put(MessageType.ERROR, defaultFormatter = new ForgeMessageFormatter(TextFormatting.RED, TextFormatting.YELLOW, TextFormatting.RED));
        this.formatters.put(MessageType.SYNTAX, new ForgeMessageFormatter(TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.WHITE));
        this.formatters.put(MessageType.INFO, new ForgeMessageFormatter(TextFormatting.BLUE, TextFormatting.DARK_GREEN, TextFormatting.GREEN));
        this.formatters.put(MessageType.HELP, new ForgeMessageFormatter(TextFormatting.AQUA, TextFormatting.GREEN, TextFormatting.YELLOW));
        getLocales(); // auto load locales

        MinecraftForge.EVENT_BUS.register(new ACFForgeListener(this));

        //TODO more default dependencies for sponge
        registerDependency(mod.getClass(), mod);
    }

    public CableLibs getMod() {
        return mod;
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return ICommandSender.class.isAssignableFrom(type);
    }

    @Override
    public synchronized CommandContexts<ForgeCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new ForgeCommandContexts(this);
        }
        return contexts;
    }

    @Override
    public synchronized CommandCompletions<ForgeCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new ForgeCommandCompletions(this);
        }
        return completions;
    }

    @Override
    public ForgeLocales getLocales() {
        if (this.locales == null) {
            this.locales = new ForgeLocales(this);
            this.locales.loadLanguages();
            this.locales.loadMissingBundles();
        }
        return locales;
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !registeredCommands.isEmpty();
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);

        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            ForgeRootCommand spongeCommand = (ForgeRootCommand) entry.getValue();
            if (!spongeCommand.isRegistered) {
                ServerCommandManager manager = (ServerCommandManager) FMLCommonHandler.instance().getMinecraftServerInstance().commandManager;
                manager.registerCommand(spongeCommand);
            }
            spongeCommand.isRegistered = true;
            registeredCommands.put(commandName, spongeCommand);
        }
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new ForgeRootCommand(this, cmd);
    }
    
    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(registeredCommands.values());
    }

    @Override
    public ForgeCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof ICommandSender)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        }
        return new ForgeCommandIssuer(this, (ICommandSender) issuer);
    }

    @Override
    public ForgeCommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new ForgeCommandExecutionContext(command, parameter, (ForgeCommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new ForgeCommandCompletionContext(command, (ForgeCommandIssuer) sender, input, config, args);
    }

    @Override
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        return new ForgeRegisteredCommand(command, cmdName, method, prefSubCommand);
    }

    @Override
    public void log(final LogLevel level, final String message, final Throwable throwable) {
        Logger logger = this.mod.getLogger();
        switch(level) {
            case INFO:
                logger.info(LogLevel.LOG_PREFIX + message);
                if (throwable != null) {
                    for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                        logger.info(LogLevel.LOG_PREFIX + line);
                    }
                }
                return;
            case ERROR:
                logger.log(Level.SEVERE, LogLevel.LOG_PREFIX + message);
                if (throwable != null) {
                    for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                        logger.log(Level.SEVERE,LogLevel.LOG_PREFIX + line);
                    }
                }
        }
    }

    @Override
    CommandOperationContext createCommandOperationContext(BaseCommand command, CommandIssuer issuer, String commandLabel, String[] args, boolean isAsync) {
        return new ForgeCommandOperationContext(
                this,
                issuer,
                command,
                commandLabel,
                args,
                isAsync
        );
    }

    @Override
    public ForgeConditionContext createConditionContext(CommandIssuer issuer, String config) {
        return new ForgeConditionContext((ForgeCommandIssuer) issuer, config);
    }

    @Override
    public String getCommandPrefix(CommandIssuer issuer) {
        return issuer.isPlayer() ? "/" : "";
    }
}
