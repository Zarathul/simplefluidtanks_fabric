package net.zarathul.simplefluidtanks.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * A simple wrench.
 */
public class WrenchItem extends Item
{
	private static final String toolTipKey = "item." + SimpleFluidTanks.MOD_ID + "." + SimpleFluidTanks.WRENCH_ITEM_NAME + ".tooltip";
	private static final String toolTipDetailsKey = "item." + SimpleFluidTanks.MOD_ID + "." + SimpleFluidTanks.WRENCH_ITEM_NAME + ".tooltip_details";

	public WrenchItem()
	{
		super(new Item.Properties()
				.stacksTo(1)
				.tab(SimpleFluidTanks.creativeTab));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag)
	{
		long windowHandle = Minecraft.getInstance().getWindow().getWindow();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			tooltip.addAll(Utils.multiLineTranslate(toolTipDetailsKey));
		}
		else
		{
			tooltip.addAll(Utils.multiLineTranslate(toolTipKey));
		}
	}
}
