package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.zarathul.simplefluidtanks.rendering.ModelLoadingWatchdog;
import net.zarathul.simplemods.api.configuration.Config;

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
			if (stack.getItem() == BlocksAndItems.itemValve)
			{
				BlocksAndItems.itemValve.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == BlocksAndItems.itemTank)
			{
				BlocksAndItems.itemTank.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == BlocksAndItems.itemWrench)
			{
				BlocksAndItems.itemWrench.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
			else if (stack.getItem() == BlocksAndItems.itemPortableTank)
			{
				BlocksAndItems.itemPortableTank.addTooltip(stack, tooltipContext, tooltipFlag, lines);
			}
		});
	}
}
