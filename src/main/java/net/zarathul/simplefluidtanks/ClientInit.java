package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.zarathul.simplefluidtanks.items.ModItems;
import net.zarathul.simplefluidtanks.rendering.ModelLoadingWatchdog;
import net.zarathul.simplemodslib.api.configuration.Config;

public class ClientInit implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		// Rendering
		ModelLoadingPlugin.register(new ModelLoadingWatchdog());

		// Config
		Config.registerClientSideNetworking();
		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
			Config.initialize(SimpleFluidTanks.MOD_ID, SimpleFluidTanks.CONFIG_GUI_TITLE, false, Settings::init);
		});

		ModItems.registerTooltips();
	}
}
