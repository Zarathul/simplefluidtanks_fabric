package net.zarathul.simplefluidtanks;

import net.zarathul.simplefluidtanks.configuration.ConfigSetting;

public final class Settings
{
	@ConfigSetting(descriptionKey = "buckets_per_tank", description = "The amount of fluid one tank block can hold measured in buckets. If this value is changed, all tanks already placed in the world need to be manually updated by right-clicking the valve with the wrench.", category = "misc")
	public static int bucketsPerTank;
	public static final int bucketsPerTankDefault = 32;
	public static boolean bucketsPerTankValidator(int value)
	{
		return (value > 1);
	}

	@ConfigSetting(descriptionKey = "buckets_per_portable_tank", description = "The amount of fluid one portable tank can hold measured in buckets. Changing this value affects already existing portable tanks and may lead to loss of fluids if the value is lowered.", category = "misc", needsWorldRestart = true)
	public static int bucketsPerPortableTank;
	public static final int bucketsPerPortableTankDefault = 16;
	public static boolean bucketsPerPortableTankValidator(int value)
	{
		return (value > 1);
	}

	@ConfigSetting(descriptionKey = "tankblock_hardness", description = "The amount of hits the block can take before it breaks (-1 = indestructible).", category = "blocks")
	public static float tankBlockHardness;
	public static final float tankBlockHardnessDefault = 50;
	public static boolean tankBlockHardnessValidator(float value)
	{
		return (value >= -1.0f);
	}

	@ConfigSetting(descriptionKey = "tankblock_resistance", description = "The blocks resistance to explosions.", category = "blocks")
	public static float tankBlockResistance;
	public static final float tankBlockResistanceDefault = 1000;
	public static boolean tankBlockResistanceValidator(float value)
	{
		return (value >= 1.0f);
	}

	@ConfigSetting(descriptionKey = "valveblock_hardness", description = "The amount of hits the block can take before it breaks (-1 = indestructible).", category = "blocks")
	public static float valveBlockHardness;
	public static final float valveBlockHardnessDefault = 50;
	public static boolean valveBlockHardnessValidator(float value)
	{
		return (value >= -1.0f);
	}

	@ConfigSetting(descriptionKey = "valveblock_resistance", description = "The blocks resistance to explosions.", category = "blocks")
	public static float valveBlockResistance;
	public static final float valveBlockResistanceDefault = 1000;
	public static boolean valveBlockResistanceValidator(float value)
	{
		return (value >= 1.0f);
	}
}
