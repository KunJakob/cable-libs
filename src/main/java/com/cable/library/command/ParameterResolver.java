package com.cable.library.command;

import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ParameterResolver {
    @Nullable
    Object resolve(ICommandSender sender, String arg, String parameterName);
}