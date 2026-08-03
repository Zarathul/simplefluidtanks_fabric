package net.zarathul.simplemods.api.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

public interface IFluidContainerItem
{
	default
	FluidStack getFluid(ItemStack stack)
	{
		var component = stack.get(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT);
		if (component == null) return FluidStack.empty();

		FluidStack fluid = FluidStack.from(component);

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

		stack.set(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(fluid.getAmount(), getCapacity(), fluid.getRegistryKey()));

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
			fluid = fillFluid.copy();
			// limit the stored fluid to the tanks capacity
			if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), capacity));
			stack.set(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(fluid.getAmount(), capacity, fluid.getRegistryKey()));

			return fluid.getAmount();
		}

		if (!fluid.isSameFluid(fillFluid)) return 0;

		int remainingCapacity = capacity - fluid.getAmount();
		int fillAmount = Math.min(remainingCapacity, fillFluid.getAmount());
		if (fillAmount > 0)
		{
			fluid.changeAmount(fillAmount);
			stack.set(SimpleFluidTanks.FLUID_CONTAINER_COMPONENT, new FluidContainerComponent(fluid.getAmount(), capacity, fluid.getRegistryKey()));
		}

		return fillAmount;
	}
}
