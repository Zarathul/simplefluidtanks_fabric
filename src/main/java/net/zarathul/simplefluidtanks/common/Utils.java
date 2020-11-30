package net.zarathul.simplefluidtanks.common;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * General utility class.
 */
public final class Utils
{
	/**
	 * Gets the {@link BlockEntity} at the specified coordinates, cast to the specified type.
	 * 
	 * @param world
	 * The world.
	 * @param tileType
	 * The type the {@link BlockEntity} should be cast to.
	 * @param pos
	 * The coordinates of the {@link BlockEntity}.
	 * @return The {@link BlockEntity} or <code>null</code> if no {@link BlockEntity} was found or the types didn't match.
	 */
 	public static <T extends BlockEntity> T getBlockEntityAt(Level world, Class<T> tileType, BlockPos pos)
	{
		if (world != null && tileType != null && pos != null)
		{
			BlockEntity tile = world.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);

			if (tile != null && tile.getClass() == tileType)
			{
				return (T) tile;
			}
		}

		return null;
	}

	/**
	 * Gets the {@link ValveBlock}s {@link BlockEntity} for a linked {@link TankBlock}.
	 * 
	 * @return The valves {@link ValveBlockEntity}<br>
	 * or<br>
	 * <code>null</code> if no linked {@link ValveBlock} was found.
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link TankBlock}s coordinates.
	 */
	public static ValveBlockEntity getValve(Level world, BlockPos pos)
	{
		if (world != null && pos != null)
		{
			TankBlockEntity tankEntity = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos);

			if (tankEntity != null)
			{
				ValveBlockEntity valveEntity = tankEntity.getValve();

				return valveEntity;
			}
		}

		return null;
	}

	/**
	 * A predicate that returns <code>true</code> if passed string is neither <code>null</code> nor empty.
	 */
	private static final Predicate<String> stringNotNullOrEmpty = new Predicate<String>()
	{
		@Override
		public boolean apply(String item)
		{
			return !Strings.isNullOrEmpty(item);
		}
	};

	/**
	 * Checks a list of strings for <code>null</code> and empty elements.
	 * 
	 * @param items
	 * The list of strings to check.
	 * @return
	 * <code>true</code> if the list neither contains <code>null</code> elements nor empty strings, otherwise <code>false</code>.
	 */
	public static boolean notNullorEmpty(Iterable<String> items)
	{
		return Iterables.all(items, stringNotNullOrEmpty);
	}

	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 *
	 * @param key
	 * The name of the key.
	 * @param args
	 * Formatting arguments.
	 * @return
	 * A list of localized text components for the specified key, or an empty list if the key was not found.
	 */
	public static ArrayList<TextComponent> multiLineTranslate(String key, Object... args)
	{
		ArrayList<TextComponent> components = new ArrayList<>();

		if ((key != null) && I18n.exists(key))
		{
			String text = I18n.get(key, args);
			String[] lines = text.split("\\n");

			for (String line : lines)
			{
				components.add(new TextComponent(line));
			}
		}

		return components;
	}
	
	/**
	 * Calculates the fluid level for the specified fill percentage.
	 * 
	 * @param fillPercentage
	 * The fill percentage.
	 * @return
	 * A value between 0 and {@code BakedTankModel.FLUID_LEVELS} (inclusive).
	 */
	public static int getFluidLevel(int fillPercentage)
	{
		int level = (int)Math.round((fillPercentage / 100.0d) * SimpleFluidTanks.MAX_FILL_LEVEL);
		
		// Make sure that even for small amounts the fluid is rendered at the first level.
		return (fillPercentage > 0) ? Math.max(1, level) : 0;
	}
	
	/**
	 * Calculates the comparator redstone signal strength based on the quotient of the specified values.
	 * 
	 * @param numerator
	 * The numerator.
	 * @param denominator
	 * The denominator.
	 * @return
	 * A value between 0 and 15.
	 */
	public static int getComparatorLevel(float numerator, float denominator)
	{
		int level = (denominator != 0) ? ((int) Math.floor((numerator / denominator) * 14.0f)) + ((numerator > 0) ? 1 : 0) : 0;
		
		return level;
	}

	// Belongs to getMetricFormattedNumber
	private static final int FACTOR = 1000;
	private static final double FACTOR_LOG = Math.log(FACTOR);
	private static final char[] METRIC_SUFFIXES = { 'k', 'M', 'G', 'T', 'P', 'E' };

	/**
	 * Shortens a number using metric suffixes and applies the provided format and locale.
	 *
	 * @param number
	 * The number to shorten. Numbers lower than 1000 remain unchanged.
	 * @param shortFormat
	 * The string format to apply in case {@code number} gets shortened (3 arguments). The first argument is the
	 * shortened number (floating point), the second is the metric suffix and the third is the passed in object
	 * ({@code misc}) which can be anything.
	 * @param longFormat
	 * The string format to apply in case {@code number} is not shortened (2 arguments). The first argument is the
	 * unmodified number (decimal) and the second is the passed in object ({@code misc}) which can be anything.
	 * @param locale
	 * The locale to use for the number format.
	 * @param misc
	 * Can be used to add additional text to the output string.
	 * @return
	 * <c>null</c> if either {@code shortFormat}, {@code longFormat} or {@code locale} is <c>null</c>, otherwise the
	 * potentially shortened and formatted number.
	 */
	public static String getMetricFormattedNumber(long number, String shortFormat, String longFormat, Locale locale, Object misc)
	{
		if (shortFormat == null || longFormat == null || locale == null) return null;
		if (number < FACTOR) return String.format(locale, longFormat, number, misc);

		int exponent = (int)(Math.log(number) / FACTOR_LOG);

		return String.format(locale, shortFormat, number / Math.pow(FACTOR, exponent), METRIC_SUFFIXES[exponent - 1], misc);
	}

	/**
	 * Shortens a number using metric suffixes and applies the provided format (UK locale).
	 *
	 * @param number
	 * The number to shorten. Numbers lower than 1000 remain unchanged.
	 * @param shortFormat
	 * The string format to apply in case {@code number} gets shortened (3 arguments). The first argument is the
	 * shortened number (floating point), the second is the metric suffix and the third is the passed in object
	 * ({@code misc}) which can be anything.
	 * @param longFormat
	 * The string format to apply in case {@code number} is not shortened (2 arguments). The first argument is the
	 * unmodified number (decimal) and the second is the passed in object ({@code misc}) which can be anything.
	 * @param misc
	 * Can be used to add additional text to the output string.
	 * @return
	 * <c>null</c> if either {@code shortFormat}, {@code longFormat} or {@code locale} is <c>null</c>, otherwise the
	 * potentially shortened and formatted number.
	 */
	public static String getMetricFormattedNumber(long number, String shortFormat, String longFormat, Object misc)
	{
		return getMetricFormattedNumber(number, shortFormat, longFormat, Locale.UK, misc);
	}

	/**
	 * Cache for {@code isInterfaceAvailable()} return values.
	 */
	private static HashMap<String, Boolean> InterfaceLookupCache = new HashMap<>();

	/**
	 * Checks if the given interface is available. This is used to call into APIs of other mods that may not always be
	 * there.
	 *
	 * @param packageName
	 * The name of the package containing the interface.
	 * @param interfaceName
	 * The name of the interface to check.
	 *
	 * @return
	 * <c>true</c> if the interface exists, otherwise <c>false</c>.
	 */
	public static boolean isInterfaceAvailable(String packageName, String interfaceName)
	{
		String FullyQualifiedName = packageName + "." + interfaceName;

		if (InterfaceLookupCache.containsKey(FullyQualifiedName)) return InterfaceLookupCache.get(FullyQualifiedName);

		try
		{
			Class<?> Interface = Class.forName(FullyQualifiedName);
			InterfaceLookupCache.put(FullyQualifiedName, true);

			return true;
		}
		catch (Exception e)
		{
			InterfaceLookupCache.put(FullyQualifiedName, false);

			return false;
		}
	}

	/**
	 * Checks if an item is a wrench.
	 *
	 * @param item
	 * The item to check.
	 *
	 * @return
	 * <c>true</c> if the item is a wrench, otherwise <c>false</c>.
	 */
	public static boolean isWrenchItem(Item item)
	{
		// TODO: Add support for other wrenches or tools
		return (item == SimpleFluidTanks.itemWrench/*
		|| (Utils.isInterfaceAvailable("cofh.api.item", "IToolHammer") && item instanceof IToolHammer)
		|| (Utils.isInterfaceAvailable("blusunrize.immersiveengineering.api.tool", "ITool") && item instanceof ITool)
		|| (Utils.isInterfaceAvailable("appeng.api.implementations.items", "IAEWrench") && item instanceof IAEWrench)*/);
	}

	/**
	 * Performs a raycast along the players view axis to get the block the player is looking at.
	 *
	 * @param world
	 * The world.
	 * @param player
	 * The player.
	 * @return
	 * The result of the raycast. Check type() to see if the raycast did hit a block or not.
	 */
	public static BlockHitResult getPlayerPOVHitResult(Level world, Player player)
	{
		float pitch = player.xRot;
		float yaw = player.yRot;
		Vec3 eyePos = player.getEyePosition(1.0F);
		float h = Mth.cos(-yaw * 0.017453292F - 3.1415927F);
		float i = Mth.sin(-yaw * 0.017453292F - 3.1415927F);
		float j = -Mth.cos(-pitch * 0.017453292F);
		float k = Mth.sin(-pitch * 0.017453292F);
		float l = i * j;
		float n = h * j;
		Vec3 target = eyePos.add((double) l * 5.0D, (double) k * 5.0D, (double) n * 5.0D);

		return world.clip(new ClipContext(eyePos, target, net.minecraft.world.level.ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
	}
}
