package net.zarathul.simplefluidtanks.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	public static final BooleanProperty WAS_WRENCHED = BooleanProperty.create("was_wrenched");
	public static final BooleanProperty RERENDER_TRIGGER = BooleanProperty.create("rerender_trigger");

	public TankBlock(ResourceKey<Block> id)
	{
		super(Block.Properties.of()
			.setId(id)
			.strength(Settings.tankBlockHardness(), Settings.tankBlockResistance())
			.sound(SoundType.GLASS)
			.noOcclusion());
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState)
	{
		return new TankBlockEntity(worldPosition, blockState);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState)
	{
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(WAS_WRENCHED, RERENDER_TRIGGER);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return defaultBlockState().setValue(WAS_WRENCHED, false).setValue(RERENDER_TRIGGER, false);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state)
	{
		// Always propagate since there is no more way to check the BlockEntity.
		return true;
//		BlockEntity entity = blockGetter.getBlockEntity(pos);
//		TankBlockEntity tankEntity = (entity != null) ? (TankBlockEntity)entity : null;
//
//		return (tankEntity == null || tankEntity.getFillLevel() == 0);
//		return super.propagatesSkylightDown(state);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		handleDestruction(level, pos, state);
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit)
	{
		handleDestruction(level, pos, state);
		super.onExplosionHit(state, level, pos, explosion, onHit);
	}

	@Override
	protected void handleToolWrenchClick(Level world, BlockPos pos, Player player, ItemStack equippedItemStack)
	{
		// dismantle aka. instantly destroy the tank and drop the
		// appropriate item, telling the connected valve to rebuild in the process
		if (player.isCrouching())
		{
			TankBlockEntity tankEntity = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos);
			if (tankEntity == null)
			{
				SimpleFluidTanks.log.error("Missing TankBlockEntity at {}.", pos.toShortString());
				return;
			}

			ValveBlockEntity valveEntity = null;

			if (tankEntity.isPartOfTank())
			{
				valveEntity = tankEntity.getValve();
				if (valveEntity == null)
				{
					SimpleFluidTanks.log.error("Missing ValveBlockEntity at {}.", tankEntity.getValveCoords().toShortString());
				}

				// set the WAS_WRENCHED property to prevent the multiblock from disbanding
				var state = world.getBlockState(pos);
				world.setBlock(pos, state.setValue(WAS_WRENCHED, true), Block.UPDATE_NONE);
			}

			world.destroyBlock(pos, true);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return simpleCodec(props -> new TankBlock(props.blockId()));
	}

	private void handleDestruction(Level level, BlockPos pos, BlockState state)
	{
		if (!level.isClientSide())
		{
			// Only disband the multiblock if the block was not wrenched.
			if (!state.getValue(WAS_WRENCHED))
			{
				ValveBlockEntity valveEntity = Utils.getValve(level, pos);

				if (valveEntity != null)
				{
					valveEntity.disbandMultiblock(pos);
				}
			}
		}
	}
}
