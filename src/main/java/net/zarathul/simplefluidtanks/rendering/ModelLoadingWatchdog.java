package net.zarathul.simplefluidtanks.rendering;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.zarathul.simplefluidtanks.BlocksAndItems;

public class ModelLoadingWatchdog implements ModelLoadingPlugin
{
	@Override
	public void initialize(Context initContext)
	{
		initContext.modifyBlockModelAfterBake().register((model, context) -> {
			if (context.state().getBlock() == BlocksAndItems.blockTank)
			{
				return new TankBlockStateModel(model);
			}
			return model;
		});
	}
}
