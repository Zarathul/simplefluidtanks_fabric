package net.zarathul.simplefluidtanks.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zarathul.simplefluidtanks.blocks.ModBlocks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.rendering.ConnectedTexturesHelper;
import net.zarathul.simplemodslib.api.fluid.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Holds {@link BlockEntity} data for {@link TankBlock}s,
 */
public class TankBlockEntity extends BlockEntity
{
	/**
	 *  Defines how many visual steps there are in the fluid model.
	 */
	public static final int FILL_LEVELS = 16;

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
	 * Texture indices for the 6 sides of the tank block, see {@link net.zarathul.simplefluidtanks.rendering.ConnectedTexturesHelper}.
	 */
	private int[] textures;

	/**
	 * Default constructor.
	 */
	public TankBlockEntity(final BlockPos pos, final BlockState state)
	{
		super(ModBlocks.TANK_ENTITY, pos, state);

		fillLevel = 0;
		isPartOfTank = false;
		valveCoords = null;
		connections = new boolean[6];
		textures = new int[6];
		Arrays.fill(textures, 0);
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
	protected void saveAdditional(ValueOutput output)
	{
		output.putByte(TAG_FILL_LEVEL, (byte)fillLevel);
		output.putBoolean(TAG_IS_PART_OF_TANK, isPartOfTank);

		if (valveCoords != null)
		{
			int[] valveCoordsArray = new int[] { valveCoords.getX(), valveCoords.getY(), valveCoords.getZ() };
			output.putIntArray(TAG_VALVE_COORDS, valveCoordsArray);
		}

		output.putBoolean(TAG_CONNECTION_Y_NEG, connections[Direction.DOWN.get3DDataValue()]);
		output.putBoolean(TAG_CONNECTION_Y_POS, connections[Direction.UP.get3DDataValue()]);
		output.putBoolean(TAG_CONNECTION_Z_NEG, connections[Direction.NORTH.get3DDataValue()]);
		output.putBoolean(TAG_CONNECTION_Z_POS, connections[Direction.SOUTH.get3DDataValue()]);
		output.putBoolean(TAG_CONNECTION_X_NEG, connections[Direction.WEST.get3DDataValue()]);
		output.putBoolean(TAG_CONNECTION_X_POS, connections[Direction.EAST.get3DDataValue()]);

		output.putIntArray(TAG_TEXTURES, textures);
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		fillLevel = input.getByteOr(TAG_FILL_LEVEL, (byte)0);
		isPartOfTank = input.getBooleanOr(TAG_IS_PART_OF_TANK, false);

		if (isPartOfTank)
		{
			int[] valveCoordsArray = input.getIntArray(TAG_VALVE_COORDS).get();
			valveCoords = new BlockPos(valveCoordsArray[0], valveCoordsArray[1], valveCoordsArray[2]);
		}

		connections = new boolean[6];
		connections[Direction.DOWN.get3DDataValue()]  = input.getBooleanOr(TAG_CONNECTION_Y_NEG, false);
		connections[Direction.UP.get3DDataValue()]    = input.getBooleanOr(TAG_CONNECTION_Y_POS, false);
		connections[Direction.NORTH.get3DDataValue()] = input.getBooleanOr(TAG_CONNECTION_Z_NEG, false);
		connections[Direction.SOUTH.get3DDataValue()] = input.getBooleanOr(TAG_CONNECTION_Z_POS, false);
		connections[Direction.WEST.get3DDataValue()]  = input.getBooleanOr(TAG_CONNECTION_X_NEG, false);
		connections[Direction.EAST.get3DDataValue()]  = input.getBooleanOr(TAG_CONNECTION_X_POS, false);

		textures = input.getIntArray(TAG_TEXTURES).get();
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
			// To get the BlockStateModel to consistently rerender the block state MUST change. Flipping a dummy boolean block state property does the trick.
			BlockState oldState = level.getBlockState(worldPosition);
			BlockState newState = oldState.setValue(TankBlock.RERENDER_TRIGGER, !oldState.getValue(TankBlock.RERENDER_TRIGGER));
			level.setBlockAndUpdate(worldPosition, newState);
			level.sendBlockUpdated(worldPosition, oldState, newState, Block.UPDATE_ALL);
		}
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
		return fillLevel == 0 || getFluid() == Fluids.EMPTY;
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
		if (isPartOfTank() && level != null)
		{
			return level.getBlockEntity(valveCoords, ModBlocks.VALVE_ENTITY).orElse(null);
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
		if (isPartOfTank() || valvePos == null || level == null) return false;

		var valveEntity = level.getBlockEntity(valvePos, ModBlocks.VALVE_ENTITY);

		if (valveEntity.isPresent())
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
	 * A value between {@code 0} and {@link TankBlockEntity#FILL_LEVELS}.
	 * @param forceBlockUpdate
	 * Specifies if a block update should be forced.
	 * @return <code>true</code> if the fill level has changed, otherwise <code>false</code>.
	 */
	public boolean setFillLevel(int value, boolean forceBlockUpdate)
	{
		value = Mth.clamp(value, 0, FILL_LEVELS);

		boolean fillLevelChanged = (value != fillLevel);
		fillLevel = value;

		if (fillLevelChanged || forceBlockUpdate)
		{
			setChanged();
		}

		return fillLevelChanged;
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
	 * The coordinates of the connected {@link ValveBlock}.
	 *
	 * @return
	 * The {@link ValveBlock}s coordinates, if the {@link TankBlock} is connected to one, otherwise {@code null}.
	 */
	public BlockPos getValveCoords()
	{
		return valveCoords;
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
		if (level == null ||
			checkPos.getX() < worldPosition.getX() - 1 || checkPos.getX() > worldPosition.getX() + 1 ||
			checkPos.getY() < worldPosition.getY() - 1 || checkPos.getY() > worldPosition.getY() + 1 ||
			checkPos.getZ() < worldPosition.getZ() - 1 || checkPos.getZ() > worldPosition.getZ() + 1)
		{
			return false;
		}

		var connectionCandidate = level.getBlockEntity(checkPos, ModBlocks.TANK_ENTITY);

		if (connectionCandidate.isPresent())
		{
			return (connectionCandidate.get().hasValveAt(valveCoords));
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
		fillLevel = 0;
		isPartOfTank = false;
		valveCoords = null;
		Arrays.fill(connections, false);
		Arrays.fill(textures, 0);

		if (!suppressBlockUpdates)
		{
			setChanged();
		}
	}

	/**
	 * Get all texture indexes.
	 * @return
	 * An array of texture indexes provided by {@link ConnectedTexturesHelper}. The index corresponds to {@link Direction#get3DDataValue()}.
	 */
	public int[] getTextureIndexes()
	{
		return textures;
	}
}
