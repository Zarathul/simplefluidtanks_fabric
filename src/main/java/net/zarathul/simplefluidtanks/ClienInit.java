package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.zarathul.simplefluidtanks.configuration.gui.ConfigGui;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveBlockRenderer;

public class ClienInit implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> {
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom_left"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom_right"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right_bottom"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right_top"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_none"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_right"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom_left"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom_right"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_left"));
			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_right"));
		});

		BlockEntityRendererRegistry.INSTANCE.register(SimpleFluidTanks.entityTank, TankBlockRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(SimpleFluidTanks.entityValve, ValveBlockRenderer::new);

		// Register client side only command to show the config.
		CommandRegistrationCallback.EVENT.register((dispatcher, isDedicatedServer) -> {
			if (isDedicatedServer) return;

			dispatcher.register(
				Commands.literal("sft").requires((commandSource) -> {
					return commandSource.hasPermission(2);
				})
				.executes(context -> {
					context.getSource().sendSuccess(new TranslatableComponent("commands.sft.info"), false);
					return 1;
				})
				.then(
					Commands.literal("config")
					.executes(context -> {
						Minecraft.getInstance().setScreen(new ConfigGui(new TextComponent("SimpleFluidTanks"), null, Settings.class, SimpleFluidTanks.MOD_ID));
						return 1;
					})
				)
			);
		});
	}
}
