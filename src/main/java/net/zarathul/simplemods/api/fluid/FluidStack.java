package net.zarathul.simplemods.api.fluid;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Objects;

public class FluidStack
{
	public static final int BUCKET_VOLUME = 1000;	// in mB (milli-Buckets)
	private static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
	private static final ResourceLocation EMPTY_FLUID_KEY = Registry.FLUID.getKey(Fluids.EMPTY);

	private Fluid fluid;
	private int amount;
	private ResourceLocation key;

	public FluidStack(Fluid fluid, int amount)
	{
		this(fluid, amount, Registry.FLUID.getKey(fluid));
	}

	private FluidStack(Fluid fluid, int amount, ResourceLocation key)
	{
		this.fluid  = fluid;
		this.amount = amount;
		this.key    = key;
	}

	private FluidStack()
	{
	}

	public FluidStack copy()
	{
		return new FluidStack(fluid, amount, key);
	}

	public Fluid getFluid()
	{
		return fluid;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public void changeAmount(int delta)
	{
		this.amount += delta;
	}

	public ResourceLocation getRegistryKey()
	{
		return key;
	}

	public boolean isEmpty()
	{
		return ((this == EMPTY) || (amount <= 0));
	}

	public boolean isSameFluid(FluidStack other)
	{
		return (other.fluid.isSame(this.fluid));
	}

	public static FluidStack empty()
	{
		return EMPTY.copy();
	}

	private static final String TAG_FLUID_KEY = "fluid_key";
	private static final String TAG_FLUID_AMOUNT = "fluid_amount";

	public CompoundTag save(CompoundTag tag)
	{
		tag.putInt(TAG_FLUID_AMOUNT, amount);

		ResourceLocation fluidKey = (amount == 0) ? EMPTY_FLUID_KEY : key;
		tag.putString(TAG_FLUID_KEY, fluidKey.toString());

		return tag;
	}

	public void load(BlockState state, CompoundTag tag)
	{
		amount = tag.getInt(TAG_FLUID_AMOUNT);
		key    = (!tag.contains(TAG_FLUID_KEY) || (amount == 0)) ? EMPTY_FLUID_KEY : new ResourceLocation(tag.getString(TAG_FLUID_KEY));
		fluid  = Registry.FLUID.get(key);
	}

	public static FluidStack load(CompoundTag tag)
	{
		FluidStack stack = new FluidStack();
		stack.load(null, tag);

		return stack;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof FluidStack)) return false;

		FluidStack otherStack = (FluidStack)other;
		return (otherStack.getFluid().isSame(fluid) && (otherStack.getAmount() == amount));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(fluid, amount);
	}
}
