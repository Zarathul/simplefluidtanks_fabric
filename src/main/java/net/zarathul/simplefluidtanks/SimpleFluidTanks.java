package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.zarathul.simplefluidtanks.blocks.WrenchableBlock;
import net.zarathul.simplemodslib.api.configuration.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleFluidTanks implements ModInitializer
{
	// constants
	public static final String MOD_ID = "simplefluidtanks";

	// creative tab
	public static final String CONFIG_GUI_TITLE = "Simple Fluid Tanks";

	// logger
	public static final Logger LOG = LogManager.getLogger(MOD_ID);

	public static Identifier modId(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

	@Override
	public void onInitialize()
	{
		// Config
		Config.initialize(MOD_ID, "Simple Fluid Tanks", false, Settings::init);
		Config.registerServerSideNetworking();
		CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> Config.registerCommand(dispatcher, MOD_ID));
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			Config.initialize(MOD_ID, CONFIG_GUI_TITLE, server.isDedicatedServer(), Settings::init);
		});

		// Register Blocks & Items.
		BlocksAndItems.initialize();

		// Necessary for dismantling blocks with the wrench on crouch right-click.
		// Without this WrenchableBlock.use() is never called when crouching.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (world.isClientSide() || (!player.isCrouching()) || player.isSpectator()) return InteractionResult.PASS;

			BlockState blockState = world.getBlockState(hit.getBlockPos());
			Block hitBlock = blockState.getBlock();
			ItemStack usedItem = player.getItemInHand(hand);

			if ((usedItem.getItem() == BlocksAndItems.itemWrench && hitBlock instanceof WrenchableBlock))
			{
				InteractionResult result = blockState.useItemOn(usedItem, world, player, hand, hit);
				return result;
			}

			return InteractionResult.PASS;
		});
	}
}