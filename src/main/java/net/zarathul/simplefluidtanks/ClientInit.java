package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.zarathul.simplefluidtanks.rendering.ModelLoadingWatchdog;

public class ClientInit implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ModelLoadingPlugin.register(new ModelLoadingWatchdog());
	}
}
