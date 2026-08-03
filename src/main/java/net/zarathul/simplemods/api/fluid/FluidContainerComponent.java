package net.zarathul.simplemods.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record FluidContainerComponent(int amount, int capacity, Identifier fluid)
{
	public static final Codec<FluidContainerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("amount").forGetter(FluidContainerComponent::amount),
		Codec.INT.fieldOf("capacity").forGetter(FluidContainerComponent::capacity),
		Identifier.CODEC.fieldOf("fluid").forGetter(FluidContainerComponent::fluid)
	).apply(instance, FluidContainerComponent::new));
}
