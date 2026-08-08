package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.zarathul.simplefluidtanks.blocks.WrenchableBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplemods.api.configuration.Config;
import net.zarathul.simplemods.api.fluid.FluidApi;
import net.zarathul.simplemods.api.fluid.FluidHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleFluidTanks implements ModInitializer
{
	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String SIMPLE_MODS_ID = "simplemods";
	// This MUST be 16 to get the correct UV coordinates during rendering, because 16 is hardcoded into the interpolation method.
	public static final int MAX_FILL_LEVEL = 16;

	// creative tab
	public static final String CREATIVE_MODE_TAB_TITLE = "Simple Mods";
	public static final String CONFIG_GUI_TITLE = "Simple Fluid Tanks";
	public static final Identifier CREATIVE_MODE_TAB_ID = Identifier.fromNamespaceAndPath(SIMPLE_MODS_ID, "creative_tab");
	public static CreativeModeTab creativeTab;

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	public static boolean onDedicatedServer;


	@Override
	public void onInitialize()
	{
		Config.initialize(MOD_ID, "Simple Fluid Tanks", false, Settings::init);
		Config.registerServerSideNetworking();
		CommandRegistrationCallback.EVENT.register(Config::registerCommand);

		FluidApi.initialize();

		// Register Blocks & Items.
		BlocksAndItems.initialize();

		// Register creative tab.
		creativeTab = MakeCreativeTab();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_MODE_TAB_ID, creativeTab);

		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			Config.initialize(MOD_ID, CONFIG_GUI_TITLE, server.isDedicatedServer(), Settings::init);
		});

		// Necessary for dismantling blocks with the wrench on sneak right-click.
		// Without this WrenchableBlock.use() is never called when sneaking.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (world.isClientSide() || (!player.isShiftKeyDown()) || player.isSpectator()) return InteractionResult.PASS;

			BlockState blockState = world.getBlockState(hit.getBlockPos());
			Block hitBlock = blockState.getBlock();
			ItemStack usedItem = player.getItemInHand(hand);

			if (((usedItem.getItem() == BlocksAndItems.itemWrench) && ((hitBlock instanceof WrenchableBlock))) ||
				((usedItem.getItem() == BlocksAndItems.itemPortableTank) && (FluidHelper.isFluidHandler(world, hit.getBlockPos()))))
			{
				InteractionResult result = blockState.useItemOn(usedItem, world, player, hand, hit);
				return result;
			}

			return InteractionResult.PASS;
		});

		// Prevent buckets from doing their usual thing when right-clicking a valve.
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (player.isSpectator()) return InteractionResult.PASS;

			ItemStack items = player.getItemInHand(hand);
			if ((items.getItem() instanceof BucketItem))
			{
				BlockHitResult hit = Utils.getPlayerPOVHitResult(world, player);
				if (hit.getType() == HitResult.Type.BLOCK)
				{
					BlockState state = world.getBlockState(hit.getBlockPos());
					if ((state != null) && (state.getBlock() == BlocksAndItems.blockValve))
					{
						return InteractionResult.SUCCESS_SERVER;
					}
				}
			}

			return InteractionResult.PASS;
		});
	}

	public static CreativeModeTab MakeCreativeTab()
	{
		var simpleModsTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CREATIVE_MODE_TAB_ID);

		if (simpleModsTab.isPresent()) return simpleModsTab.get().value();

		return FabricCreativeModeTab.builder()
			.title(Component.literal(CREATIVE_MODE_TAB_TITLE))
			.icon(() -> new ItemStack(BlocksAndItems.blockValve))
			.displayItems((parameters, output) -> {
				output.accept(BlocksAndItems.itemTank);
				output.accept(BlocksAndItems.itemValve);
				output.accept(BlocksAndItems.itemWrench);
				output.accept(BlocksAndItems.itemPortableTank);
			})
			.build();
	}
}