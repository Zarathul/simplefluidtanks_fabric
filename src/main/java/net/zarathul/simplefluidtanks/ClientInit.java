package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
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
		ModelLoadingPlugin.register(new ModelLoadingWatchdog());

		Config.registerClientSideNetworking();
		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
			Config.initialize(SimpleFluidTanks.MOD_ID, SimpleFluidTanks.CONFIG_GUI_TITLE, false, Settings::init);
		});

		// Set tooltips for items.
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			if (stack.getItem() == ModItems.VALVE)
			{
				ModItems.VALVE.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == ModItems.TANK)
			{
				ModItems.TANK.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == ModItems.WRENCH)
			{
				ModItems.WRENCH.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == ModItems.PORTABLE_TANK)
			{
				ModItems.PORTABLE_TANK.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
		});
	}
}
