package net.zarathul.simplefluidtanks.blocks.entities;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simplefluidtanks.Settings;
import net.zarathul.simplefluidtanks.blocks.ModBlocks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.BasicAStar;
import net.zarathul.simplefluidtanks.common.BlockSearchMode;
import net.zarathul.simplefluidtanks.common.Directions;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import net.zarathul.simplemodslib.api.fluid.IFluidHandler;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

/**
 * Holds {@link BlockEntity} data for {@link ValveBlock}s,
 */
public class ValveBlockEntity extends BlockEntity implements IFluidHandler
{
	/**
	 * The fill priorities of all connected {@link TankBlock}s.
	 */
	private Multimap<Integer, BlockPos> tankPriorities;

	/**
	 * A bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 * 
	 * @see Direction
	 */
	private byte tankFacingSides;

	/**
	 * A temporary mapping of connected {@link TankBlock}s to their priorities.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlockEntity} from nbt data.
	 */
	private HashMap<BlockPos, Integer> tankToPriorityMappings;

	/**
	 * A temporary set of all connected {@link TankBlock}s.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlockEntity} from nbt data.
	 */
	private HashSet<BlockPos> tanks;

	/**
	 * A temporary set of all connected {@link TankBlock}s before disbanding the multiblock structure.<br>
	 * <b>Caution:</b> This will be empty after reloading the {@link ValveBlockEntity} from nbt data.
	 */
	private HashSet<BlockPos> tanksBeforeDisband;

	/**
	 * Holds a {@link BasicAStar} instance while the tank finding algorithms are running.
	 */
	private BasicAStar aStar;
	
	public ValveBlockEntity(final BlockPos pos, final BlockState state)
	{
		super(ModBlocks.VALVE_ENTITY, pos, state);

		tankPriorities = ArrayListMultimap.create();
		tankFacingSides = -1;

		fluid = FluidStack.empty();
		capacity = 0;
	}

