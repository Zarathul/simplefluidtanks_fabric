package net.zarathul.simplefluidtanks.blocks.entities;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.rendering.ConnectedTexturesHelper;
import net.zarathul.simplemods.api.fluid.FluidStack;

import java.util.Arrays;

/**
 * Holds {@link BlockEntity} data for {@link TankBlock}s,
 */
public class TankBlockEntity extends BlockEntity implements BlockEntityClientSerializable
{
	/**
	 * The fill level of the tank.
	 */
	private int fillLevel;

	/**
	 * Indicates if the {@link TankBlock} is part of a multiblock tank aka. connected to a {@link ValveBlock}.
	 */
	private boolean isPartOfTank;

	/**
	 * The coordinates of the {@link ValveBlock} the {@link TankBlock} is connected to.
	 */
	private BlockPos valveCoords;

	/**
	 * Contains information on which side there are other {@link TankBlock}s that belong to the same multiblock structure.
	 */
	private boolean[] connections;

	/**
	 * Texture indices for the 6 sides of the tank block, see {@link net.zarathul.simplefluidtanks.rendering.ConnectedTexturesHelper#textures}.
	 */
	private int[] textures;

	/**
	 * Default constructor.
	 */
	public TankBlockEntity()
	{
		super(SimpleFluidTanks.entityTank);

		fillLevel = 0;
		isPartOfTank = false;
		valveCoords = null;
		connections = new boolean[6];
		textures = new int[6];
	}

	private static final String TAG_FILL_LEVEL = "fill_level";
	private static final String TAG_IS_PART_OF_TANK = "is_part_of_tank";
	private static final String TAG_VALVE_COORDS = "valve_coords";
	private static final String TAG_CONNECTION_Y_NEG = "connection_y_neg";
	private static final String TAG_CONNECTION_Y_POS = "connection_y_pos";
	private static final String TAG_CONNECTION_Z_NEG = "connection_z_neg";
	private static final String TAG_CONNECTION_Z_POS = "connection_z_pos";
	private static final String TAG_CONNECTION_X_NEG = "connection_x_neg";
	private static final String TAG_CONNECTION_X_POS = "connection_x_pos";
	private static final String TAG_TEXTURES = "textures";

	@Override
	public CompoundTag save(CompoundTag tag)
	{
		super.save(tag);

		tag.putByte(TAG_FILL_LEVEL, (byte)fillLevel);
		tag.putBoolean(TAG_IS_PART_OF_TANK, isPartOfTank);

		if (valveCoords != null)
		{
			int[] valveCoordsArray = new int[] { valveCoords.getX(), valveCoords.getY(), valveCoords.getZ() };
			tag.putIntArray(TAG_VALVE_COORDS, valveCoordsArray);
		}

		tag.putBoolean(TAG_CONNECTION_Y_NEG, connections[Direction.DOWN.get3DDataValue()]);
		tag.putBoolean(TAG_CONNECTION_Y_POS, connections[Direction.UP.get3DDataValue()]);
		tag.putBoolean(TAG_CONNECTION_Z_NEG, connections[Direction.NORTH.get3DDataValue()]);
		tag.putBoolean(TAG_CONNECTION_Z_POS, connections[Direction.SOUTH.get3DDataValue()]);
		tag.putBoolean(TAG_CONNECTION_X_NEG, connections[Direction.WEST.get3DDataValue()]);
		tag.putBoolean(TAG_CONNECTION_X_POS, connections[Direction.EAST.get3DDataValue()]);

		tag.putIntArray(TAG_TEXTURES, textures);

		return tag;
	}

	@Override
	public void load(BlockState state, CompoundTag tag)
	{
		super.load(state, tag);

		fillLevel = tag.getByte(TAG_FILL_LEVEL);
		isPartOfTank = tag.getBoolean(TAG_IS_PART_OF_TANK);

		if (isPartOfTank)
		{
			int[] valveCoordsArray = tag.getIntArray(TAG_VALVE_COORDS);
			valveCoords = new BlockPos(valveCoordsArray[0], valveCoordsArray[1], valveCoordsArray[2]);
		}

		connections = new boolean[6];
		connections[Direction.DOWN.get3DDataValue()] = tag.getBoolean(TAG_CONNECTION_Y_NEG);
		connections[Direction.UP.get3DDataValue()] = tag.getBoolean(TAG_CONNECTION_Y_POS);
		connections[Direction.NORTH.get3DDataValue()] = tag.getBoolean(TAG_CONNECTION_Z_NEG);
		connections[Direction.SOUTH.get3DDataValue()] = tag.getBoolean(TAG_CONNECTION_Z_POS);
		connections[Direction.WEST.get3DDataValue()] = tag.getBoolean(TAG_CONNECTION_X_NEG);
		connections[Direction.EAST.get3DDataValue()] = tag.getBoolean(TAG_CONNECTION_X_POS);

		textures = tag.getIntArray(TAG_TEXTURES);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag)
	{
		return save(tag);
	}

