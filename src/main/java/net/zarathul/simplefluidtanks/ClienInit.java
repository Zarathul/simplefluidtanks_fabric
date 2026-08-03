package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.rendering.ValveBlockRenderer;

public class ClienInit implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
//		BlockEntityRenderers.register(SimpleFluidTanks.blockEntityTypeTank, TankBlockRenderer::new);
//		BlockEntityRenderers.register(SimpleFluidTanks.blockEntityTypeValve, ValveBlockRenderer::new);

//		ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> {
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom_left"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom_right"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right_bottom"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right_top"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_none"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_right"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom_left"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom_right"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_left"));
//			registry.register(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_right"));
//		});
	}
}