	private static final String TAG_FLUID_CAPACITY = "fluid_capacity";
	private static final String TAG_TANK_FACING_SIDES = "tank_facing_sides";

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);

		fluid.save(output);
		output.putInt(TAG_FLUID_CAPACITY, capacity);

		writeTankPrioritiesToNBT(output);
		output.putByte(TAG_TANK_FACING_SIDES, tankFacingSides);
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);

		fluid.load(input);
		capacity = input.getIntOr(TAG_FLUID_CAPACITY, 0);

		readTankPrioritiesFromNBT(input);
		tankFacingSides = input.getByteOr(TAG_TANK_FACING_SIDES, (byte)0);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return saveWithoutMetadata(registries);
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();

		if (level != null)
		{
			// To get the BlockStateModel to consistently rerender the block state MUST change.
			BlockState oldState = level.getBlockState(worldPosition);
			BlockState newState = (hasTanks()) ?
								  ValveBlock.getStateFromEntity(this, oldState) :
								  ValveBlock.getDisconnectedState(oldState);

			level.setBlockAndUpdate(worldPosition, newState);
			level.sendBlockUpdated(worldPosition, oldState, newState, Block.UPDATE_ALL);
		}
	}

	/**
	 * Gets the amount of fluid in the multiblock tank.
	 * 
	 * @return The amount of fluid in millibuckets.
	 */
	public int getFluidAmount()
	{
		return fluid.getAmount();
	}

	/**
	 * Updates the bitmask storing which sides of the {@link ValveBlock} face connected {@link TankBlock}s.
	 */
	public void updateTankFacingSides()
	{
		int sides = 0;

		if (isInTankList(worldPosition.south()))
		{
			sides = sides | Directions.sidesToBitFlagsMappings.get(Direction.SOUTH);
		}

		if (isInTankList(worldPosition.north()))
		{
			sides = sides | Directions.sidesToBitFlagsMappings.get(Direction.NORTH);
		}

		if (isInTankList(worldPosition.east()))
		{
			sides = sides | Directions.sidesToBitFlagsMappings.get(Direction.EAST);
		}

		if (isInTankList(worldPosition.west()))
		{
			sides = sides | Directions.sidesToBitFlagsMappings.get(Direction.WEST);
		}

		if (isInTankList(worldPosition.above()))
		{
			sides = sides | Directions.sidesToBitFlagsMappings.get(Direction.UP);
		}

		if (isInTankList(worldPosition.below()))
		{
			sides = sides | Directions.sidesToBitFlagsMappings.get(Direction.DOWN);
		}

		tankFacingSides = (byte) sides;
	}

	/**
	 * Checks if the specified side of the {@link ValveBlock} is facing a connected {@link TankBlock}.
	 * 
	 * @param side
	 * The side to check.
	 * @return <code>true</code> if the {@link ValveBlock} is facing a connected {@link TankBlock} on the specified side, otherwise <code>false</code>.
	 * @see Direction
	 */
	public boolean isFacingTank(Direction side)
	{
		if (side != null)
		{
			byte flags = Directions.sidesToBitFlagsMappings.get(side).byteValue();

			return (tankFacingSides & flags) == flags;
		}

		return false;
	}

	/**
	 * Checks if the {@link ValveBlock} has connected {@link TankBlock}s.
	 * 
	 * @return <code>true</code> if the {@link ValveBlock} has connected {@link TankBlock}s, otherwise <code>false</code>.
	 */
	public boolean hasTanks()
	{
		// the ValveBlock also counts as a tank in the multiblock structure
		return (tankPriorities.size() - 1) > 0;
	}

	/**
	 * Disconnects all connected {@link TankBlock}s and resets the {@link ValveBlock} itself (capacity etc.).
	 * The TankBlock or ValveBlock located at <code>ignorePos</code> will not get it's BlockState updated.
	 *
	 * @param ignorePos
	 * Tank- or ValveBlock at this position will not get it's BlockState updated.
	 */
	public void disbandMultiblock(BlockPos ignorePos)
	{
		disbandMultiblock(false, ignorePos);
	}

	/**
	 * Disconnects all connected {@link TankBlock}s and resets the {@link ValveBlock} itself (capacity etc.).
	 * 
	 * @param suppressBlockUpdates
	 * Specifies if block updates should be suppressed.
	 * @param ignorePos
	 * Tank- or ValveBlock at this position will not get it's BlackState updated.
	 */
	public void disbandMultiblock(boolean suppressBlockUpdates, BlockPos ignorePos)
	{
		for (BlockPos tankPos : tankPriorities.values())
		{
			var tankEntity = level.getBlockEntity(tankPos, ModBlocks.TANK_ENTITY);

			if (tankEntity.isPresent() && (!tankPos.equals(ignorePos)))
			{
				tankEntity.get().disconnect(suppressBlockUpdates);
			}
		}

		if (suppressBlockUpdates)
		{
			// This Set is used later to update TankBlocks that are no longer part of the multiblock structure.
			tanksBeforeDisband = new HashSet<>();
			tanksBeforeDisband.addAll(tankPriorities.values());
		}

		tankPriorities.clear();
		tankFacingSides = 0;
		fluid = FluidStack.empty();
		capacity = 0;

		if (!suppressBlockUpdates && (!worldPosition.equals(ignorePos)))
		{
			setChanged();
		}
	}

	/**
	 * Re-runs the tank searching and prioritization algorithms and redistributes the fluid. 
	 * This allows for additional {@link TankBlock}s to be added to the multiblock structure.
	 */
	public void formMultiblock()
	{
		// store the current fluid for reinsertion
		FluidStack fluidBackup = fluid.copy();

		// find new tanks and update the valves textures

		// block updates are suppressed here because tanks are updated anyway when the fluid is distributed
		disbandMultiblock(true, null);
		findAndPrioritizeTanks();
		// tanks that are no longer part of the multiblock structure need to be updated to render correctly
		updateOrphanedTanks();
		updateTankFacingSides();

		// redistribute the fluid
		setFluid(fluidBackup);
		distributeFluidToTanks(true);

		setChanged();
	}

	/**
	 * Finds all {@link TankBlock}s connected to the {@link ValveBlock} and computes their filling priorities.
	 */
	private void findAndPrioritizeTanks()
	{
		generateTankList();
		computeFillPriorities();

		ArrayList<TankBlockEntity> tankEntities = new ArrayList<>(tankPriorities.size());

		// set the valve for all connected tanks
		for (BlockPos tankPos : tankPriorities.values())
		{
			var tankEntityOptional = level.getBlockEntity(tankPos, ModBlocks.TANK_ENTITY);

			if (tankEntityOptional.isPresent())
			{
				TankBlockEntity tankEntity = tankEntityOptional.get();
				tankEntity.setValve(worldPosition);
				tankEntities.add(tankEntity);
			}
		}
		
		// This needs to be done after setting the valve. Otherwise, the connected textures will be wrong.
		for (TankBlockEntity tankEntity : tankEntities)
		{
			tankEntity.updateConnections();
		}

		// calculate and set the internal tanks capacity, note the " + 1" is needed because the ValveBlock itself is considered a tank with storage capacity
		setCapacity((tankEntities.size() + 1) * Settings.bucketsPerTank() * FluidStack.BUCKET_VOLUME);
	}

	/**
	 * Marks TankBlocks that are no longer part of the multiblock structure for an update.
	 */
	private void updateOrphanedTanks()
	{
		Collection<BlockPos> tanksToUpdate = Collections2.filter(tanksBeforeDisband, Predicates.not(Predicates.in(tanks)));

		for (BlockPos tankPos : tanksToUpdate)
		{
			var tankEntity = level.getBlockEntity(tankPos, ModBlocks.TANK_ENTITY);

			if (tankEntity.isPresent())
			{
				tankEntity.get().setChanged();
			}
		}

		tanksBeforeDisband.clear();
	}

	/**
	 * Distributes the fluid currently held by the multiblock tank over the connected {@link TankBlock}s.
	 */
	private void distributeFluidToTanks()
	{
		distributeFluidToTanks(false);
	}

	/**
	 * Distributes the fluid currently held by the multiblock tank over the connected {@link TankBlock}s.
	 * 
	 * @param forceBlockUpdates
	 * Specifies if block updates should be forced.
	 */
	private void distributeFluidToTanks(boolean forceBlockUpdates)
	{
		// returned amount is mb(milli buckets)
		int amountToDistribute = fluid.getAmount();

		if (amountToDistribute == 0 || amountToDistribute == capacity)
		{
			// there is nothing to distribute or the internal tank is full (no fill percentage calculations needed)
			int fillPercentage = (amountToDistribute == 0) ? 0 : 100;

			for (BlockPos tankPos : tankPriorities.values())
			{
				var tankEntity = level.getBlockEntity(tankPos, ModBlocks.TANK_ENTITY);

				if (tankEntity.isPresent())
				{
					tankEntity.get().setFillLevel(getFluidLevel(fillPercentage), forceBlockUpdates);
				}
			}
		}
		else
		{
			// get the fill priorities and sort them low to high
			int[] priorities = Ints.toArray(tankPriorities.keySet());
			Arrays.sort(priorities);

			Collection<BlockPos> tanksToFill;

			// for each priority get all the TankBlocks and fill them evenly
			for (int priority : priorities)
			{
				tanksToFill = tankPriorities.get(priority);

				int capacity = tanksToFill.size() * Settings.bucketsPerTank() * FluidStack.BUCKET_VOLUME;
				int fillPercentage = Mth.clamp((int) Math.ceil((double) amountToDistribute / (double) capacity * 100d), 0, 100);

				for (BlockPos tankPos : tanksToFill)
				{
					var tankEntity = level.getBlockEntity(tankPos, ModBlocks.TANK_ENTITY);

					if (tankEntity.isPresent())
					{
						tankEntity.get().setFillLevel(getFluidLevel(fillPercentage), forceBlockUpdates);
					}
				}

				amountToDistribute -= Math.min(capacity, amountToDistribute);
			}
		}
	}

	/**
	 * Generates a list of the coordinates of all {@link TankBlock}s connected to the {@link ValveBlock}. This list is temporary and contains no priority information.
	 */
	private void generateTankList()
	{
		tanks = new HashSet<>();

		ArrayList<BlockPos> currentTanks = new ArrayList<>();
		ArrayList<BlockPos> newTanks = new ArrayList<>();
		Collection<BlockPos> adjacentTanks;

		currentTanks.add(worldPosition);

		// simple flood find algorithm: get all adjacent TankBlocks to the current one -> repeat for all blocks found until there is nothing left to find
		do
		{
			for (BlockPos tank : currentTanks)
			{
				adjacentTanks = findAdjacentTanks(tank);

				for (BlockPos adjacentTank : adjacentTanks)
				{
					if (tanks.add(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());
	}

	/**
	 * Computes the fill priorities for all connected {@link TankBlock}s.
	 */
	private void computeFillPriorities()
	{
		aStar = new BasicAStar();
		tankToPriorityMappings = new HashMap<>();

		ArrayList<BlockPos> tanksWithoutLowerTanks = new ArrayList<>();
		ArrayList<BlockPos> currentTanks = new ArrayList<>();
		ArrayList<BlockPos> tanksOnSameHeight;
		ArrayList<BlockPos> lowerTanks;
		HashMap<BlockPos, Integer> tanksToPrioritize = new HashMap<>();
		HashSet<BlockPos> newTanks = new HashSet<>();
		HashSet<BlockPos> handledSourceTanks = new HashSet<>();
		HashSet<BlockPos> handledSegmentTanks = new HashSet<>();

		currentTanks.add(worldPosition);

		int priority = 0;
		int adjustedPriority;

		/*
		 * Prioritization uses two separate algorithms:
		 * - find the closest, lowest tanks that are connected to the current one (algorithm 1)
		 * - if the closest, lowest tank is the same as the current tank, find all tanks on the same height level that are reachable 
		 *   without ever going over the current tanks height level and set them all to the current priority if the current priority
		 *   is a higher value (= actual lower priority) than the priority the tanks already have (algorithm 2)
		 * - otherwise just set the closest, lowest tanks priority to the current priority value
		 * - in both cases add the tanks directly above the found tanks to the list of tanks that are processed in the next iteration
		 *   
		 *   The order of execution is important. Tanks found by the algorithm 1 have to be prioritized before tanks found by algorithm 2.
		 */

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				if (handledSegmentTanks.contains(currentTank)) continue;

				lowerTanks = getClosestLowestTanks(currentTank);

				// handle tanks with lower tanks first, store the rest for later processing
				if (lowerTanks.getFirst() == currentTank)
				{
					tanksWithoutLowerTanks.add(currentTank);
					handledSegmentTanks.addAll(lowerTanks);
				}
				else
				{
					handledSourceTanks.add(currentTank);

					for (BlockPos lowerTank : lowerTanks)
					{
						tanksToPrioritize.put(lowerTank, priority);
						newTanks.addAll(getAdjacentTanks(lowerTank, BlockSearchMode.Above));
					}
				}
			}

			// find connected tanks on the same height without stepping over the height level of the initial tank
			for (BlockPos tankWithoutLowerTanks : tanksWithoutLowerTanks)
			{
				if (!tanksToPrioritize.containsKey(tankWithoutLowerTanks))
				{
					tanksOnSameHeight = getTanksOnSameHeight(tankWithoutLowerTanks);

					if (Collections.disjoint(tanksOnSameHeight, handledSourceTanks))
					{
						for (BlockPos tankOnSameHeight : tanksOnSameHeight)
						{
							adjustedPriority = (tankToPriorityMappings.containsKey(tankOnSameHeight)) ?
									Math.max(priority, tankToPriorityMappings.get(tankOnSameHeight)) : priority;

							tanksToPrioritize.put(tankOnSameHeight, adjustedPriority);
							newTanks.addAll(getAdjacentTanks(tankOnSameHeight, BlockSearchMode.Above));
						}
					}
				}
			}

			for (Entry<BlockPos, Integer> entry : tanksToPrioritize.entrySet())
			{
				setTankPriority(entry.getKey(), entry.getValue());
			}

			priority++;

			tanksWithoutLowerTanks.clear();
			handledSourceTanks.clear();
			handledSegmentTanks.clear();
			tanksToPrioritize.clear();
			currentTanks.clear();

			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		aStar = null;
	}

	/**
	 * Gets a list of all tanks on the same height level that are reachable from one another without ever stepping over the initial tanks height level.
	 * 
	 * @param startTank
	 * The tanks to start from.
	 * @return An {@link ArrayList} of found tanks.
	 */
	private ArrayList<BlockPos> getTanksOnSameHeight(BlockPos startTank)
	{
		if (startTank == null)
		{
			return null;
		}

		ArrayList<BlockPos> adjacentTanks;
		ArrayList<BlockPos> foundTanks = new ArrayList<>();
		ArrayList<BlockPos> currentTanks = new ArrayList<>();
		EnumSet<BlockSearchMode> searchFlags;
		HashSet<BlockPos> handledTanks = new HashSet<>();
		HashSet<BlockPos> newTanks = new HashSet<>();

		currentTanks.add(startTank);

		/*
		 * Algorithm 2:
		 * - uses basically the same flood find algorithm generateTankList() uses, only modification is the height level limit
		 */

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				if (currentTank.getY() == startTank.getY())
				{
					foundTanks.add(currentTank);
				}

				searchFlags = (currentTank.getY() < startTank.getY()) ? BlockSearchMode.All : BlockSearchMode.SameLevelAndBelow;
				adjacentTanks = getAdjacentTanks(currentTank, searchFlags);

				for (BlockPos adjacentTank : adjacentTanks)
				{
					if (!handledTanks.contains(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}

				handledTanks.add(currentTank);
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		return foundTanks;
	}

	/**
	 * Sets or updates the priority for the {@link TankBlock} at the specified coordinates.
	 * 
	 * @param tank
	 * The {@link TankBlock} coordinates.
	 * @param priority
	 * The {@link TankBlock}s priority.
	 */
	private void setTankPriority(BlockPos tank, int priority)
	{
		if (tank == null || priority < 0)
		{
			return;
		}

		Integer oldPriority = tankToPriorityMappings.put(tank, priority);

		if (oldPriority == null)
		{
			tankPriorities.put(priority, tank);
		}
		else
		{
			tankPriorities.remove(oldPriority, tank);
			tankPriorities.put(priority, tank);
		}
	}

	/**
	 * Gets the closes, lowest tanks reachable from the specified tank.
	 * 
	 * @param startTank
	 * The {@link TankBlock}s coordinates to start from.
	 * @return An {@link ArrayList} of found tanks.
	 */
	private ArrayList<BlockPos> getClosestLowestTanks(BlockPos startTank)
	{
		if (startTank == null)
		{
			return null;
		}

		ArrayList<BlockPos> tanksInSegment;
		ArrayList<BlockPos> adjacentTanks;
		ArrayList<BlockPos> closestTanksWithTanksBelow;
		ArrayList<BlockPos> tanksWithTanksBelow = new ArrayList<>();
		ArrayList<BlockPos> newTanks = new ArrayList<>();
		ArrayList<BlockPos> foundTanks = new ArrayList<>();
		ArrayList<BlockPos> currentTanks = new ArrayList<>();

		currentTanks.add(startTank);

		/*
		 * Algorithm 1:
		 * - find all the tanks connected to the current one without going up or down, this is called a segment
		 * - find all tanks in the segment that have tanks below them
		 * - pick the ones that are closest to the current tank
		 * - repeat until no tanks are found below the tanks in the current segment
		 * - the tanks in the segments of the last iterations are the ones we are looking for
		 */

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				tanksInSegment = getTanksInSegment(currentTank);

				for (BlockPos segmentTank : tanksInSegment)
				{
					adjacentTanks = getAdjacentTanks(segmentTank, BlockSearchMode.Below);

					if (!adjacentTanks.isEmpty() && !hasPriority(adjacentTanks.getFirst()))
					{
						tanksWithTanksBelow.add(segmentTank);
					}
				}

				if (!tanksWithTanksBelow.isEmpty())
				{
					// if there is more than one way down, only consider the closest ones
					closestTanksWithTanksBelow = (tanksWithTanksBelow.size() > 1) ?
							getClosestTanks(tanksInSegment, tanksWithTanksBelow, currentTank) : tanksWithTanksBelow;

					for (BlockPos closestTank : closestTanksWithTanksBelow)
					{
						newTanks.add(closestTank.below());
					}
				}
				else
				{
					foundTanks.addAll(tanksInSegment);
				}

				tanksWithTanksBelow.clear();
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		return foundTanks;
	}

	/**
	 * Gets the {@link BlockPos} from the specified {@link Collection} that are closest to the specified destination.
	 * Note: A-Star needs to be set up before calling this.
	 * 
	 * @param passableBlocks
	 * The {@link BlockPos} the search algorithm is allowed to move through.
	 * @param sources
	 * The coordinates of the source blocks whose distances to the destination is measured.
	 * @param destination
	 * The coordinates of the destination block.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getClosestTanks(Collection<BlockPos> passableBlocks, Collection<BlockPos> sources, BlockPos destination)
	{
		if (tanks == null || tanks.isEmpty() || sources == null || sources.isEmpty() || destination == null)
		{
			return null;
		}

		ArrayList<Integer> distances = new ArrayList<>();
		Multimap<Integer, BlockPos> distanceToTanksMappings = ArrayListMultimap.create();
		int distance;

		// simply use my crappy A-Star implementation to measure the distances
		aStar.setPassableBlocks(tanks);

		for (BlockPos source : sources)
		{
			distance = (source.equals(destination)) ? 0 : aStar.getShortestPath(source, destination).currentCost;
			distances.add(distance);
			distanceToTanksMappings.put(distance, source);
		}

		Collections.sort(distances);

		return new ArrayList<>(distanceToTanksMappings.get(distances.getFirst()));
	}

	/**
	 * Gets all tanks in the same segment as the specified one. Segment means all tanks connected without going up or down.
	 * 
	 * @param firstTank
	 * The coordinates of the tank to start from.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getTanksInSegment(BlockPos firstTank)
	{
		if (firstTank == null)
		{
			return null;
		}

		LinkedHashSet<BlockPos> foundTanks = new LinkedHashSet<>();
		foundTanks.add(firstTank);

		ArrayList<BlockPos> currentTanks = new ArrayList<>();
		ArrayList<BlockPos> newTanks = new ArrayList<>();
		Collection<BlockPos> adjacentTanks;

		currentTanks.add(firstTank);

		do
		{
			for (BlockPos currentTank : currentTanks)
			{
				adjacentTanks = getAdjacentTanks(currentTank, BlockSearchMode.SameLevel);

				for (BlockPos adjacentTank : adjacentTanks)
				{
					if (foundTanks.add(adjacentTank))
					{
						newTanks.add(adjacentTank);
					}
				}
			}

			currentTanks.clear();
			currentTanks.addAll(newTanks);
			newTanks.clear();
		}
		while (!currentTanks.isEmpty());

		return new ArrayList<>(foundTanks);
	}

	/**
	 * Gets adjacent tanks to the specified one (uses temporary tank list).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getAdjacentTanks(BlockPos block, BlockSearchMode mode)
	{
		return getOrFindAdjacentTanks(block, mode, null, true);
	}

	/**
	 * Gets adjacent tanks to the specified one (uses temporary tank list).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> getAdjacentTanks(BlockPos block, EnumSet<BlockSearchMode> searchFlags)
	{
		return getOrFindAdjacentTanks(block, null, searchFlags, true);
	}

	/**
	 * Gets all adjacent tanks to the specified one (uses {@link BlockEntity} data to determine which blocks are valid tanks).
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @return An {@link ArrayList} of the found {@link BlockPos}.
	 */
	private ArrayList<BlockPos> findAdjacentTanks(BlockPos block)
	{
		return getOrFindAdjacentTanks(block, null, BlockSearchMode.All, false);
	}

	/**
	 * Gets all adjacent tanks to the specified one.
	 * 
	 * @param block
	 * The coordinates of the tank to get the adjacent tanks for.
	 * @param mode
	 * Specifies which adjacent tanks should be included.
	 * @param searchFlags
	 * Specifies which adjacent tanks should be included.
	 * @param useTankList
	 * Specifies if the temporary tank list or {@link BlockEntity} should be used to determine which blocks are valid tanks.
	 * @return
	 * A list of all adjacent tanks.
	 */
	private ArrayList<BlockPos> getOrFindAdjacentTanks(BlockPos block, BlockSearchMode mode, EnumSet<BlockSearchMode> searchFlags, boolean useTankList)
	{
		if (block == null || (mode == null && searchFlags == null))
		{
			return null;
		}

		ArrayList<BlockPos> foundTanks = new ArrayList<>();
		ArrayList<BlockPos> adjacentBlocks = new ArrayList<>();

		if (mode == BlockSearchMode.SameLevel || (searchFlags != null && searchFlags.contains(BlockSearchMode.SameLevel)))
		{
			adjacentBlocks.add(block.east());	// X+
			adjacentBlocks.add(block.west());	// X-
			adjacentBlocks.add(block.south());	// Z+
			adjacentBlocks.add(block.north());	// Z-
		}

		if (mode == BlockSearchMode.Above || (searchFlags != null && searchFlags.contains(BlockSearchMode.Above)))
		{
			adjacentBlocks.add(block.above());	// Y+
		}

		if (mode == BlockSearchMode.Below || (searchFlags != null && searchFlags.contains(BlockSearchMode.Below)))
		{
			adjacentBlocks.add(block.below());	// Y-
		}

		if (useTankList)
		{
			// use the tank cache to check if we found valid tanks
			for (BlockPos adjacentBlock : adjacentBlocks)
			{
				if (isInTankList(adjacentBlock))
				{
					foundTanks.add(adjacentBlock);
				}
			}
		}
		else
		{
			// use the block types and tile entity data to check if we found valid tanks
			for (BlockPos adjacentBlock : adjacentBlocks)
			{
				if (isUnlinkedTank(adjacentBlock))
				{
					foundTanks.add(adjacentBlock);
				}
			}
		}

		return foundTanks;
	}

	/**
	 * Checks if the block at the specified location is a {@link TankBlock} that is not connected to a {@link ValveBlock}.
	 * 
	 * @param pos
	 * The coordinates of the block to check.
	 * @return <code>true</code> if the block at the specified position is a valid {@link TankBlock}, otherwise <code>false</code>.
	 */
	private boolean isUnlinkedTank(BlockPos pos)
	{
		if (pos == null || level == null)
		{
			return false;
		}
		
		BlockState state = level.getBlockState(pos);

		if (state.getBlock() == ModBlocks.TANK)
		{
			var tankEntity = level.getBlockEntity(pos, ModBlocks.TANK_ENTITY);

			if (tankEntity.isPresent())
			{
				return !tankEntity.get().isPartOfTank();
			}
		}
		else
		{
			// this valve is also considered an unlinked tank as long as it has no associated tanks
			return (pos.equals(worldPosition) && tankPriorities.isEmpty());
		}

		return false;
	}

	/**
	 * Checks if the specified coordinates are contained in the temporary tank list.
	 * 
	 * @param pos
	 * The coordinates of the block to check.
	 * @return <code>true</code> if the specified coordinates were found, otherwise <code>false</code>.
	 */
	private boolean isInTankList(BlockPos pos)
	{
		if (pos == null)
		{
			return false;
		}

		if (tanks != null)
		{
			return tanks.contains(pos);
		}
		else if (tankPriorities != null)
		{
			return tankPriorities.values().contains(pos);
		}

		return false;
	}

	/**
	 * Checks if the tank at the specified coordinates has a priority associated with it.
	 * 
	 * @param tank
	 * The coordinates of the block to check.
	 * @return <code>true</code> if the tank has a priority, otherwise <code>false</code>.
	 */
	private boolean hasPriority(BlockPos tank)
	{
		return tankPriorities.containsValue(tank);
	}

	/**
	 * Writes the tank priority map to the specified NBT tag.
	 * 
	 * @param output
	 * The tag to write to.
	 */
	private void writeTankPrioritiesToNBT(ValueOutput output)
	{
		if (output == null)
		{
			return;
		}

		ValueOutput tankPrioritiesTag = output.child("TankPriorities");
		BlockPos currentPos;
		int[] serializableEntry;
		int i = 0;

		for (Entry<Integer, BlockPos> entry : tankPriorities.entries())
		{
			currentPos = entry.getValue();
			serializableEntry = new int[] { entry.getKey(), currentPos.getX(), currentPos.getY(), currentPos.getZ() };
			tankPrioritiesTag.putIntArray(Integer.toString(i), serializableEntry);
			i++;
		}
	}

	/**
	 * Read the tank priority map from the specified NBT tag.
	 * 
	 * @param input
	 * The tag to read from.
	 */
	private void readTankPrioritiesFromNBT(ValueInput input)
	{
		if (input != null)
		{
			var tankPrioritiesTag = input.child("TankPriorities").get();

			if (!tankPrioritiesTag.keySet().isEmpty())
			{
				tankPriorities = ArrayListMultimap.create();

				String key;
				int i = 0;
				int[] serializedEntry;

				while (tankPrioritiesTag.contains(key = Integer.toString(i)))
				{
					serializedEntry = tankPrioritiesTag.getIntArray(key).get();
					tankPriorities.put(serializedEntry[0], new BlockPos(serializedEntry[1], serializedEntry[2], serializedEntry[3]));
					i++;
				}
			}
		}
	}
	/**
	 *
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
	public static ValveBlockEntity getValve(LevelAccessor world, BlockPos pos)
	{
		if (world != null && pos != null)
		{
			var tankEntity = world.getBlockEntity(pos, ModBlocks.TANK_ENTITY);

			if (tankEntity.isPresent())
			{
				ValveBlockEntity valveEntity = tankEntity.get().getValve();
				return valveEntity;
			}
		}

		return null;
	}
	/**
	 *
	 * Calculates the fluid level for the specified fill percentage.
	 *
	 * @param fillPercentage
	 * The fill percentage.
	 * @return
	 * A value between 0 and {@link TankBlockEntity#FILL_LEVELS} (inclusive).
	 */
	public static int getFluidLevel(int fillPercentage)
	{
		int level = (int)Math.round((fillPercentage / 100.0d) * TankBlockEntity.FILL_LEVELS);

		// Make sure that even for small amounts the fluid is rendered at the first level.
		return (fillPercentage > 0) ? Math.max(1, level) : 0;
	}

	//
	//	IFluidHandler
	//

	private FluidStack fluid;
	private int capacity;

	public FluidStack getFluid() { return fluid; }

	public int getCapacity()
	{
		return capacity;
	}

	/**
	 * Sets the capacity of the tank.
	 *
	 * @param newCapacity
	 * The capacity the tank should have.<br>
	 * Values smaller than 0 are ignored.
	 */
	private void setCapacity(int newCapacity)
	{
		capacity = Math.max(0, newCapacity);		// negative capacity makes no sense
		// limit the fluid amount to the new capacity
		if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), capacity));
	}

	/**
	 * Sets the type and amount of fluid contained in the tank.
	 *
	 * @param newFluid
	 * The fluid that should be contained in the tank.<br>
	 * Note that the amount will be limited to the tanks capacity.
	 */
	public void setFluid(FluidStack newFluid)
	{
		fluid = newFluid.copy();
		// limit the stored fluid to the tanks capacity
		if (!fluid.isEmpty()) fluid.setAmount(Math.min(fluid.getAmount(), capacity));
	}

	public void fluidChanged(FluidChange change)
	{
		distributeFluidToTanks();
		setChanged();
	}
}
