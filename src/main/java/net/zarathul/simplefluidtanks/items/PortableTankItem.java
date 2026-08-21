package net.zarathul.simplefluidtanks.items;

import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplemodslib.api.fluid.FluidContainerItemBase;
import net.zarathul.simplemodslib.api.fluid.FluidStack;

public class PortableTankItem extends FluidContainerItemBase
{
	public PortableTankItem(Properties properties)
	{
		int defaultCapacity = Settings.bucketsPerPortableTank() * FluidStack.BUCKET_VOLUME;
		super(properties.stacksTo(1), defaultCapacity);
	}
}
