package com.cable.library.command

import com.cable.library.command.MetaDataProvider.metaDataProviders
import com.cable.library.text.of
import com.cable.library.text.red
import net.minecraft.command.CommandHandler
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.FMLCommonHandler
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.ArrayList

@Target(AnnotationTarget.CLASS)
annotation class Command(
    val aliases: Array<String>,
    val node: String,
    val neededArgs: Array<String> = [],
    val optionalArgs: Array<String> = [],
    val neededMetaData: Array<String> = [],
    val optionalMetaData: Array<String> = [],
    val showTypes: Boolean = false,
    val remainingLabel: String = "",
    val isRemainingOptional: Boolean = true,
    val playerOnly: Boolean = false,
    val consoleOnly: Boolean = false
)

interface ICommandExecutor : IAnnotatedCommandExecutor {
    fun getAliases(): Array<String>
    fun getNode(): String
    fun getNeededArgs(): Array<String>
    fun getOptionalArgs(): Array<String>
    fun getNeededMetaData(): Array<String>
    fun getOptionalMetaData(): Array<String>
    fun getShowTypes(): Boolean = false
    fun getRemainingLabel(): String = ""
    fun getIsRemainingOptional(): Boolean = true
    fun getIsPlayerOnly(): Boolean = false
    fun getIsConsoleOnly(): Boolean = false

    override fun toCommand(parentCommand: String): ICableCommand = toCommand(
            getAliases(),
            getNode(),
            getNeededArgs(),
            getOptionalArgs(),
            getNeededMetaData(),
            getOptionalMetaData(),
            getShowTypes(),
            getRemainingLabel(),
            getIsRemainingOptional(),
            getIsPlayerOnly(),
            getIsConsoleOnly(),
            parentCommand
    )
}

interface IAnnotatedRootCommand : IAnnotatedCommandExecutor {
    override fun run(server: MinecraftServer, sender: ICommandSender, params: Parameters) {}
}

interface IAnnotatedCommandExecutor {
    companion object {
        val ALL = hashMapOf<String, ICableCommand>()
    }

    fun run(server: MinecraftServer, sender: ICommandSender, params: Parameters)

    fun getSubcommands(): List<IAnnotatedCommandExecutor> = listOf()

    fun register() {
        val ch = FMLCommonHandler.instance().minecraftServerInstance.getCommandManager() as CommandHandler

        val command = toCommand()
        ALL[command.name] = command
        ch.registerCommand(command)
    }

