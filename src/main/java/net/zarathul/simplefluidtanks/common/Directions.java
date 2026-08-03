package net.zarathul.simplefluidtanks.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;

/**
 * Provides constants and helper methods dealing with vanilla directions.
 */
public class Directions
{
	/**
	 * Maps vanilla minecraft direction values to bitflags.
	 */
	public static final ImmutableMap<Direction, Integer> sidesToBitFlagsMappings = ImmutableMap.<Direction, Integer>builder()
			.put(Direction.DOWN, 1)
			.put(Direction.UP, 2)
			.put(Direction.NORTH, 4)
			.put(Direction.SOUTH, 8)
			.put(Direction.WEST, 16)
			.put(Direction.EAST, 32)
			.build();
}
