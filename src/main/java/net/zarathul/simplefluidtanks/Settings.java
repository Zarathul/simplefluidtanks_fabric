package net.zarathul.simplefluidtanks;

import net.minecraft.resources.Identifier;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplemodslib.api.configuration.Config;
import net.zarathul.simplemodslib.api.configuration.ConfigSetting;

public final class Settings
{

	public static final Identifier BUCKETS_PER_TANK = Utils.createModIdentifier("buckets_per_tank");
	public static final Identifier BUCKETS_PER_PORTABLE_TANK = Utils.createModIdentifier("buckets_per_portable_tank");
	public static final Identifier TANK_BLOCK_DESTRUCTION_TIME = Utils.createModIdentifier("tank_block_destruction_time");
	public static final Identifier TANK_BLOCK_RESISTANCE = Utils.createModIdentifier("tank_block_resistance");
	public static final Identifier VALVE_BLOCK_DESTRUCTION_TIME = Utils.createModIdentifier("valve_block_destruction_time");
	public static final Identifier VALVE_BLOCK_RESISTANCE = Utils.createModIdentifier("valve_block_resistance");

	public static int   bucketsPerTank()            { return (int)   Config.getSetting(BUCKETS_PER_TANK).get().value; }
	public static int   bucketsPerPortableTank()    { return (int)   Config.getSetting(BUCKETS_PER_PORTABLE_TANK).get().value; }
	public static float tankBlockDestructionTime()  { return (float) Config.getSetting(TANK_BLOCK_DESTRUCTION_TIME).get().value; }
	public static float tankBlockResistance()       { return (float) Config.getSetting(TANK_BLOCK_RESISTANCE).get().value; }
	public static float valveBlockDestructionTime() { return (float) Config.getSetting(VALVE_BLOCK_DESTRUCTION_TIME).get().value; }
	public static float valveBlockResistance()      { return (float) Config.getSetting(VALVE_BLOCK_RESISTANCE).get().value; }

	public static void init()
	{
		Config.addInt(BUCKETS_PER_TANK, 32, ConfigSetting.INT_GREATER_THAN_ZERO, "The amount of fluid one tank block can hold measured in buckets. If this value is changed, all tanks already placed in the world need to be manually updated by right-clicking the valve with the wrench.", "misc", true, 4, false);
		Config.addInt(BUCKETS_PER_PORTABLE_TANK, 16, ConfigSetting.INT_GREATER_THAN_ZERO, "The amount of fluid one portable tank can hold measured in buckets. Changing this value affects already existing portable tanks and may lead to loss of fluids if the value is lowered.", "misc", true, 4, false);

		Config.addFloat(TANK_BLOCK_DESTRUCTION_TIME,  50.0f, ConfigSetting.FLOAT_GREATER_OR_EQUAL_TO_MINUS_ONE, "The amount of time it takes to destroy the tank (-1 = indestructible).", "blocks", true, 4, false);
		Config.addFloat(TANK_BLOCK_RESISTANCE,  1000.0f, ConfigSetting.FLOAT_GREATER_THAN_ZERO, "The blocks resistance to explosions.", "blocks", true, 4, false);
		Config.addFloat(VALVE_BLOCK_DESTRUCTION_TIME, 50.0f, ConfigSetting.FLOAT_GREATER_OR_EQUAL_TO_MINUS_ONE, "The amount of time it takes to destroy the valve (-1 = indestructible).", "blocks", true, 4, false);
		Config.addFloat(VALVE_BLOCK_RESISTANCE, 1000.0f, ConfigSetting.FLOAT_GREATER_THAN_ZERO, "The blocks resistance to explosions.", "blocks", true, 4, false);
	}
}
