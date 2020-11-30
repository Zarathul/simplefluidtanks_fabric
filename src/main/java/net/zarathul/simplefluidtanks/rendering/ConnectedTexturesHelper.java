package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.core.Direction;

public class ConnectedTexturesHelper
{
	public static int getEastTextureIndex(boolean[] connections)
	{
		int  texture = 0;

		if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 8; // emptyTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 7; // leftRightTopTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 12; // topBottomLeftTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 6; // leftRightBottomTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 13; // topBottomRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 11; // topBottomTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 5; // leftRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 14; // topLeftTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 2; // bottomLeftTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 3; // bottomRightTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 15; // topRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()])
		{
			texture = 10; // topTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 1; // bottomTexture;
		}
		else if (connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 4; // leftTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 9; // rightTexture
		}

		return texture;
	}

	public static int getWestTextureIndex(boolean[] connections)
	{
		int  texture = 0;

		if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 8;	// emptyTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 7; // leftRightTopTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 13; // topBottomRightTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 6; // leftRightBottomTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 12; // topBottomLeftTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 11; // topBottomTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 5; // leftRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 15; // topRightTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 3; // bottomRightTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 2; // bottomLeftTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 14; // topLeftTexture
		}
		else if (connections[Direction.UP.get3DDataValue()])
		{
			texture = 10; // topTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 1; // bottomTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 9; // rightTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 4; // leftTexture
		}

		return texture;
	}

	public static int getSouthTextureIndex(boolean[] connections)
	{
		int  texture = 0;

		if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 8;	// emptyTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 7; // leftRightTopTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 13; // topBottomRightTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 6; // leftRightBottomTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 12; // topBottomLeftTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 11; // topBottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 5; // leftRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()])
		{
			texture = 15; // topRightTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 3; // bottomRightTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 2; // bottomLeftTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 14; // topLeftTexture
		}
		else if (connections[Direction.UP.get3DDataValue()])
		{
			texture = 10; // topTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 1; // bottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()])
		{
			texture = 9; // rightTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()])
		{
			texture = 4; // leftTexture
		}

		return texture;
	}

	public static int getNorthTextureIndex(boolean[] connections)
	{
		int  texture = 0;

		if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 8;	// emptyTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 7; // leftRightTopTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 12; // topBottomLeftTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 6; // leftRightBottomTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 13; // topBottomRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 11; // topBottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 5; // leftRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()])
		{
			texture = 14; // topLeftTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 2; // bottomLeftTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 3; // bottomRightTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.UP.get3DDataValue()])
		{
			texture = 15; // topRightTexture
		}
		else if (connections[Direction.UP.get3DDataValue()])
		{
			texture = 10; // topTexture
		}
		else if (connections[Direction.DOWN.get3DDataValue()])
		{
			texture = 1; // bottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()])
		{
			texture = 4; // leftTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()])
		{
			texture = 9; // rightTexture
		}

		return texture;
	}

	public static int getUpTextureIndex(boolean[] connections)
	{
		int  texture = 0;

		if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 8;	// emptyTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 12; // topBottomLeftTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 7; // leftRightTopTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 13; // topBottomRightTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()])
		{
			texture = 6; // leftRightBottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 5; // leftRightTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 11; // topBottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 14; // topLeftTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 15; // topRightTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 3; // bottomRightTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()])
		{
			texture = 2; // bottomLeftTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()])
		{
			texture = 4; // leftTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()])
		{
			texture = 9; // rightTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 10; // topTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 1; // bottomTexture
		}

		return texture;
	}

	public static int getDownTextureIndex(boolean[] connections)
	{
		int  texture = 0;

		if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 8;	// emptyTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 12; // topBottomLeftTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 6; // leftRightBottomTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 13; // topBottomRightTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()])
		{
			texture = 7; // leftRightTopTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 5; // leftRightTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 11; // topBottomTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()] && connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 2; // bottomLeftTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()] && connections[Direction.WEST.get3DDataValue()])
		{
			texture = 3; // bottomRightTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()] && connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 15; // topRightTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()] && connections[Direction.EAST.get3DDataValue()])
		{
			texture = 14; // topLeftTexture
		}
		else if (connections[Direction.EAST.get3DDataValue()])
		{
			texture = 4; // leftTexture
		}
		else if (connections[Direction.WEST.get3DDataValue()])
		{
			texture = 9; // rightTexture
		}
		else if (connections[Direction.SOUTH.get3DDataValue()])
		{
			texture = 1; // bottomTexture
		}
		else if (connections[Direction.NORTH.get3DDataValue()])
		{
			texture = 10; // topTexture
		}

		return texture;
	}
}