	@Override
	public void fromClientTag(CompoundTag tag)
	{
		load(getBlockState(), tag);
	}

	/**
	 * Checks if the {@link TankBlock} is part of a multiblock tank.
	 * 
	 * @return <code>true</code> if the {@link TankBlock} is part of a multiblock tank, otherwise false.
	 */
	public boolean isPartOfTank()
	{
		return isPartOfTank && valveCoords != null;
	}

	/**
	 * Checks if the {@link TankBlock} is empty.
	 * 
	 * @return <code>true</code> if the {@link TankBlock} is empty, otherwise false.
	 */
	public boolean isEmpty()
	{
		return fillLevel == 0;
	}

	/**
	 * Gets the {@link ValveBlock}s {@link BlockEntity} the {@link TankBlock} is linked to.
	 * 
	 * @return The valves {@link BlockEntity}<br>
	 * or<br>
	 * <code>null</code> if the {@link TankBlock} is not linked to a {@link ValveBlock}.
	 */
	public ValveBlockEntity getValve()
	{
		if (isPartOfTank())
		{
			return Utils.getBlockEntityAt(level, ValveBlockEntity.class, valveCoords);
		}

		return null;
	}

	/**
	 * Links the {@link TankBlock} to a {@link ValveBlock}.
	 * 
	 * @param valvePos
	 * The coordinates of the {@link ValveBlock}.
	 * @return <code>true</code> if linking succeeded, otherwise <code>false</code>.
	 */
	public boolean setValve(BlockPos valvePos)
	{
		if (isPartOfTank() || valvePos == null) return false;

		ValveBlockEntity valveEntity = Utils.getBlockEntityAt(level, ValveBlockEntity.class, valvePos);

		if (valveEntity != null)
		{
			valveCoords = valvePos.immutable();
			isPartOfTank = true;

			return true;
		}

		return false;
	}

	/**
	 * Gets the {@link TankBlock}s current fill level.
	 * 
	 * @return The {@link TankBlock}s filling level in percent.
	 */
	public int getFillLevel()
	{
		return fillLevel;
	}

	/**
	 * Sets the {@link TankBlock}s current fill level.
	 * 
	 * @param value
	 * The {@link TankBlock}s fill level.
	 * @param forceBlockUpdate
	 * Specifies if a block update should be forced.
	 * @return <code>true</code> if the fill level has changed, otherwise <code>false</code>.
	 */
	public boolean setFillLevel(int value, boolean forceBlockUpdate)
	{
		value = Mth.clamp(value, 0, SimpleFluidTanks.MAX_FILL_LEVEL);

		boolean levelChanged = (value != fillLevel);
		fillLevel = value;

		if (levelChanged || forceBlockUpdate)
		{
			sync();
			setChanged();
		}

		return levelChanged;
	}

	/**
	 * Gets the {@link Fluid} inside the multiblock tank structure.
	 * 
	 * @return The fluid or <code>Fluids.EMPTY</code> if the {@link TankBlock} is not linked to a {@link ValveBlock} or the multiblock tank is empty.
	 */
	public Fluid getFluid()
	{
		ValveBlockEntity valve = getValve();

		if (valve != null)
		{
			FluidStack fluidStack = valve.getFluid();

			if (fluidStack != null)
			{
				return fluidStack.getFluid();
			}
		}

		return Fluids.EMPTY;
	}
	
	/**
	 * Determines if the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure on the specified side.
	 * 
	 * @param side
	 * The side to check.
	 * @return <code>true</code> the the specified side is connected, otherwise <code>false</code>.
	 */
	public boolean isConnected(Direction side)
	{
		if (side == null) return false;
		
		return connections[side.get3DDataValue()];
	}

