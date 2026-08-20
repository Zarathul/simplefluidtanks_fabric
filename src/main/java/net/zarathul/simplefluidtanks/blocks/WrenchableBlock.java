package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zarathul.simplefluidtanks.BlocksAndItems;

/**
 * A base class for blocks that have custom behavior when a wrench is used on them.
 */
public abstract class WrenchableBlock extends BaseEntityBlock
{
	protected WrenchableBlock(Block.Properties props)
	{
		super(props);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		ItemStack heldStack = player.getItemInHand(hand);

		if (!heldStack.isEmpty() && BlocksAndItems.isWrenchItem(heldStack.getItem()))
		{
			if (!level.isClientSide())
			{
				handleToolWrenchClick(level, pos, player, heldStack);
			}

			return InteractionResult.SUCCESS;
		}

		return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
	}

	/**
	 * Handles clicks with wrenches on the block.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player using the item.
	 * @param equippedItemStack
	 * The item(stack) used on the block.
	 */
	protected abstract void handleToolWrenchClick(Level world, BlockPos pos, Player player, ItemStack equippedItemStack);
}
