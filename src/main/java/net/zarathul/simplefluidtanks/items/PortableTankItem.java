package net.zarathul.simplefluidtanks.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplemodslib.ModComponents;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.fluid.FluidContainerComponent;
import net.zarathul.simplemodslib.api.fluid.FluidContainerItemBase;
import net.zarathul.simplemodslib.api.fluid.FluidHelper;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class PortableTankItem extends FluidContainerItemBase
{
	private static final String TOOLTIP_KEY                    = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip";
	private static final String TOOLTIP_MODE_MAX_KEY           = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip_max";
	private static final String TOOLTIP_MODE_SINGLE_BUCKET_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip_single_bucket";
	private static final String TOOLTIP_DETAILS_KEY            = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip_details";

	public PortableTankItem(Properties properties)
	{
		int defaultCapacity = Settings.bucketsPerPortableTank() * FluidStack.BUCKET_VOLUME;
		super(properties.stacksTo(1), defaultCapacity);
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
			FluidContainerComponent component = stack.get(ModComponents.FLUID_CONTAINER_COMPONENT);
			if (component == null) return;

			String fluidName = FluidHelper.getFluidName(component.fluidId());

			tooltip.addAll(Utils.multiLineTranslate(
				TOOLTIP_KEY,
				fluidName,
				(!fluidName.isEmpty()) ? " " : "",	// Insert a space to make the tooltip look nicer, if getting a name was successful.
				component.fluidId(),
				Utils.getMetricFormattedNumber(component.amount() / FluidStack.BUCKET_VOLUME, "%.1f", "%d", "B"),
				Utils.getMetricFormattedNumber(component.capacity() / FluidStack.BUCKET_VOLUME, "%.1f %s%s", "%d %s", "B"),
				(component.singleBucketMode()) ? Utils.translate(TOOLTIP_MODE_SINGLE_BUCKET_KEY) : Utils.translate(TOOLTIP_MODE_MAX_KEY))
			);
		}
	}
}
