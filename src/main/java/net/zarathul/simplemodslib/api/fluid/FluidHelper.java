package net.zarathul.simplemodslib.api.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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

import java.util.HashMap;

public final class FluidHelper
{
	private static final HashMap<Fluid, Item> FLUID_TO_BUCKET = new HashMap<>();
	private static final RandomSource random = RandomSource.create();

	public enum FluidHandlerInteraction
	{
		NONE,
		FILL,
		DRAIN
	}

	public record FluidHandlerInteractionResult(boolean success, FluidHandlerInteraction interaction, FluidStack fluid)
	{
		public static FluidHandlerInteractionResult failure() { return new FluidHandlerInteractionResult(false, FluidHandlerInteraction.NONE, FluidStack.empty()); }
		public static FluidHandlerInteractionResult success(FluidHandlerInteraction interaction, FluidStack fluid) { return new FluidHandlerInteractionResult(true, interaction, fluid); }
	}

	public static FluidHandlerInteractionResult InteractWithFluidHandler(ServerPlayer player, InteractionHand hand, IFluidHandler handler)
	{
		ItemStack items = player.getItemInHand(hand);
		Item heldItem = items.getItem();
		FluidHandlerInteractionResult result;

		if (heldItem == Items.BUCKET)	// empty bucket
		{
			result = fillEmptyBucket(player, hand, handler);
		}
		else if (isBucket(heldItem))
		{
			result = drainBucket(player, hand, handler);
		}
		else if (isFluidContainerItem(heldItem))
		{
			if (player.isCrouching())
			{
				result = fillFluidContainerItem(items, handler);
			}
			else
			{
				result = drainFluidContainerItem(items, handler);
			}
		}
		else
		{
			result = FluidHandlerInteractionResult.failure();
		}

		if (result.success())
		{
			var soundEvent = result.fluid().getFluid().getPickupSound();
			if (soundEvent.isPresent())
			{
				((ServerPlayer)player).connection.send(new ClientboundSoundPacket(
					Holder.direct(soundEvent.get()),
					SoundSource.BLOCKS,
					player.getX(), player.getY(), player.getZ(),
					1.0f, 1.0f, random.nextLong()));
			}
		}

		return result;
	}

	public static boolean isFluidHandler(Level world, BlockPos pos)
	{
		BlockEntity blockEntity = world.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
		return (blockEntity instanceof IFluidHandler);
	}

	public static boolean isFluidContainerItem(ItemStack item)
	{
		return isFluidContainerItem(item.getItem());
	}

	public static boolean isFluidContainerItem(Item item)
	{
		return IFluidContainerItem.class.isAssignableFrom(item.getClass());
	}

	public static String getFluidName(Identifier fluidId)
	{
		if (fluidId == null) return "";

		var registryResult = BuiltInRegistries.FLUID.get(fluidId);
		if (registryResult.isEmpty()) return "";

		Fluid fluid = registryResult.get().value();
		String fluidName = fluid.defaultFluidState().createLegacyBlock().getBlock().getName().getString();

		return fluidName;
	}

	private static FluidHandlerInteractionResult fillEmptyBucket(Player player, InteractionHand hand, IFluidHandler handler)
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

				return FluidHandlerInteractionResult.success(FluidHandlerInteraction.FILL, new FluidStack(handlerFluid.getFluid(), FluidStack.BUCKET_VOLUME));
			}
		}

		return FluidHandlerInteractionResult.failure();
	}

	private static FluidHandlerInteractionResult drainBucket(Player player, InteractionHand hand, IFluidHandler handler)
	{
		BucketItem heldBucket = (BucketItem)player.getItemInHand(hand).getItem();
		Fluid bucketFluid = heldBucket.getContent();
		FluidStack handlerFluid = handler.getFluid();

		// Try to fill one bucket worth of fluid into the handler, if there is enough room. The type of
		// fluid is determined by the bucket. If successful, replace the bucket in the players hand with an empty one.
		if ((handler.getCapacity() - handlerFluid.getAmount()) >= FluidStack.BUCKET_VOLUME)
		{
			FluidStack fillFluid = new FluidStack(bucketFluid, FluidStack.BUCKET_VOLUME);

			if (handler.fill(fillFluid) > 0)
			{
				if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));

				return FluidHandlerInteractionResult.success(FluidHandlerInteraction.DRAIN, fillFluid);
			}
		}

		return FluidHandlerInteractionResult.failure();
	}

	private static FluidHandlerInteractionResult fillFluidContainerItem(ItemStack stack, IFluidHandler handler)
	{
		FluidStack handlerFluid = handler.getFluid();
		IFluidContainerItem heldItem = (IFluidContainerItem)stack.getItem();

		int itemFillAmount = heldItem.fill(stack, handler.getFluid().copy());
		if (itemFillAmount > 0)
		{
			FluidStack fillFluid = new FluidStack(handlerFluid.getFluid(), itemFillAmount);
			handler.drain(fillFluid);

			return FluidHandlerInteractionResult.success(FluidHandlerInteraction.FILL, fillFluid);
		}
		else
		{
			return FluidHandlerInteractionResult.failure();
		}
	}

	private static FluidHandlerInteractionResult drainFluidContainerItem(ItemStack stack, IFluidHandler handler)
	{
		FluidStack handlerFluid = handler.getFluid();
		IFluidContainerItem heldItem = (IFluidContainerItem)stack.getItem();

		int remainingHandlerCapacity = handler.getCapacity() - handlerFluid.getAmount();
		// If the handler is empty, it means it can accept any fluid type. Use the fluid type of the container in that case.
		FluidStack drainableFluid = (handlerFluid.isEmpty()) ? FluidStack.getFluid(stack) : handlerFluid.copy();
		drainableFluid.setAmount(remainingHandlerCapacity);

		FluidStack drainedFluid = heldItem.drain(stack, drainableFluid);
		if (!drainedFluid.isEmpty() && (handler.fill(drainedFluid) > 0))
		{
			 return FluidHandlerInteractionResult.success(FluidHandlerInteraction.DRAIN, drainableFluid);
		}

		return FluidHandlerInteractionResult.failure();
	}

	private static Item getBucketForFluid(Fluid fluid)
	{
		// TODO: Find a better way to do this
		if (FLUID_TO_BUCKET.isEmpty()) BuiltInRegistries.FLUID.forEach(x -> FLUID_TO_BUCKET.put(x, x.getBucket()));

		return FLUID_TO_BUCKET.get(fluid);
	}

	private static boolean isBucket(Item item)
	{
		// TODO: Find a better way to do this
		if (FLUID_TO_BUCKET.isEmpty()) BuiltInRegistries.FLUID.forEach(x -> FLUID_TO_BUCKET.put(x, x.getBucket()));

		return FLUID_TO_BUCKET.containsValue(item);
	}
}
