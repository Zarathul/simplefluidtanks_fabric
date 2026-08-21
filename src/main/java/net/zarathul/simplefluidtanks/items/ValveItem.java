package net.zarathul.simplefluidtanks.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;

/**
 * {@link ValveBlock} in item form.
 */
public class ValveItem extends BlockItem
{
	public ValveItem(Block block, Properties properties)
	{
		super(block, properties.stacksTo(64));
	}
}
