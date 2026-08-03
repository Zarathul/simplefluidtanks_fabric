package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.blocks.WrenchableBlock;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.items.PortableTankItem;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplemods.api.fluid.FluidContainerComponent;
import net.zarathul.simplemods.api.fluid.FluidHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimpleFluidTanks implements ModInitializer
{
	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String SIMPLE_MODS_ID = "simplemods";
	// This MUST be 16 to get the correct UV coordinates during rendering, because 16 is hardcoded into the interpolation method.
	public static final int MAX_FILL_LEVEL = 16;

	// block, item and tileentity names
	public static final String TANK_BLOCK_NAME = "tank";
	public static final String VALVE_BLOCK_NAME = "valve";

	public static final String TANK_ITEM_NAME = "tank";
	public static final String VALVE_ITEM_NAME = "valve";

	public static final String WRENCH_ITEM_NAME = "wrench";
	public static final String PORTABLE_TANK_ITEM_NAME = "portable_tank";

	public static final String TANK_BLOCK_ENTITY_NAME = "tank";
	public static final String VALVE_BLOCK_ENTITY_NAME = "valve";

	// creative tab
	public static final String CREATIVE_MODE_TAB_TITLE = "Simple Mods";
	public static final Identifier CREATIVE_MODE_TAB_ID = Identifier.fromNamespaceAndPath(SIMPLE_MODS_ID, "creative_tab");
	public static final CreativeModeTab creativeTab = MakeCreativeTab();

	// blocks
	public static final TankBlock blockTank;
	public static final ValveBlock blockValve;

	// tileEntities
	public static BlockEntityType<TankBlockEntity> blockEntityTypeTank;
	public static BlockEntityType<ValveBlockEntity> blockEntityTypeValve;

	// items
	public static final TankItem itemTank;
	public static final ValveItem itemValve;
	public static final WrenchItem itemWrench;
	public static final PortableTankItem itemPortableTank;
	public static DataComponentType<FluidContainerComponent> FLUID_CONTAINER_COMPONENT; // TODO: move to api

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	public static boolean onDedicatedServer;

	static
	{
		Config.reset();
		Settings.init();
		Config.loadOrCreateConfigFile(MOD_ID, false);

		blockTank = new TankBlock(createBlockKey(TANK_BLOCK_NAME));
		itemTank = new TankItem(createItemKey(TANK_ITEM_NAME));
		blockValve = new ValveBlock(createBlockKey(VALVE_BLOCK_NAME));
		itemValve = new ValveItem(createItemKey(VALVE_ITEM_NAME));
		itemWrench = new WrenchItem(createItemKey(WRENCH_ITEM_NAME));
		itemPortableTank = new PortableTankItem(createItemKey(PORTABLE_TANK_ITEM_NAME));
	}

	@Override
	public void onInitialize()
	{
		// Register Blocks & Items.
		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(TANK_ITEM_NAME), itemTank);
		Registry.register(BuiltInRegistries.BLOCK, Utils.createModIdentifier(TANK_BLOCK_NAME), blockTank);
		blockEntityTypeTank = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createBlockEntityKey(TANK_BLOCK_ENTITY_NAME), new BlockEntityType<>(TankBlockEntity::new, Set.of(blockTank)));

		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(VALVE_ITEM_NAME), itemValve);
		Registry.register(BuiltInRegistries.BLOCK, Utils.createModIdentifier(VALVE_BLOCK_NAME), blockValve);
		blockEntityTypeValve = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createBlockEntityKey(VALVE_BLOCK_ENTITY_NAME), new BlockEntityType<>(ValveBlockEntity::new, Set.of(blockValve)));

		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(WRENCH_ITEM_NAME), itemWrench);
		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(PORTABLE_TANK_ITEM_NAME), itemPortableTank);

		// Register creative tab.
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_MODE_TAB_ID, creativeTab);

		// TODO: move to api
		FLUID_CONTAINER_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(SIMPLE_MODS_ID, "fluid_container"),
			DataComponentType.<FluidContainerComponent>builder().persistent(FluidContainerComponent.CODEC).build());

		// Register config command
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			dispatcher.register(
				Commands.literal("sft")
				.executes(context -> {
					context.getSource().sendSuccess(() -> Component.translatable("commands.sft.info"), false);
					return 1;
				})
				.then(
					Commands.literal("config")
					.executes(context -> {
						var player =  context.getSource().getPlayer();
						List<Config.ConfigValue> configValues = new ArrayList<>();
						Config.writeServerSettings(false, configValues, player);
						SimpleFluidTanks.ConfigCommandPayload outgoingPayload = new SimpleFluidTanks.ConfigCommandPayload(configValues, SimpleFluidTanks.onDedicatedServer);

						ServerPlayNetworking.send(player, outgoingPayload);

						return 1;
					})
				)
			);
		});

		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			onDedicatedServer = server.isDedicatedServer();
			Config.reset();
			Settings.init();
			Config.loadOrCreateConfigFile(MOD_ID, onDedicatedServer);
		});

		// Necessary for dismantling blocks with the wrench on sneak right-click.
		// Without this WrenchableBlock.use() is never called when sneaking.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (world.isClientSide() || (!player.isShiftKeyDown()) || player.isSpectator()) return InteractionResult.PASS;

			BlockState blockState = world.getBlockState(hit.getBlockPos());
			Block hitBlock = blockState.getBlock();
			ItemStack usedItem = player.getItemInHand(hand);

			if (((usedItem.getItem() == itemWrench) && ((hitBlock instanceof WrenchableBlock))) ||
				((usedItem.getItem() == itemPortableTank) && (FluidHelper.isFluidHandler(world, hit.getBlockPos()))))
			{
				InteractionResult result = blockState.useItemOn(usedItem, world, player, hand, hit);
				return result;
			}

			return  InteractionResult.PASS;
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
					if ((state != null) && (state.getBlock() == blockValve))
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
			.icon(() -> new ItemStack(blockValve))
			.displayItems((parameters, output) -> {
				output.accept(itemTank);
				output.accept(itemValve);
				output.accept(itemWrench);
				output.accept(itemPortableTank);
			})
			.build();
	}

	private static ResourceKey<Block> createBlockKey(String name)
	{
		return ResourceKey.create(Registries.BLOCK, Utils.createModIdentifier(name));
	}

	private static ResourceKey<BlockEntityType<?>> createBlockEntityKey(String name)
	{
		return ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Utils.createModIdentifier(name));
	}

	private static ResourceKey<Item> createItemKey(String name)
	{
		return ResourceKey.create(Registries.ITEM, Utils.createModIdentifier(name));
	}

	public record ConfigCommandPayload(List<Config.ConfigValue> values, boolean fromDedicatedServer) implements CustomPacketPayload
	{
		public static final Identifier ID = Utils.createModIdentifier("config_command");
		public static final CustomPacketPayload.Type<ConfigCommandPayload> TYPE = new CustomPacketPayload.Type<>(ID);
		public static final StreamCodec<FriendlyByteBuf, ConfigCommandPayload> STREAM_CODEC = StreamCodec.composite(
			Config.LIST_STREAM_CODEC, ConfigCommandPayload::values,
			ByteBufCodecs.BOOL, ConfigCommandPayload::fromDedicatedServer,
			ConfigCommandPayload::new
		);

		public @NonNull Type<? extends CustomPacketPayload> type()
		{
			return TYPE;
		}
	}
}