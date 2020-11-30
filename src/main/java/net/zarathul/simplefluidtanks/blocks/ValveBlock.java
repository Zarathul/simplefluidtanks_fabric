package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplemods.api.fluid.FluidHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a valve in the mods multiblock structure.
 */
public class ValveBlock extends WrenchableBlock
{
	public ValveBlock()
	{
		super(Block.Properties.of(SimpleFluidTanks.tankMaterial)
				.strength(Settings.valveBlockHardness, Settings.valveBlockResistance)
				.sound(SoundType.METAL));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockGetter blockGetter)
	{
		return new ValveBlockEntity();
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState)
	{
		return false;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack items)
	{
		if (!level.isClientSide() && (placer != null))
		{
			Direction facing = placer.getDirection().getOpposite();

			ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);

			if (valveEntity != null)
			{
				//level.setBlockEntity(pos, valveEntity);
				valveEntity.setFacing(facing);
				valveEntity.formMultiblock();
				level.getChunkAt(pos).markUnsaved();
			}
		}

		super.setPlacedBy(level, pos, state, placer, items);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (!world.isClientSide())
		{
			ValveBlockEntity valveEntity = Utils.getBlockEntityAt(world, ValveBlockEntity.class, pos);

			if (valveEntity != null)
			{
				FluidHelper.FluidHandlerInteractionResult result = FluidHelper.InteractWithFluidHandler(player, hand, valveEntity);
				if (result.isSuccess())
				{
					Fluid fluid = valveEntity.getFluid().getFluid();
					SoundEvent soundevent = (fluid == Fluids.LAVA) ?
											(result.getInteraction() == FluidHelper.FluidHandlerInteraction.drain) ?
											SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_FILL_LAVA :
											(result.getInteraction() == FluidHelper.FluidHandlerInteraction.drain) ?
											SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL;

					((ServerPlayer)player).connection.send(new ClientboundSoundPacket(
							soundevent,
							SoundSource.BLOCKS,
							player.getX(), player.getY(), player.getZ(),
							1.0f, 1.0f));
				}
			}
		}

		if (FluidHelper.isFluidContainerItem(player.getItemInHand(hand))) return InteractionResult.SUCCESS;

		return super.use(state, world, pos, player, hand, hit);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState)
	{
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
	{
		ValveBlockEntity valveEntity = Utils.getBlockEntityAt(world, ValveBlockEntity.class, pos);

		if (valveEntity != null)
		{
			float fluidAmount = valveEntity.getFluidAmount();
			float capacity = valveEntity.getCapacity();
			int signalStrength = Utils.getComparatorLevel(fluidAmount, capacity);

			return signalStrength;
		}

		return 0;
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!world.isClientSide())
		{
			if (newState.getBlock() != SimpleFluidTanks.blockValve)
			{
				// disband the multiblock if the valve is mined/destroyed
				ValveBlockEntity valveEntity = Utils.getBlockEntityAt(world, ValveBlockEntity.class, pos);

				if (valveEntity != null)
				{
					valveEntity.disbandMultiblock(pos);
				}
			}
		}

		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	protected void handleToolWrenchClick(Level world, BlockPos pos, Player player, ItemStack equippedItemStack)
	{
		// On sneak use: disband the multiblock | On use: rebuild the multiblock

		ValveBlockEntity valveEntity = Utils.getBlockEntityAt(world, ValveBlockEntity.class, pos);

		if (player.isCrouching())
		{
			if (valveEntity != null)
			{
				valveEntity.disbandMultiblock(pos);
			}

			world.destroyBlock(pos, true);
		}
		else if (valveEntity != null)
		{
			// rebuild the tank
			valveEntity.formMultiblock();
		}
	}
}
