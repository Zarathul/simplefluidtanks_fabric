package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.zarathul.simplefluidtanks.items.ModItems;
import net.zarathul.simplefluidtanks.rendering.ModelLoadingWatchdog;
import net.zarathul.simplemodslib.ModComponents;
import net.zarathul.simplemodslib.Utils;
import net.zarathul.simplemodslib.api.configuration.Config;
import net.zarathul.simplemodslib.api.fluid.FluidContainerComponent;
import net.zarathul.simplemodslib.api.fluid.FluidHelper;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import org.lwjgl.glfw.GLFW;

public class ClientInit implements ClientModInitializer
{
	private static final String PORTABLE_TANK_TOOLTIP_KEY                    = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip";
	private static final String PORTABLE_TANK_TOOLTIP_DETAILS_KEY            = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip_details";
	private static final String PORTABLE_TANK_TOOLTIP_MODE_MAX_KEY           = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip_max";
	private static final String PORTABLE_TANK_TOOLTIP_MODE_SINGLE_BUCKET_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.PORTABLE_TANK_NAME + ".tooltip_single_bucket";
	private static final String TANK_TOOLTIP_KEY         = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.TANK_NAME + ".tooltip";
	private static final String TANK_TOOLTIP_DETAILS_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.TANK_NAME + ".tooltip_details";
	private static final String VALVE_TOOLTIP_KEY         = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.VALVE_NAME + ".tooltip";
	private static final String VALVE_TOOLTIP_DETAILS_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.VALVE_NAME + ".tooltip_details";
	private static final String WRENCH_TOOLTIP_KEY         = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.WRENCH_NAME + ".tooltip";
	private static final String WRENCH_TOOLTIP_DETAILS_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.WRENCH_NAME + ".tooltip_details";

	@Override
	public void onInitializeClient()
	{
		// Rendering
		ModelLoadingPlugin.register(new ModelLoadingWatchdog());

		// Config
		Config.registerClientSideNetworking();
		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
			Config.initialize(SimpleFluidTanks.MOD_ID, SimpleFluidTanks.CONFIG_GUI_TITLE, false, Settings::init);
		});

		// Item tooltips.
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			String TOOLTIP_KEY;
			String TOOLTIP_DETAILS_KEY;
			Object[] formattingArgs = {};
			var mc = Minecraft.getInstance();
			long windowHandle = mc.getWindow().handle();
			int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
			int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);
			boolean isShiftPressed = (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS);

			if (stack.getItem() == ModItems.VALVE)
			{
				TOOLTIP_KEY = VALVE_TOOLTIP_KEY;
				TOOLTIP_DETAILS_KEY = VALVE_TOOLTIP_DETAILS_KEY;
			}
			else if (stack.getItem() == ModItems.TANK)
			{
				TOOLTIP_KEY = TANK_TOOLTIP_KEY;
				TOOLTIP_DETAILS_KEY = TANK_TOOLTIP_DETAILS_KEY;
				formattingArgs = new Object[] { Settings.bucketsPerTank() };
			}
			else if (stack.getItem() == ModItems.WRENCH)
			{
				TOOLTIP_KEY = WRENCH_TOOLTIP_KEY;
				TOOLTIP_DETAILS_KEY = WRENCH_TOOLTIP_DETAILS_KEY;
			}
			else if (stack.getItem() == ModItems.PORTABLE_TANK)
			{
				TOOLTIP_KEY = PORTABLE_TANK_TOOLTIP_KEY;
				TOOLTIP_DETAILS_KEY = PORTABLE_TANK_TOOLTIP_DETAILS_KEY;

				if (isShiftPressed)
				{
					formattingArgs = new Object[]{Settings.bucketsPerPortableTank()};
				}
				else
				{
					FluidContainerComponent component = stack.get(ModComponents.FLUID_CONTAINER_COMPONENT);
					if (component == null) return;

					String fluidName = FluidHelper.getFluidName(component.fluidId());
					formattingArgs = new Object[] {
						fluidName,
						(!fluidName.isEmpty()) ? " " : "",	// Insert a space to make the tooltip look nicer, if getting a name was successful.
						component.fluidId(),
						Utils.getMetricFormattedNumber(component.amount() / FluidStack.BUCKET_VOLUME, "%.1f", "%d", "B"),
						Utils.getMetricFormattedNumber(component.capacity() / FluidStack.BUCKET_VOLUME, "%.1f %s%s", "%d %s", "B"),
						(component.singleBucketMode()) ? Utils.translate(PORTABLE_TANK_TOOLTIP_MODE_SINGLE_BUCKET_KEY) : Utils.translate(PORTABLE_TANK_TOOLTIP_MODE_MAX_KEY)
					};
				}
			}
			else
			{
				return;
			}

			if (isShiftPressed)
			{
				int maxWidth = mc.getWindow().getGuiScaledWidth() / 3;
				lines.addAll(Utils.multiLineTranslateWithMaxWidth(TOOLTIP_DETAILS_KEY, maxWidth, formattingArgs));
			}
			else
			{
				lines.addAll(Utils.multiLineTranslate(TOOLTIP_KEY, formattingArgs));
			}
		});
	}
}
