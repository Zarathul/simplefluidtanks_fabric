package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	private final HashSet<BlockPos> ignoreBlockBreakCoords;

	public TankBlock()
	{
		super(Block.Properties.of(SimpleFluidTanks.tankMaterial)
				.strength(Settings.tankBlockHardness, Settings.tankBlockResistance)
				.sound(SoundType.GLASS)
				.noOcclusion());

		ignoreBlockBreakCoords = new HashSet<BlockPos>();
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockGetter blockGetter)
	{
		return new TankBlockEntity();
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState)
	{
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter blockGetter, BlockPos pos)
	{
		BlockEntity entity = blockGetter.getBlockEntity(pos);
		TankBlockEntity tankEntity = (entity != null) ? (TankBlockEntity)entity : null;

		return (tankEntity == null || tankEntity.getFillLevel() == 0);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!world.isClientSide())
		{
			if (newState.getBlock() != SimpleFluidTanks.blockTank)
			{
				// ignore the event if the tanks coordinates are on the ignore list
				if (ignoreBlockBreakCoords.contains(pos))
				{
					ignoreBlockBreakCoords.remove(pos);
				}
				else
				{
					// get the valve the tank is connected to and disband the multiblock
					ValveBlockEntity valveEntity = Utils.getValve(world, pos);

					if (valveEntity != null)
					{
						valveEntity.disbandMultiblock(pos);
					}
				}
			}
		}

		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	protected void handleToolWrenchClick(Level world, BlockPos pos, Player player, ItemStack equippedItemStack)
	{
		// dismantle aka. instantly destroy the tank and drop the
		// appropriate item, telling the connected valve to rebuild in the process
		if (player.isCrouching())
		{
			TankBlockEntity tankEntity = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos);
			ValveBlockEntity valveEntity = null;

			if (tankEntity != null && tankEntity.isPartOfTank())
			{
				valveEntity = tankEntity.getValve();
				// ignore the BlockBreak event for this TankBlock, this way
				// there will be no reset of the whole tank
				ignoreBlockBreakCoords.add(pos.immutable());
			}

			world.destroyBlock(pos, true);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}
}
