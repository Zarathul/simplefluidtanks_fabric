package net.zarathul.simplemods.api.fluid;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FluidApi
{
	public static DataComponentType<FluidContainerComponent> FLUID_CONTAINER_COMPONENT;
	public static final String SIMPLE_MODS_ID = "simplemods";

	private FluidApi() {}

	static
	{
		FluidApi.FLUID_CONTAINER_COMPONENT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(SIMPLE_MODS_ID, "fluid_container"),
			DataComponentType.<FluidContainerComponent>builder().persistent(FluidContainerComponent.CODEC).build());
	}

	public static void initialize()
	{
		Logger log = LogManager.getLogger(SIMPLE_MODS_ID);
		log.info("Initializing Fluid-Api.");
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
}
