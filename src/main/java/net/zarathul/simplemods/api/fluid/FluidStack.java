package net.zarathul.simplemods.api.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Objects;

public class FluidStack
{
	public static final int BUCKET_VOLUME = 1000;	// in mB (milli-Buckets)
	private static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
	private static final Identifier EMPTY_FLUID_KEY = BuiltInRegistries.FLUID.getDefaultKey();

	private Fluid fluid;
	private int amount;
	private Identifier key;

	public FluidStack(Fluid fluid, int amount)
	{
		this(fluid, amount, BuiltInRegistries.FLUID.getKey(fluid));
	}

	private FluidStack(Fluid fluid, int amount, Identifier key)
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

	public Identifier getRegistryKey()
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

	private static final String FLUID_KEY = "fluid_key";
	private static final String FLUID_AMOUNT = "fluid_amount";

	public void save(ValueOutput output)
	{
		output.putInt(FLUID_AMOUNT, amount);
		output.putString(FLUID_KEY, key.toString());
	}

	public void load(ValueInput input)
	{
		amount = input.getInt(FLUID_AMOUNT).get();
		key    = Identifier.parse(input.getString(FLUID_KEY).get());
		fluid  = BuiltInRegistries.FLUID.get(key).get().value();
	}

	public static FluidStack from(FluidContainerComponent input)
	{
		var amount = input.amount();
		var key    = (amount == 0) ? EMPTY_FLUID_KEY : input.fluid();
		var fluid  = BuiltInRegistries.FLUID.get(key).get().value();	// TODO: maybe store Reference<Fluid> ?

		FluidStack stack = new FluidStack(fluid, amount, key);

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
