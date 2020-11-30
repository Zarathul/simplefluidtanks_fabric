package net.zarathul.simplemods.api.fluid;


public interface IFluidHandler
{
	/**
	 * Get the fluid inside the container.
	 *
	 * @return
	 * A {@link FluidStack} or {@link FluidStack#empty()} for empty containers, never <c>null</c>.
	 */
	FluidStack getFluid();

	/**
	 * Get the total capacity the container can hold.
	 *
	 * @return
	 * A value of <c>0</c> or greater.
	 */
	int getCapacity();

	/**
	 * Tries to drain the specified amount of the specified fluid type from the container.
	 * It is assumed that if the container has the amount of fluid requested, it can be drained in one go.
	 *
	 * @param fluid
	 * The type and amount of fluid to drain from the container. Must not be <c>null</c>.
	 * If this amount is bigger than the amount of fluid actually container in the container, the draining will
	 * still succeed.
	 * @return
	 * The actually drained fluid or {@link FluidStack#empty()} in case of failure, never <c>null</c>.
	 */
	FluidStack drain(FluidStack fluid);

	/**
	 * Tries to fill the specified amount of the specified fluid type into the container.
	 * It is assumed that if the container has enough remaining capacity, it can be filled up to that amount in one go.
	 *
	 * @param fluid
	 * The type and amount of fluid to fill into the container. Must not be <c>null</c>.
	 * If this amount is bigger than the remaining capacity of the container, the filling will still succeed.
	 * @return
	 * <c>0</c> on failure.
	 */
	int fill(FluidStack fluid);
}