    fun toCommand(
        aliases: Array<String>,
        node: String,
        neededArgs: Array<String> = arrayOf(),
        optionalArgs: Array<String> = arrayOf(),
        neededMetaData: Array<String> = arrayOf(),
        optionalMetaData: Array<String> = arrayOf(),
        showTypes: Boolean = false,
        remainingLabel: String = "",
        isRemainingOptional: Boolean = true,
        playerOnly: Boolean = false,
        consoleOnly: Boolean = false,
        parentCommand: String = ""
    ): ICableCommand {

        val subcommands = getSubcommands().map { it.toCommand(parentCommand = "$parentCommand${aliases[0]} ") }

        return object: ICableCommand {
            override fun getName() = aliases[0]

            override fun getNode() = node

            override fun getSubcommands(): List<ICableCommand> = subcommands

            override fun getTabCompletions(
                server: MinecraftServer,
                sender: ICommandSender,
                args: Array<String>,
                targetPos: BlockPos?
            ): MutableList<String> {
                return subcommands.asSequence().map{it.name}.toMutableList()
            }

            override fun getUsage(sender: ICommandSender): String {

                var usage = "/$parentCommand$name"

                if (subcommands.isNotEmpty()) {
                    val usable = subcommands.filter { it.checkPermission(sender.server!!, sender) }

                    if (usable.isEmpty()) {
                        return " <subcommands that you don't have permission to use!>"
                    }

                    usage += " <${usable[0].name}"
                    for (subcommand in usable.subList(1, usable.size)) {
                        if (subcommand.checkPermission(sender.server!!, sender)) {
                            usage += " | ${subcommand.name}"
                        }
                    }
                    usage += ">"

                    if (this@IAnnotatedCommandExecutor !is IAnnotatedRootCommand) {
                        usage = usage.replace("<", "[").replace(">", "]")
                    }
                } else {

                    val formatArg: (str: String) -> String = {
                        val arg = if (it.contains("::")) it else "$it::$it"

                        if (showTypes && arg.split("::")[0] != arg.split("::")[1]) {
                            if (arg.split("::")[1] == "literal") {
                                "'${arg.split("::")[0]}'"
                            } else {
                                arg
                            }
                        } else {
                            if (arg.split("::")[1] == "literal") {
                                "'${arg.split("::")[0]}'"
                            } else {
                                arg.split("::")[0]
                            }
                        }
                    }

                    for (needed in neededArgs) {
                        usage += " <${formatArg(needed)}>"
                    }
                    for (optional in optionalArgs) {
                        usage += " [${formatArg(optional)}]"
                    }

                    if (remainingLabel.isNotBlank()) {
                        if (isRemainingOptional) {
                            usage += " [$remainingLabel]"
                        } else {
                            usage += " <$remainingLabel>"
                        }
                    }

                }

                return usage
            }

            override fun compareTo(other: ICommand?) = name.compareTo(other?.name!!)

            override fun isUsernameIndex(args: Array<String>, index: Int) = false

            override fun getAliases(): MutableList<String> = aliases.copyOfRange(1, aliases.size).toMutableList()

            override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {

                val needed = ArrayList<String>(neededArgs.toList().map{if (!it.contains("::")) "$it::$it" else it})
                val optional = ArrayList<String>(optionalArgs.toList().map{if (!it.contains("::")) "$it::$it" else it})

                if (args.isNotEmpty()) {
                    val subcommand = subcommands.find{ it.aliases.asSequence().plus(it.name).map{ s -> s.toLowerCase() }.contains(args[0].toLowerCase())}
                    if (subcommand != null) {
                        if (subcommand.checkPermission(server, sender)) {
                            subcommand.execute(server, sender, args.copyOfRange(1, args.size))
                        } else {
                            sender.sendMessage("You do not have permission to use this subcommand!".red())
                        }
                        return
                    }
                }

                if (this@IAnnotatedCommandExecutor is IAnnotatedRootCommand) {
                    sender.sendMessage(getUsage(sender).red())
                    return
                }

                if (sender !is EntityPlayerMP && playerOnly) {
                    return sender.sendMessage("Only players use this command.".red())
                } else if (sender !is MinecraftServer && consoleOnly) {
                    return sender.sendMessage("Only the console can use this command.".red())
                }

                val given = ArrayList<String>(args.toList())

                val params = Parameters(sender, given, needed, optional, remainingLabel, isRemainingOptional)

                neededMetaData.forEach {
                    val provider = metaDataProviders[it.toLowerCase()]
                            ?: throw IllegalStateException("No meta data provider registered for ${it.toLowerCase()}")
                    val provided = provider.provide(sender, true)
                    if (provided == null) {
                        return
                    } else {
                        params.metaData[it.toLowerCase()] = provided
                    }
                }

                optionalMetaData.forEach {
                    val provider = metaDataProviders[it.toLowerCase()]
                            ?: throw IllegalStateException("No meta data provider registered for ${it.toLowerCase()}")
                    provider.provide(sender, false)?.let { resolved -> params.metaData[it.toLowerCase()] = resolved }
                }

                if (params.didSucceed()) {
                    run(server, sender, params)
                } else {
                    sender.sendMessage(getUsage(sender).red())
                }
            }
        }
    }

    fun toCommand(parentCommand: String = ""): ICableCommand {
        val annotation = javaClass.getAnnotation(Command::class.java);
        return toCommand(annotation.aliases,
                annotation.node,
                annotation.neededArgs,
                annotation.optionalArgs,
                annotation.neededMetaData,
                annotation.optionalMetaData,
                annotation.showTypes,
                annotation.remainingLabel,
                annotation.isRemainingOptional,
                annotation.playerOnly,
                annotation.consoleOnly,
                "$parentCommand")
    }
}

interface ICableCommand : ICommand {
    fun getNode(): String
    fun getSubcommands(): List<ICableCommand>
    override fun checkPermission(server: MinecraftServer, sender: ICommandSender)= sender.canUseCommand(4, getNode())
}

