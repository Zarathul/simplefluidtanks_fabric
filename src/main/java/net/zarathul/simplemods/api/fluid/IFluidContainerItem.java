package net.zarathul.simplemods.api.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IFluidContainerItem
{
	String CONTAINER_TAG_NAME = "fluid_container_item";

	default
	FluidStack getFluid(ItemStack stack)
	{
		CompoundTag tag = stack.getTagElement(CONTAINER_TAG_NAME);
		if (tag == null) return FluidStack.empty();

		FluidStack fluid = FluidStack.load(tag);

		return fluid;
	}

	int getCapacity();

	default
	FluidStack drain(ItemStack stack, FluidStack drainFluid)
	{
		FluidStack fluid = getFluid(stack);
		int drainAmount = drainFluid.getAmount();

		if (fluid.isEmpty() || !fluid.isSameFluid(drainFluid) || (drainAmount <= 0)) return FluidStack.empty();

		FluidStack drainedFluid = fluid.copy();
		drainedFluid.setAmount(Math.min(fluid.getAmount(), drainAmount));
		fluid.changeAmount(-drainedFluid.getAmount());

		CompoundTag tag = stack.getOrCreateTagElement(CONTAINER_TAG_NAME);
		fluid.save(tag);

		// Update damage value, which is used to display the fill level of the tank.
		// Because the damage indicator is hidden if the item is undamaged, always set
		// the damage to 1 instead of 0 so that it remains visible.
		int damage = getCapacity() - fluid.getAmount();
		damage = Math.max(damage, 1);
		stack.setDamageValue(damage);

		return drainedFluid;
	}

	default
	int fill(ItemStack stack, FluidStack fillFluid)
	{
		if (fillFluid.isEmpty()) return 0;

		FluidStack fluid = getFluid(stack);
		int capacity = getCapacity();

		if (fluid.isEmpty())
		{
			CompoundTag tag = stack.getOrCreateTagElement(CONTAINER_TAG_NAME);
			fluid = fillFluid.copy();
			// limit the stored fluid to the tanks capacity
			if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), capacity));
			fluid.save(tag);

			// See comment in drain().
			int damage = capacity - fluid.getAmount();
			damage = Math.max(damage, 1);
			stack.setDamageValue(damage);

			return fluid.getAmount();
		}

		if (!fluid.isSameFluid(fillFluid)) return 0;

		int remainingCapacity = capacity - fluid.getAmount();
		int fillAmount = Math.min(remainingCapacity, fillFluid.getAmount());
		if (fillAmount > 0)
		{
			fluid.changeAmount(fillAmount);
			CompoundTag tag = stack.getOrCreateTagElement(CONTAINER_TAG_NAME);
			fluid.save(tag);

			// See comment in drain().
			int damage = capacity - fluid.getAmount();
			damage = Math.max(damage, 1);
			stack.setDamageValue(damage);
		}

		return fillAmount;
	}
}
