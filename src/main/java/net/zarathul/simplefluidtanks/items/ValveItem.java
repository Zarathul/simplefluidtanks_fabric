package net.zarathul.simplefluidtanks.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.zarathul.simplefluidtanks.BlocksAndItems;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplemodslib.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;


/**
 * {@link ValveBlock} in item form.
 */
public class ValveItem extends BlockItem
{
	private static final String toolTipKey = "item." + SimpleFluidTanks.MOD_ID + "." + BlocksAndItems.VALVE_ITEM_NAME + ".tooltip";
	private static final String toolTipDetailsKey = "item." + SimpleFluidTanks.MOD_ID + "." + BlocksAndItems.VALVE_ITEM_NAME + ".tooltip_details";

	public ValveItem(ResourceKey<Item> id)
	{
		super(BlocksAndItems.blockValve, new Item.Properties().setId(id).stacksTo(64));
	}

	@Environment(EnvType.CLIENT)
	public void addTooltip(ItemStack stack, TooltipContext context, TooltipFlag tooltipFlag, List<Component> tooltip)
	{
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			tooltip.addAll(Utils.multiLineTranslate(toolTipDetailsKey));
		}
		else
		{
			tooltip.add(Component.translatable(toolTipKey));
		}
	}
}