class Parameters {
    private val params = hashMapOf<String, Any>()
    private var succeeded = true
    internal val metaData = hashMapOf<String, Any>()

    var remaining = ""
        private set

    public companion object {
        public val resolvers: HashMap<String, ParameterResolver> = hashMapOf(
                "player" to ParameterResolver { sender, arg, _ ->
                    val player = FMLCommonHandler.instance().minecraftServerInstance.playerList.getPlayerByUsername(arg)
                    if (player != null) {
                        player
                    } else {
                        sender.sendMessage("There is no player online named $arg.".red())
                        null
                    }
                },
                "user" to ParameterResolver { sender, arg, _ ->
                    try {
                        FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache.getProfileByUUID(UUID.fromString(arg))
                                ?: sender.sendMessage("No known user with UUID: $arg".red())
                    } catch (e: Exception) {
                        val lower = arg.toLowerCase()
                        val matched = FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache
                                .usernames.find { it.toLowerCase() == lower }
                        if (matched == null) {
                            sender.sendMessage("Invalid user: $arg".red())
                            null
                        } else {
                            FMLCommonHandler.instance().minecraftServerInstance.playerProfileCache
                                    .getGameProfileForUsername(lower)
                        }
                    }
                },
                "uuid" to ParameterResolver { sender, arg, _ ->
                    try {
                        UUID.fromString(arg)
                    } catch (e: Exception) {
                        sender.sendMessage("Invalid UUID: $arg".red())
                        null
                    }
                },
                "text" to ParameterResolver { _, arg, _ -> arg },
                "number" to ParameterResolver { sender, arg, _ ->
                    try {
                        arg.toInt()
                    } catch (e: NumberFormatException) {
                        sender.sendMessage("Invalid number: $arg.".red())
                        null
                    }
                },
                "decimal" to ParameterResolver { sender, arg, _ ->
                    try {
                        arg.toDouble()
                    } catch (e: NumberFormatException) {
                        sender.sendMessage("Invalid decimal: $arg. A decimal must be a valid number".red())
                        null
                    }
                },
                "price" to ParameterResolver { sender, arg, _ ->
                    try {
                        val price = arg.toInt()
                        if (price < 0) {
                            sender.sendMessage("Invalid price: $arg. A price cannot be negative.".red())
                            null
                        } else {
                            price
                        }
                    } catch (e: NumberFormatException) {
                        sender.sendMessage("Invalid price: $arg. A price must be a number.".red())
                    }
                },
                "yes/no" to ParameterResolver { sender, arg, _ ->
                    if (arg.toLowerCase() in arrayOf("yes", "yep", "yeah", "true", "affirmative")) {
                        true
                    } else if (arg.toLowerCase() in arrayOf("no", "nope", "nah", "false", "negative")) {
                        false
                    } else {
                        sender.sendMessage("Invalid yes/no: $arg.".red())
                        null
                    }
                },
                "world" to ParameterResolver { sender, arg, _ ->
                    val world = FMLCommonHandler.instance().minecraftServerInstance.worlds
                            .find { it.worldInfo.worldName == arg || it.provider.dimension.toString() == arg }

                    if (world != null) {
                        world
                    } else {
                        sender.sendMessage("Invalid world: $arg.".red())
                        null
                    }
                },
                "literal" to ParameterResolver { _, arg, parameterName -> arg.equals(parameterName, ignoreCase = true) }
        )
    }

    fun <T> getNeededMetaData(name: String): T = metaData[name.toLowerCase()]!! as T
    fun <T> getOptionalMetaData(name: String, orElse: T): T = metaData[name.toLowerCase()] as? T ?: orElse

