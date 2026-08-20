package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.items.ModItems;
import net.zarathul.simplemodslib.api.block.BlockRegistrar;

public final class ModBlocks
{
	private static final BlockRegistrar REGISTRAR = new BlockRegistrar(SimpleFluidTanks.MOD_ID);

	public static final TankBlock TANK = REGISTRAR.register("tank", TankBlock::new);
	public static final ValveBlock VALVE = REGISTRAR.register("valve", ValveBlock::new);

	public static final BlockEntityType<TankBlockEntity> TANK_ENTITY = REGISTRAR.register("tank", TankBlockEntity::new, TANK);
	public static final BlockEntityType<ValveBlockEntity> VALVE_ENTITY = REGISTRAR.register("valve", ValveBlockEntity::new, VALVE);

	public static void init()
	{
		SimpleFluidTanks.LOG.info("Registering blocks.");
	}

	public static InteractionResult useBlockCallback(Player player, Level level, InteractionHand hand, BlockHitResult hit)
	{
		// Necessary for dismantling blocks with the wrench on crouch right-click.
		// Without this WrenchableBlock.use() is never called when crouching.
		if (level.isClientSide() || (!player.isCrouching()) || player.isSpectator()) return InteractionResult.PASS;

		BlockState blockState = level.getBlockState(hit.getBlockPos());
		Block hitBlock = blockState.getBlock();
		ItemStack usedItem = player.getItemInHand(hand);

		if ((usedItem.getItem() == ModItems.WRENCH && hitBlock instanceof WrenchableBlock))
		{
			InteractionResult result = blockState.useItemOn(usedItem, level, player, hand, hit);
			return result;
		}

		return InteractionResult.PASS;
	}
}
