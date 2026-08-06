package net.zarathul.simplefluidtanks.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplemods.api.fluid.FluidHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Represents a valve in the mods multiblock structure.
 */
public class ValveBlock extends WrenchableBlock
{
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
	public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");
	public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
	public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
	public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");
	public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
	public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");

	public ValveBlock(ResourceKey<Block> id)
	{
		super(Block.Properties.of()
			.setId(id)
			.strength(Settings.valveBlockHardness(), Settings.valveBlockResistance())
			.sound(SoundType.METAL));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		return new ValveBlockEntity(worldPosition, blockState);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState)
	{
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(
			FACING,
			CONNECTED,
			CONNECTED_DOWN,
			CONNECTED_UP,
			CONNECTED_NORTH,
			CONNECTED_SOUTH,
			CONNECTED_WEST,
			CONNECTED_EAST
		);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState()
			.setValue(FACING, context.getHorizontalDirection().getOpposite())
			.setValue(CONNECTED,       false)
			.setValue(CONNECTED_DOWN,  false)
			.setValue(CONNECTED_UP,    false)
			.setValue(CONNECTED_NORTH, false)
			.setValue(CONNECTED_SOUTH, false)
			.setValue(CONNECTED_WEST,  false)
			.setValue(CONNECTED_EAST,  false);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack items)
	{
		if (!level.isClientSide())
		{
			ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
			else
			{
				SimpleFluidTanks.log.error("Missing ValveBlockEntity at {}", pos.toShortString());
			}
		}

		super.setPlacedBy(level, pos, state, placer, items);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if (!level.isClientSide())
		{
			ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);

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
						Holder.direct(soundevent),
						SoundSource.BLOCKS,
						player.getX(), player.getY(), player.getZ(),
						1.0f, 1.0f, level.getRandom().nextLong()));
				}
			}
			else
			{
				SimpleFluidTanks.log.error("Missing ValveBlockEntity at {}", pos.toShortString());
			}
		}

		if (FluidHelper.isFluidContainerItem(player.getItemInHand(hand))) return InteractionResult.SUCCESS;

		return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState)
	{
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction)
	{
		ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);

		if (valveEntity != null)
		{
			float fluidAmount = valveEntity.getFluidAmount();
			float capacity = valveEntity.getCapacity();
			int signalStrength = Utils.getComparatorLevel(fluidAmount, capacity);

			return signalStrength;
		}

		SimpleFluidTanks.log.error("Missing ValveBlockEntity at {}", pos.toShortString());
		return 0;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		handleDestruction(level, pos, state);
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state)
	{
		var e = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);
		super.destroy(level, pos, state);
	}

	@Override
	protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit)
	{
		handleDestruction(level, pos, state);
		super.onExplosionHit(state, level, pos, explosion, onHit);
	}

	@Override
	protected void handleToolWrenchClick(Level level, BlockPos pos, Player player, ItemStack equippedItemStack)
	{
		// On sneak use: disband the multiblock | On use: rebuild the multiblock

		ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);
		if (valveEntity == null)
		{
			SimpleFluidTanks.log.error("Missing ValveBlockEntity at {}", pos.toShortString());
			return;
		}

		if (player.isCrouching())
		{
			valveEntity.disbandMultiblock(pos);
			level.destroyBlock(pos, true);
		}
		else
		{
			// rebuild the tank
			valveEntity.formMultiblock();
		}
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return simpleCodec(props -> new ValveBlock(props.blockId()));
	}

	private void handleDestruction(Level level, BlockPos pos, BlockState state)
	{
		if (!level.isClientSide())
		{
			ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, pos);

			if (valveEntity == null)
			{
				SimpleFluidTanks.log.error("Missing ValveBlockEntity at {}", pos.toShortString());
				return;
			}

			valveEntity.disbandMultiblock(pos);
		}
	}

	public static BlockState getStateFromEntity(ValveBlockEntity blockEntity, BlockState oldState)
	{
		return oldState
			.setValue(CONNECTED, true)
			.setValue(CONNECTED_NORTH, blockEntity.isFacingTank(Direction.NORTH))
			.setValue(CONNECTED_SOUTH, blockEntity.isFacingTank(Direction.SOUTH))
			.setValue(CONNECTED_EAST,  blockEntity.isFacingTank(Direction.EAST))
			.setValue(CONNECTED_WEST,  blockEntity.isFacingTank(Direction.WEST))
			.setValue(CONNECTED_DOWN,  blockEntity.isFacingTank(Direction.DOWN))
			.setValue(CONNECTED_UP,    blockEntity.isFacingTank(Direction.UP));
	}

	public static BlockState getDisconnectedState(BlockState oldState)
	{
		return oldState
			.setValue(CONNECTED,       false)
			.setValue(CONNECTED_NORTH, false)
			.setValue(CONNECTED_SOUTH, false)
			.setValue(CONNECTED_EAST,  false)
			.setValue(CONNECTED_WEST,  false)
			.setValue(CONNECTED_DOWN,  false)
			.setValue(CONNECTED_UP,    false);
	}
}
