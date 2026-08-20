package net.zarathul.simplefluidtanks.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplemodslib.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;


/**
 * {@link TankBlock} in item form.
 */
public class TankItem extends BlockItem
{
	private static final String TOOLTIP_KEY         = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.TANK_NAME + ".tooltip";
	private static final String TOOLTIP_DETAILS_KEY = "item." + SimpleFluidTanks.MOD_ID + "." + ModItems.TANK_NAME + ".tooltip_details";

	public TankItem(Block block, Properties properties)
	{
		super(block, properties.stacksTo(64));
	}

	@Environment(EnvType.CLIENT)
	public void addTooltip(ItemStack stack, TooltipContext context, TooltipFlag tooltipFlag, List<Component> tooltip)
	{
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			tooltip.addAll(Utils.multiLineTranslate(TOOLTIP_DETAILS_KEY, Settings.bucketsPerTank()));
		}
		else
		{
			tooltip.add(Component.translatable(TOOLTIP_KEY));
		}
	}
}