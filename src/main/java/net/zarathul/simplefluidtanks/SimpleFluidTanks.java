package net.zarathul.simplefluidtanks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.blocks.WrenchableBlock;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.items.PortableTankItem;
import net.zarathul.simplefluidtanks.items.TankItem;
import net.zarathul.simplefluidtanks.items.ValveItem;
import net.zarathul.simplefluidtanks.items.WrenchItem;
import net.zarathul.simplemods.api.fluid.FluidHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class SimpleFluidTanks implements ModInitializer
{
	// constants
	public static final String MOD_ID = "simplefluidtanks";
	public static final String SIMPLE_MODS_ID = "simplemods";
	// This MUST be 16 to get the correct UV coordinates during rendering, because 16 is hardcoded into the interpolation method.
	public static final int MAX_FILL_LEVEL = 16;

	// block, item and tileentity names
	public static final String TANK_BLOCK_NAME = "tank";
	public static final String VALVE_BLOCK_NAME = "valve";

	public static final String TANK_ITEM_NAME = "tank";
	public static final String VALVE_ITEM_NAME = "valve";

	public static final String WRENCH_ITEM_NAME = "wrench";
	public static final String PORTABLE_TANK_ITEM_NAME = "portable_tank";

	public static final String TANKBLOCK_ENTITY_NAME = "tank";
	public static final String VALVEBLOCK_ENTITY_NAME = "valve";

	// blocks
	public static final Material tankMaterial;
	public static final TankBlock blockTank;
	public static final ValveBlock blockValve;

	// tileEntities
	public static BlockEntityType<TankBlockEntity> entityTank;
	public static BlockEntityType<ValveBlockEntity> entityValve;

	// creative tabs
	public final static CreativeModeTab creativeTab;

	// items
	public static final TankItem itemTank;
	public static final ValveItem itemValve;
	public static final WrenchItem itemWrench;
	public static final PortableTankItem itemPortableTank;

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	static
	{
		// Note: Order is important here. Both blocks and some of the icons are dependent on config settings, also
		// the creative tab is dependent on the valve block for the icon,

		// Load or create config file.
		Config.loadOrCreate(MOD_ID, Settings.class);

		tankMaterial = new Material(MaterialColor.NONE, false, true, true, false, false, false, PushReaction.BLOCK);

		blockTank = new TankBlock();
		blockValve = new ValveBlock();

		creativeTab = MakeCreativeTab();

		itemTank = new TankItem();
		itemValve = new ValveItem();
		itemPortableTank = new PortableTankItem();
		itemWrench = new WrenchItem();
	}

	@Override
	public void onInitialize()
	{
		// Register Blocks & Items.
		Registry.register(Registry.BLOCK, new ResourceLocation(MOD_ID, TANK_BLOCK_NAME), blockTank);
		entityTank = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(MOD_ID, TANKBLOCK_ENTITY_NAME), BlockEntityType.Builder.of(TankBlockEntity::new, blockTank).build(null));
		Registry.register(Registry.ITEM, new ResourceLocation(MOD_ID, TANK_ITEM_NAME), itemTank);

		Registry.register(Registry.BLOCK, new ResourceLocation(MOD_ID, VALVE_BLOCK_NAME), blockValve);
		entityValve = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(MOD_ID, VALVEBLOCK_ENTITY_NAME), BlockEntityType.Builder.of(ValveBlockEntity::new, blockValve).build(null));
		Registry.register(Registry.ITEM, new ResourceLocation(MOD_ID, VALVE_ITEM_NAME), itemValve);

		Registry.register(Registry.ITEM, new ResourceLocation(MOD_ID, WRENCH_ITEM_NAME), itemWrench);
		Registry.register(Registry.ITEM, new ResourceLocation(MOD_ID, PORTABLE_TANK_ITEM_NAME), itemPortableTank);

		// Necessary for dismantling blocks with the wrench on sneak right-click.
		// Without this WrenchableBlock.use() is never called when sneaking.
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (world.isClientSide() || (!player.isShiftKeyDown()) || player.isSpectator()) return InteractionResult.PASS;

			BlockState blockState = world.getBlockState(hit.getBlockPos());
			Block hitBlock = blockState.getBlock();
			Item usedItem = player.getItemInHand(hand).getItem();

			if (((usedItem == itemWrench) && ((hitBlock instanceof WrenchableBlock))) ||
				((usedItem == itemPortableTank) && (FluidHelper.isFluidHandler(world, hit.getBlockPos()))))
			{
				InteractionResult result = blockState.use(world, player, hand, hit);
				return result;
			}

			return  InteractionResult.PASS;
		});

		// Prevent buckets from doing their usual thing when right clicking a valve.
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (player.isSpectator()) return InteractionResultHolder.pass(player.getItemInHand(hand));

			ItemStack items = player.getItemInHand(hand);
			if ((items.getItem() instanceof BucketItem))
			{
				BlockHitResult hit = Utils.getPlayerPOVHitResult(world, player);
				if (hit.getType() == HitResult.Type.BLOCK)
				{
					BlockState state = world.getBlockState(hit.getBlockPos());
					if ((state != null) && (state.getBlock() == blockValve))
					{
						return InteractionResultHolder.success(items);
					}
				}
			}

			return InteractionResultHolder.pass(player.getItemInHand(hand));
		});
	}

	private static CreativeModeTab MakeCreativeTab()
	{
		// Checks if a "Simple Mods" tab already exists, otherwise makes one.
		return Arrays.stream(CreativeModeTab.TABS)
			.filter(tab -> tab.getRecipeFolderName().equals(SIMPLE_MODS_ID))
			.findFirst()
			.orElseGet(() -> FabricItemGroupBuilder.build(
					new ResourceLocation(MOD_ID, SIMPLE_MODS_ID),
					() -> new ItemStack(blockValve)).setRecipeFolderName(SIMPLE_MODS_ID)
			);
	}
}