    constructor(
            sender: ICommandSender,
            given: ArrayList<String>,
            needed: ArrayList<String>,
            optional: ArrayList<String>,
            remainingLabel: String,
            isRemainingOptional: Boolean
    ) {

        val totalArgs = needed.plus(optional)
        for (arg in totalArgs) {
            if (!resolvers.containsKey(arg.substringAfter("::").toLowerCase())) {
                sender.sendMessage("Missing type resolver for: ${arg.substringAfter("::")}. This is an issue for Hiroku.".red())
                succeeded = false
                return
            }
        }

        var i = -1
        while (++i < given.size) {
            val arg = given[i]
            val beginning = if (arg.contains(":")) arg.split(":")[0] else ""
            var remainder = if (arg.contains(":")) arg.split(":")[1] else arg

            if (remainder.startsWith("\"") && remainder.replace("[%\"]".toRegex(), "").length == 1) {
                remainder = remainder.substring(1)
                var completed = false
                ((i+1).until(given.size)).forEach {
                    if (given[it].endsWith("\"")) {
                        remainder += " " + given[it].substring(0, given[it].length - 1)
                        0.until(it - i).forEach {k -> given.removeAt(i + 1 + k)}
                        given[i] = "$beginning:$remainder"
                        completed = true
                        return@forEach
                    } else {
                        remainder += " ${given[it]}"
                    }
                }
                if (!completed) {
                    sender.sendMessage(of("&cMissing closing \" after $beginning:$remainder"))
                    succeeded = false
                    return
                }
            }
        }

        i = -1
        while (++i < given.size) {
            val arg = given[i]
            if (arg.contains(":")) {
                try {
                    val triedKey = arg.split(":")[0].toLowerCase()
                    val triedValue = arg.substringAfter(":")
                    val array = optional.plus(needed)
                    val resolvedKey = tryResolveNamed(sender, array, triedKey, triedValue)
                    if (!succeeded)
                        return
                    if (resolvedKey != null) {
                        needed.remove(resolvedKey)
                        optional.remove(resolvedKey)
                        given.removeAt(i--)
                        continue
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        for (neededArg in needed) {
            if (given.isEmpty()) {
                sender.sendMessage("Missing argument: ${neededArg.substringBefore("::")}".red())
                succeeded = false
                return
            } else {
                val resolver = resolvers[neededArg.substringAfter("::").toLowerCase()]!!
                val parsed = resolver.resolve(sender, given.removeAt(0), neededArg.substringBefore("::"))
                if (parsed == null) {
                    succeeded = false
                    return
                }

                params[neededArg.split("::")[0].toLowerCase()] = parsed
            }
        }

        for (optionalArg in optional) {
            if (given.isEmpty()) {
                break
            } else {
                val resolver = resolvers[optionalArg.substringAfter("::")]!!
                val parsed = resolver.resolve(sender, given.removeAt(0), optionalArg.substringBefore("::"))
                if (parsed != null) {
                    params[optionalArg.split("::")[0].toLowerCase()] = parsed
                }
            }
        }

        for (remainingGiven in given) {
            remaining += "$remainingGiven "
        }

        if (remaining.endsWith(" ")) {
            remaining.substring(0, remaining.length - 1)
        }

        if (remaining.isBlank() && remainingLabel.isNotBlank() && !isRemainingOptional) {
            sender.sendMessage("Missing argument: $remainingLabel".red())
        }

    }

    fun <T> getNeeded(arg: String): T {
        return params[arg] as T
    }

    fun <T> getOptional(arg: String, orElse: T): T {
        return (params[arg] ?: orElse) as T
    }

    fun has(arg: String): Boolean {
        return params.containsKey(arg)
    }

    fun didSucceed() = succeeded

    private fun tryResolveNamed(sender: ICommandSender, arr: List<String>, key: String, value: String): String? {
        arr.forEach {
            val parameterName = it.split("::")[0]
            val parameterType = it.split("::")[1]
            if (parameterName == key.toLowerCase()) {
                val resolver = resolvers[parameterType]!!
                val resolved = resolver.resolve(sender, value, parameterName)
                if (resolved != null) {
                    params[parameterName] = resolved
                    return it
                } else {
                    succeeded = false
                }
            }
        }

        0.until(arr.size).forEach {
            if (arr[it].split("::")[0].toLowerCase() == key.toLowerCase()) {
                val resolver = resolvers[arr[it].split("::")[1]]!!
                val resolved = resolver.resolve(sender, value, key)
                if (resolved != null) {
                    params[key.toLowerCase()] = resolved
                    return arr[it]
                } else {
                    succeeded = false
                }
            }
        }

        return null
    }
}
