package net.zarathul.simplemodslib.api.fluid;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.zarathul.simplemodslib.SimpleModsLib;

public abstract class FluidContainerItemBase extends Item implements IFluidContainerItem
{
	protected FluidContainerItemBase(Properties properties, int defaultCapacity)
	{
		super(properties.component(SimpleModsLib.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(0, 16000, FluidStack.empty().getRegistryKey(), false)));
	}

	@Override
	public boolean isBarVisible(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack)
	{
		FluidContainerComponent componentData = stack.get(SimpleModsLib.FLUID_CONTAINER_COMPONENT);
		if (componentData != null)
		{
			int fillLevel = Mth.clamp(Math.round((componentData.amount() / (float)componentData.capacity()) * 13.0f), 0, 13);
			return fillLevel;
		}
		else return super.getBarWidth(stack);
	}
	@Override
	public int getBarColor(ItemStack stack)
	{
		FluidContainerComponent componentData = stack.get(SimpleModsLib.FLUID_CONTAINER_COMPONENT);
		if (componentData != null)
		{
			int capacity = componentData.capacity();
			float freeCapacity = Math.max(0.0F, ((float)capacity - componentData.amount()) / capacity);
			return Mth.hsvToRgb(freeCapacity / 3.0F, 1.0F, 1.0F);
		}
		else return super.getBarColor(stack);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand)
	{
		if (!level.isClientSide() && !player.isCrouching())
		{
			ItemStack itemStack = player.getItemInHand(hand);
			FluidContainerComponent component = itemStack.get(SimpleModsLib.FLUID_CONTAINER_COMPONENT);
			if (component != null)
			{
				// Cycle the fill mode between max, always drain/fill the maximum amount, and bucket, drain/fill one bucket at a time.
				boolean newMode = !component.singleBucketMode();
				itemStack.set(SimpleModsLib.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(component.amount(), component.capacity(), component.fluidId(), newMode));

				return InteractionResult.SUCCESS_SERVER;
			}
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		return InteractionResult.PASS;
	}
}
