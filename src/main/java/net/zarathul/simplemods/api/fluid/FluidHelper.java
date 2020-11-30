package net.zarathul.simplemods.api.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.zarathul.simplefluidtanks.mixin.BucketItemAccessor;

import java.util.HashMap;

public class FluidHelper
{
	private static final HashMap<Fluid, Item> FLUID_TO_BUCKET = new HashMap<>();

	public enum FluidHandlerInteraction
	{
		none,
		fill,
		drain
	}

	public static class FluidHandlerInteractionResult
	{
		private final boolean success;
		private final FluidHandlerInteraction interaction;

		public FluidHandlerInteractionResult(boolean success, FluidHandlerInteraction interaction)
		{
			this.success = success;
			this.interaction = interaction;
		}

		public boolean isSuccess()
		{
			return success;
		}

		public FluidHandlerInteraction getInteraction()
		{
			return interaction;
		}
	}

	public static FluidHandlerInteractionResult InteractWithFluidHandler(Player player, InteractionHand hand, IFluidHandler handler)
	{
		ItemStack items = player.getItemInHand(hand);
		Item heldItem = items.getItem();

		if (heldItem == Items.BUCKET)	// empty bucket
		{
			return new FluidHandlerInteractionResult(fillEmptyBucket(player, hand, handler), FluidHandlerInteraction.fill);
		}
		else if (isBucket(heldItem))
		{
			return new FluidHandlerInteractionResult(drainBucket(player, hand, handler), FluidHandlerInteraction.drain);
		}
		else if (isFluidContainerItem(heldItem))
		{
			if (player.isCrouching())
			{
				return new FluidHandlerInteractionResult(fillFluidContainerItem(items, handler), FluidHandlerInteraction.fill);
			}
			else
			{
				return new FluidHandlerInteractionResult(drainFluidContainerItem(items, handler), FluidHandlerInteraction.drain);
			}
		}

		return new FluidHandlerInteractionResult(false, FluidHandlerInteraction.none);
	}

	public static boolean isFluidHandler(Level world, BlockPos pos)
	{
		BlockEntity tile = world.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
		return (tile instanceof IFluidHandler);
	}

	public static boolean isFluidContainerItem(ItemStack item)
	{
		return isFluidContainerItem(item.getItem());
	}

	public static boolean isFluidContainerItem(Item item)
	{
		return IFluidContainerItem.class.isAssignableFrom(item.getClass());
	}

	private static boolean fillEmptyBucket(Player player, InteractionHand hand, IFluidHandler handler)
	{
		FluidStack handlerFluid = handler.getFluid();

		// If the fluid handler has one bucket worth of fluid, drain it and replace
		// the empty bucket in the players hand with a filled one of the correct type.
		if ((handlerFluid.getAmount() >= FluidStack.BUCKET_VOLUME))
		{
			if (!handler.drain(new FluidStack(handlerFluid.getFluid(), FluidStack.BUCKET_VOLUME)).isEmpty() && !player.isCreative())
			{
				Item bucket = getBucketForFluid(handlerFluid.getFluid());
				player.setItemInHand(hand, new ItemStack(bucket));
				return true;
			}
		}

		return false;
	}

	private static boolean drainBucket(Player player, InteractionHand hand, IFluidHandler handler)
	{
		BucketItem heldBucket = (BucketItem)player.getItemInHand(hand).getItem();
		Fluid bucketFluid = ((BucketItemAccessor)heldBucket).getContents();
		FluidStack handlerFluid = handler.getFluid();

		// Try to fill one bucket worth of fluid into the handler, if there is enough room. The type of
		// fluid is determined by the bucket. If successful, replace the bucket in the players hand with an empty one.
		if ((handler.getCapacity() - handlerFluid.getAmount()) >= FluidStack.BUCKET_VOLUME)
		{
			if (handler.fill(new FluidStack(bucketFluid, FluidStack.BUCKET_VOLUME)) > 0)
			{
				if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				return true;
			}
		}

		return false;
	}

	private static boolean fillFluidContainerItem(ItemStack stack, IFluidHandler handler)
	{
		FluidStack handlerFluid = handler.getFluid();

		// Try to fill the item from the container either if the item is empty to begin with, or if
		// the item and the container contain the same type of fluid.
		if (!handlerFluid.isEmpty())
		{
			IFluidContainerItem heldItem = (IFluidContainerItem)stack.getItem();
			FluidStack heldItemFluid = heldItem.getFluid(stack);

			if (heldItemFluid.isEmpty() || (heldItemFluid.isSameFluid(handlerFluid)))
			{
				int remainingItemCapacity = heldItem.getCapacity() - heldItemFluid.getAmount();
				FluidStack drainedFluid = handler.drain(new FluidStack(handlerFluid.getFluid(), remainingItemCapacity));

				if (!drainedFluid.isEmpty())
				{
					return (heldItem.fill(stack, drainedFluid) > 0);
				}
			}
		}

		return false;
	}

	private static boolean drainFluidContainerItem(ItemStack stack, IFluidHandler handler)
	{
		FluidStack handlerFluid = handler.getFluid();
		IFluidContainerItem heldItem = (IFluidContainerItem)stack.getItem();
		FluidStack heldItemFluid = heldItem.getFluid(stack);

		// Try to drain the item into the container either if the container is empty to begin with, or if
		// the container and the item contain the same type of fluid.
		if (!heldItemFluid.isEmpty() && (handlerFluid.isEmpty() || heldItemFluid.isSameFluid(handlerFluid)))
		{
			int remainingHandlerCapacity = handler.getCapacity() - handlerFluid.getAmount();
			FluidStack drainedFluid = heldItem.drain(stack, new FluidStack(heldItemFluid.getFluid(), remainingHandlerCapacity));

			if (!drainedFluid.isEmpty())
			{
				return (handler.fill(drainedFluid) > 0);
			}
		}

		return false;
	}

	private static Item getBucketForFluid(Fluid fluid)
	{
		// TODO: Find a better way to do this
		if (FLUID_TO_BUCKET.isEmpty()) Registry.FLUID.forEach(x -> FLUID_TO_BUCKET.put(x, x.getBucket()));

		return FLUID_TO_BUCKET.get(fluid);
	}

	private static boolean isBucket(Item item)
	{
		// TODO: Find a better way to do this
		if (FLUID_TO_BUCKET.isEmpty()) Registry.FLUID.forEach(x -> FLUID_TO_BUCKET.put(x, x.getBucket()));

		return FLUID_TO_BUCKET.containsValue(item);
	}
}
