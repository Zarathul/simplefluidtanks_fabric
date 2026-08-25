package net.zarathul.simplefluidtanks.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ModBlocks;
import net.zarathul.simplemodslib.ModComponents;
import net.zarathul.simplemodslib.SimpleModsLib;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.fluid.FluidContainerComponent;
import net.zarathul.simplemodslib.api.fluid.FluidHelper;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.item.ItemRegistrar;

import java.util.Collections;

public final class ModItems
{
	private static final ItemRegistrar REGISTRAR = new ItemRegistrar(SimpleFluidTanks.MOD_ID);

	public static final BlockItem TANK = REGISTRAR.register("tank", ModBlocks.TANK, BlockItem::new, new Item.Properties().stacksTo(64), null, _ -> new Object[] { Settings.bucketsPerTank() });
	public static final BlockItem VALVE = REGISTRAR.register("valve", ModBlocks.VALVE, BlockItem::new, new Item.Properties().stacksTo(64));
	public static final Item WRENCH = REGISTRAR.register("wrench", Item::new, new Item.Properties().stacksTo(1));
	public static final PortableTankItem PORTABLE_TANK = REGISTRAR.register(
		"portable_tank",
		PortableTankItem::new,
		itemStack -> {
			FluidContainerComponent component = itemStack.get(ModComponents.FLUID_CONTAINER_COMPONENT);
			if (component == null) return new Object[0];

			String fluidName = FluidHelper.getFluidName(component.fluidId());
			return new Object[] {
				fluidName,
				(!fluidName.isEmpty()) ? " " : "",	// Insert a space to make the tooltip look nicer, if getting a name was successful.
				component.fluidId(),
				Utils.getMetricFormattedNumber(component.amount() / FluidStack.BUCKET_VOLUME, "%.1f", "%d", "B"),
				Utils.getMetricFormattedNumber(component.capacity() / FluidStack.BUCKET_VOLUME, "%.1f %s%s", "%d %s", "B"),
				(component.singleBucketMode()) ? Utils.translate("item.simplefluidtanks.portable_tank.tooltip_single_bucket") : Utils.translate("item.simplefluidtanks.portable_tank.tooltip_max")
			};
		},
		_ -> new Object[] { Settings.bucketsPerPortableTank() }
	);

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

	public static void registerTooltips()
	{
		REGISTRAR.registerTooltips();
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