package net.zarathul.simplefluidtanks;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.items.PortableTankItem;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplemodslib.SimpleModsLib;
import net.zarathul.simplemodslib.api.fluid.FluidStack;

import java.util.Set;

public final class BlocksAndItems
{
	// block, item and blockEntity names
	public static final String TANK_BLOCK_NAME = "tank";
	public static final String VALVE_BLOCK_NAME = "valve";
	public static final String TANK_ITEM_NAME = "tank";
	public static final String VALVE_ITEM_NAME = "valve";
	public static final String WRENCH_ITEM_NAME = "wrench";
	public static final String PORTABLE_TANK_ITEM_NAME = "portable_tank";
	public static final String TANK_BLOCK_ENTITY_NAME = "tank";
	public static final String VALVE_BLOCK_ENTITY_NAME = "valve";
	// blocks
	public static final TankBlock blockTank;
	public static final ValveBlock blockValve;
	// blockEntities
	public static final BlockEntityType<TankBlockEntity> blockEntityTypeTank;
	public static final BlockEntityType<ValveBlockEntity> blockEntityTypeValve;
	// items
	public static final TankItem itemTank;
	public static final ValveItem itemValve;
	public static final WrenchItem itemWrench;
	public static final PortableTankItem itemPortableTank;

	private BlocksAndItems() {}

	static
	{
		blockTank = new TankBlock(createBlockKey(TANK_BLOCK_NAME));
		itemTank = new TankItem(createItemKey(TANK_ITEM_NAME));

		blockValve = new ValveBlock(createBlockKey(VALVE_BLOCK_NAME));
		itemValve = new ValveItem(createItemKey(VALVE_ITEM_NAME));

		itemWrench = new WrenchItem(createItemKey(WRENCH_ITEM_NAME));
		itemPortableTank = new PortableTankItem(createItemKey(PORTABLE_TANK_ITEM_NAME), Settings.bucketsPerPortableTank() * FluidStack.BUCKET_VOLUME);

		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(TANK_ITEM_NAME), itemTank);
		Registry.register(BuiltInRegistries.BLOCK, Utils.createModIdentifier(TANK_BLOCK_NAME), blockTank);
		blockEntityTypeTank = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createBlockEntityKey(TANK_BLOCK_ENTITY_NAME), new BlockEntityType<>(TankBlockEntity::new, Set.of(blockTank)));

		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(VALVE_ITEM_NAME), itemValve);
		Registry.register(BuiltInRegistries.BLOCK, Utils.createModIdentifier(VALVE_BLOCK_NAME), blockValve);
		blockEntityTypeValve = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, createBlockEntityKey(VALVE_BLOCK_ENTITY_NAME), new BlockEntityType<>(ValveBlockEntity::new, Set.of(blockValve)));

		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(WRENCH_ITEM_NAME), itemWrench);
		Registry.register(BuiltInRegistries.ITEM, Utils.createModIdentifier(PORTABLE_TANK_ITEM_NAME), itemPortableTank);

		SimpleModsLib.creativeModeTabItems.add(itemTank);
		SimpleModsLib.creativeModeTabItems.add(itemValve);
		SimpleModsLib.creativeModeTabItems.add(itemWrench);
		SimpleModsLib.creativeModeTabItems.add(itemPortableTank);
	}

	public static void initialize()
	{
		SimpleFluidTanks.log.info("Initializing blocks and items.");
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
}
