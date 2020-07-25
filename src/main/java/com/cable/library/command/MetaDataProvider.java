package com.cable.library.command;

import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;
import java.util.HashMap;

@FunctionalInterface
public interface MetaDataProvider {
    HashMap<String, MetaDataProvider> metaDataProviders = new HashMap<>();

    @Nullable
    Object provide(ICommandSender sender, boolean required);
}
