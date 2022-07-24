package com.fibermc.essentialcommands;

import java.nio.file.Path;

import com.fibermc.essentialcommands.config.EssentialCommandsConfig;
import com.fibermc.essentialcommands.config.EssentialCommandsConfigSnapshot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

import dev.jpcode.eccore.util.TimeUtil;

public final class EssentialCommands implements ModInitializer {
    public static final ModMetadata MOD_METADATA = FabricLoader.getInstance().getModContainer("essential_commands").orElseThrow().getMetadata();
    public static final String MOD_ID = MOD_METADATA.getId();
    public static final Logger LOGGER = LogManager.getLogger("EssentialCommands");
    public static final EssentialCommandsConfig BACKING_CONFIG = new EssentialCommandsConfig(
        Path.of("./config/EssentialCommands.properties"),
        "Essential Commands Config",
        "https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation"
    );
    @SuppressWarnings("checkstyle:StaticVariableName")
    public static EssentialCommandsConfigSnapshot CONFIG = EssentialCommandsConfigSnapshot.create(BACKING_CONFIG);

    public static void log(Level level, String message) {
        final String logPrefix = "[EssentialCommands]: ";
        LOGGER.log(level, logPrefix.concat(message));
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Mod Load Initiated.");

        BACKING_CONFIG.registerLoadHandler((backingConfig) -> CONFIG = EssentialCommandsConfigSnapshot.create(backingConfig));
        BACKING_CONFIG.loadOrCreateProperties();

        ECPlaceholderRegistry.register();
        ECAbilitySources.init();

        ManagerLocator managers = ManagerLocator.getInstance();
        managers.init();
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            ECText.init(server);
            TimeUtil.init(server);
            managers.onServerStart(server);
            ECPerms.init(); // ECPerms must start after WorldDataManager at present (for warps).
        });

        CommandRegistrationCallback.EVENT.register(EssentialCommandRegistry::register);

        if (CONFIG.CHECK_FOR_UPDATES) {
            Updater.checkForUpdates();
        }

        log(Level.INFO, "Mod Load Complete.");
    }
}
