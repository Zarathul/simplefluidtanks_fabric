package net.zarathul.simplefluidtanks.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ModBlocks;
import net.zarathul.simplemodslib.SimpleModsLib;
import net.zarathul.simplemodslib.api.item.ItemRegistrar;

import java.util.Collections;

public final class ModItems
{
	private static final ItemRegistrar REGISTRAR = new ItemRegistrar(SimpleFluidTanks.MOD_ID);

	public static final String TANK_NAME = "tank";
	public static final String VALVE_NAME = "valve";
	public static final String WRENCH_NAME = "wrench";
	public static final String PORTABLE_TANK_NAME = "portable_tank";

	public static final BlockItem TANK = REGISTRAR.register(TANK_NAME, ModBlocks.TANK, BlockItem::new, new Item.Properties().stacksTo(64));
	public static final BlockItem VALVE = REGISTRAR.register(VALVE_NAME, ModBlocks.VALVE, BlockItem::new, new Item.Properties().stacksTo(64));
	public static final Item WRENCH = REGISTRAR.register(WRENCH_NAME, Item::new, new Item.Properties().stacksTo(1));
	public static final PortableTankItem PORTABLE_TANK = REGISTRAR.register(PORTABLE_TANK_NAME, PortableTankItem::new);

	public static void init()
	{
		SimpleFluidTanks.LOG.info("Registering items.");

		Collections.addAll(SimpleModsLib.creativeModeTabItems,
			TANK,
			VALVE,
			WRENCH,
			PORTABLE_TANK
		);
	}

	/**
	 * Checks if an item is a wrench.
	 *
	 * @param item
	 * The item to check.
	 *
	 * @return
	 * <c>true</c> if the item is a wrench, otherwise <c>false</c>.
	 */
	public static boolean isWrenchItem(Item item)
	{
		// TODO: Add support for other wrenches or tools
		return (item == WRENCH/*
		|| (Utils.isInterfaceAvailable("cofh.api.item", "IToolHammer") && item instanceof IToolHammer)
		|| (Utils.isInterfaceAvailable("blusunrize.immersiveengineering.api.tool", "ITool") && item instanceof ITool)
		|| (Utils.isInterfaceAvailable("appeng.api.implementations.items", "IAEWrench") && item instanceof IAEWrench)*/);
	}
}