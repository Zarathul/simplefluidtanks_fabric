package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.resources.Identifier;
import net.zarathul.simplefluidtanks.blocks.ModBlocks;
import net.zarathul.simplefluidtanks.items.ModItems;
import net.zarathul.simplemodslib.api.configuration.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleFluidTanks implements ModInitializer
{
	public static final String MOD_ID = "simplefluidtanks";
	public static final Logger LOG = LogManager.getLogger(MOD_ID);
	public static final String CONFIG_GUI_TITLE = "Simple Fluid Tanks";

	public static Identifier modId(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

	@Override
	public void onInitialize()
	{
		// Config
		Config.initialize(MOD_ID, CONFIG_GUI_TITLE, false, Settings::init);
		Config.registerServerSideNetworking();
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> Config.registerCommand(dispatcher, MOD_ID));
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			Config.initialize(MOD_ID, CONFIG_GUI_TITLE, server.isDedicatedServer(), Settings::init);
		});

		// Blocks & Items
		ModBlocks.init();
		ModItems.init();

		UseBlockCallback.EVENT.register(ModBlocks::useBlockCallback);
	}
}