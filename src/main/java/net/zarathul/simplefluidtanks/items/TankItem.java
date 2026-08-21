package net.zarathul.simplefluidtanks.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.zarathul.simplefluidtanks.blocks.TankBlock;

/**
 * {@link TankBlock} in item form.
 */
public class TankItem extends BlockItem
{
	public TankItem(Block block, Properties properties)
	{
		super(block, properties.stacksTo(64));
	}
}