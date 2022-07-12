package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;


public class WarpSetCommand implements Command<ServerCommandSource> {

    public WarpSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean requiresPermission;
        try {
            requiresPermission = BoolArgumentType.getBool(context, "requires_permission");
        } catch (IllegalArgumentException ign) {
            requiresPermission = false;
        }

        var warpNameText = ECText.accent(warpName);
        //Add warp
        try {
            worldDataManager.setWarp(warpName, new MinecraftLocation(senderPlayer), requiresPermission);
            //inform command sender that the home has been set
            source.sendFeedback(
                ECText.getInstance().getText("cmd.warp.set.feedback", warpNameText),
                CONFIG.BROADCAST_TO_OPS
            );
        } catch (CommandSyntaxException e) {
            source.sendError(
                ECText.getInstance().getText("cmd.warp.set.error.exists", TextFormatType.Error, warpNameText)
            );
        }

        return 1;
    }
}
