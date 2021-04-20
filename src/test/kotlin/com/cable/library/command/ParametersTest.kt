//package com.cable.library.command
//
//import io.mockk.mockk
//import net.minecraft.command.ICommandSender
//import net.minecraft.server.MinecraftServer
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Test
//
//internal class ParametersTest {
//
//    @Test
//    fun `should resolve quoted parameters`() {
//        @Command(
//            aliases = ["cmd"],
//            node = "something",
//            neededArgs = ["name::text"]
//        )
//        class TestCommand: IAnnotatedCommandExecutor {
//            override fun run(server: MinecraftServer, sender: ICommandSender, params: Parameters) {
//                assertEquals("test text", params.getNeeded<String>("name"))
//            }
//        }
//
//        val x = TestCommand().toCommand()
//        x.execute(mockk(), mockk(), arrayOf("\"test", "text\""))
//    }
//
//    @Test
//    fun `should dissect two separate parameters`() {
//        @Command(
//            aliases = ["cmd"],
//            node = "something",
//            neededArgs = ["name1::text", "name2::text"]
//        )
//        class TestCommand: IAnnotatedCommandExecutor {
//            override fun run(server: MinecraftServer, sender: ICommandSender, params: Parameters) {
//                assertEquals("test", params.getNeeded<String>("name1"))
//                assertEquals("text", params.getNeeded<String>("name2"))
//            }
//        }
//
//        val x = TestCommand().toCommand()
//        x.execute(mockk(), mockk(), arrayOf("test", "text"))
//    }
//}