	/**
	 * Checks if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates.
	 * 
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @return <code>true</code> if the {@link TankBlock} is connected to a {@link ValveBlock} at the specified coordinates, otherwise <code>false</code>.
	 */
	public boolean hasValveAt(BlockPos pos)
	{
		if (!isPartOfTank() || pos == null)
		{
			return false;
		}

		return pos.equals(valveCoords);
	}

	/**
	 * Builds an array that holds information on which side of the {@link TankBlock} is connected to another {@link TankBlock} of the same multiblock structure.
	 * Also updates the texture array used for connected textures rendering.
	 */
	public void updateConnections()
	{
		connections[Direction.EAST.get3DDataValue()]  = shouldConnectTo(worldPosition.east());		// X+
		connections[Direction.WEST.get3DDataValue()]  = shouldConnectTo(worldPosition.west());		// X-
		connections[Direction.UP.get3DDataValue()]    = shouldConnectTo(worldPosition.above());		// Y+
		connections[Direction.DOWN.get3DDataValue()]  = shouldConnectTo(worldPosition.below());		// Y-
		connections[Direction.SOUTH.get3DDataValue()] = shouldConnectTo(worldPosition.south());		// Z+
		connections[Direction.NORTH.get3DDataValue()] = shouldConnectTo(worldPosition.north());		// Z-

		textures[Direction.EAST.get3DDataValue()]  = ConnectedTexturesHelper.getEastTextureIndex(connections);
		textures[Direction.WEST.get3DDataValue()]  = ConnectedTexturesHelper.getWestTextureIndex(connections);
		textures[Direction.SOUTH.get3DDataValue()] = ConnectedTexturesHelper.getSouthTextureIndex(connections);
		textures[Direction.NORTH.get3DDataValue()] = ConnectedTexturesHelper.getNorthTextureIndex(connections);
		textures[Direction.UP.get3DDataValue()]    = ConnectedTexturesHelper.getUpTextureIndex(connections);
		textures[Direction.DOWN.get3DDataValue()]  = ConnectedTexturesHelper.getDownTextureIndex(connections);
	}

	/**
	 * Checks if this tank and the one passed are part of the same multi-block tank.
	 *
	 * @param other
	 * The other tank to check.
	 * @return
	 * <c>true</c> if both tanks are part of the same multi-block tank, otherwise <c>false</c>.
	 */
	public boolean isPartOfSameTank(TankBlockEntity other)
	{
		if ((other == null) || !isPartOfTank || !other.isPartOfTank()) return false;

		return (other.getValve() == getValve());
	}

	/**
	 * Checks if the {@link TankBlock}s textures should connect to a {@link TankBlock} at the specified coordinates.
	 * 
	 * @param checkPos
	 * The coordinates of the connection candidate.
	 * @return <code>true</code> if the textures should connect, otherwise <code>false</code>.
	 */
	private boolean shouldConnectTo(BlockPos checkPos)
	{
		// only check adjacent blocks
		if (checkPos.getX() < worldPosition.getX() - 1 || checkPos.getX() > worldPosition.getX() + 1 ||
			checkPos.getY() < worldPosition.getY() - 1 || checkPos.getY() > worldPosition.getY() + 1 ||
			checkPos.getZ() < worldPosition.getZ() - 1 || checkPos.getZ() > worldPosition.getZ() + 1)
		{
			return false;
		}

		TankBlockEntity connectionCandidate = Utils.getBlockEntityAt(level, TankBlockEntity.class, checkPos);

		if (connectionCandidate != null)
		{
			return (connectionCandidate.hasValveAt(valveCoords));
		}

		return false;
	}

	/**
	 * Disconnects the {@link TankBlock} from a multiblock tank.
	 * 
	 * @param suppressBlockUpdates
	 * Specifies if block updates should be suppressed.
	 */
	public void disconnect(boolean suppressBlockUpdates)
	{
		isPartOfTank = false;
		fillLevel = 0;
		valveCoords = null;
		Arrays.fill(connections, false);
		Arrays.fill(textures, -1);

		if (!suppressBlockUpdates)
		{
			sync();
			setChanged();
		}
	}

	/**
	 * Get the texture index for the specified side.
	 *
	 * @param direction
	 * One of the {@link Direction} values.
	 * @return
	 * An index for the {@link ConnectedTexturesHelper#textures} array, or <c>-1</c> if there is no texture set for
	 * the specified side.
	 */
	public int getTextureIndex(Direction direction)
	{
		return textures[direction.get3DDataValue()];
	}
}
