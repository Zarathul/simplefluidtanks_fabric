package net.zarathul.simplefluidtanks.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.zarathul.simplefluidtanks.BlocksAndItems;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplemods.api.fluid.FluidApi;
import net.zarathul.simplemods.api.fluid.FluidContainerComponent;
import net.zarathul.simplemods.api.fluid.FluidContainerItemBase;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class PortableTankItem extends FluidContainerItemBase
{
	private static final String TOOLTIP_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + BlocksAndItems.PORTABLE_TANK_ITEM_NAME + ".tooltip";
	private static final String TOOLTIP_MODE_MAX_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + BlocksAndItems.PORTABLE_TANK_ITEM_NAME + ".tooltip_max";
	private static final String TOOLTIP_MODE_SINGLE_BUCKET_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + BlocksAndItems.PORTABLE_TANK_ITEM_NAME + ".tooltip_single_bucket";
	private static final String TOOLTIP_DETAILS_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + BlocksAndItems.PORTABLE_TANK_ITEM_NAME + ".tooltip_details";

	public PortableTankItem(ResourceKey<Item> id, int defaultCapacity)
	{
		super(new Item.Properties()
				.setId(id)
				.stacksTo(1),
			defaultCapacity);
	}

	@Environment(EnvType.CLIENT)
	public void addTooltip(ItemStack stack, TooltipContext context, TooltipFlag tooltipFlag, List<Component> tooltip)
	{
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			tooltip.addAll(Utils.multiLineTranslate(TOOLTIP_DETAILS_KEY, Settings.bucketsPerPortableTank()));
		}
		else
		{
			FluidContainerComponent component = stack.get(FluidApi.FLUID_CONTAINER_COMPONENT);
			if (component == null) return;

			tooltip.addAll(Utils.multiLineTranslate(
				TOOLTIP_KEY,
				component.fluidId(),
				Utils.getMetricFormattedNumber(component.amount(), "%.1f", "%d", "B"),
				Utils.getMetricFormattedNumber(component.capacity(), "%.1f %s%s", "%d %s", "B"),
				(component.singleBucketMode()) ? Utils.translate(TOOLTIP_MODE_SINGLE_BUCKET_KEY) : Utils.translate(TOOLTIP_MODE_MAX_KEY))
			);
		}
	}
}
