package net.zarathul.simplemodslib;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.zarathul.simplemodslib.api.fluid.FluidContainerComponent;

public class SimpleModsLib implements ModInitializer
{
	public static final String MOD_ID = "simplemodslib";
	public static DataComponentType<FluidContainerComponent> FLUID_CONTAINER_COMPONENT = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		Identifier.fromNamespaceAndPath(MOD_ID, "fluid_container"),
		DataComponentType.<FluidContainerComponent>builder().persistent(FluidContainerComponent.CODEC).build());

	@Override
	public void onInitialize()
	{
	}
}
