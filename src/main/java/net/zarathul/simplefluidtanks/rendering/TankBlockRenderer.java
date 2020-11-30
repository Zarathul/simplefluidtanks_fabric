package net.zarathul.simplefluidtanks.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;

import java.util.function.Function;

// FIXME: Partial transparency of tank blocks is all kinds of broken. Seems like the vertices aren't sorted properly.

public class TankBlockRenderer extends BlockEntityRenderer<TankBlockEntity>
{
	private static final float yLightFactor = 0.5f;
	private static final float zLightFactor = 0.8f;
	private static final float xLightFactor = 0.6f;
	private static final float EPSILON = 0.00005f;

	private static final TextureAtlasSprite[] textures;

	private static final TextureAtlasSprite normalTexture;
	private static final TextureAtlasSprite bottomTexture;
	private static final TextureAtlasSprite bottomLeftTexture;
	private static final TextureAtlasSprite bottomRightTexture;
	private static final TextureAtlasSprite leftTexture;
	private static final TextureAtlasSprite leftRightTexture;
	private static final TextureAtlasSprite leftRightBottomTexture;
	private static final TextureAtlasSprite leftRightTopTexture;
	private static final TextureAtlasSprite emptyTexture;
	private static final TextureAtlasSprite rightTexture;
	private static final TextureAtlasSprite topTexture;
	private static final TextureAtlasSprite topBottomTexture;
	private static final TextureAtlasSprite topBottomLeftTexture;
	private static final TextureAtlasSprite topBottomRightTexture;
	private static final TextureAtlasSprite topLeftTexture;
	private static final TextureAtlasSprite topRightTexture;

	static
	{
		Function<ResourceLocation, TextureAtlasSprite> spriteGetter = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

		normalTexture          = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank"));
		bottomTexture          = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom"));
		bottomLeftTexture      = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom_left"));
		bottomRightTexture     = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_bottom_right"));
		leftTexture            = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left"));
		leftRightTexture       = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right"));
		leftRightBottomTexture = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right_bottom"));
		leftRightTopTexture    = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_left_right_top"));
		emptyTexture           = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_none"));
		rightTexture           = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_right"));
		topTexture             = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top"));
		topBottomTexture       = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom"));
		topBottomLeftTexture   = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom_left"));
		topBottomRightTexture  = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_bottom_right"));
		topLeftTexture         = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_left"));
		topRightTexture        = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/tank_top_right"));

		textures = new TextureAtlasSprite[]
		{
			normalTexture,				//  0
			bottomTexture,				//  1
			bottomLeftTexture,			//  2
			bottomRightTexture,			//  3
			leftTexture,				//  4
			leftRightTexture,			//  5
			leftRightBottomTexture,		//  6
			leftRightTopTexture,		//  7
			emptyTexture,				//  8
			rightTexture,				//  9
			topTexture,					// 10
			topBottomTexture,			// 11
			topBottomLeftTexture,		// 12
			topBottomRightTexture,		// 13
			topLeftTexture,				// 14
			topRightTexture				// 15
		};
	}


	public TankBlockRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher)
	{
		super(blockEntityRenderDispatcher);
	}

	@Override
	public boolean shouldRenderOffScreen(TankBlockEntity blockEntity)
	{
		return super.shouldRenderOffScreen(blockEntity);
	}

	@Override
	public void render(TankBlockEntity entity, float tickDelta, PoseStack poses, MultiBufferSource bufferSource, int light, int overlay)
	{
		poses.pushPose();

		Level world = entity.getLevel();
		if (world == null) return;

		BlockPos pos = entity.getBlockPos();
		Matrix4f worldMatrix = poses.last().pose();
		TextureAtlasSprite eastTexture, westTexture, southTexture, northTexture, upTexture, downTexture;
		TextureAtlasSprite eastTextureI, westTextureI, southTextureI, northTextureI;
		TextureAtlasSprite fluidTexture = null;

		// TODO: maybe drop the visibility flags for internal faces and do the epsilon offset thing again
		boolean eastFaceVisible;
		boolean eastFaceVisibleI = world.getBlockState(pos.east()).isAir();
		boolean westFaceVisible;
		boolean westFaceVisibleI = world.getBlockState(pos.west()).isAir();;
		boolean southFaceVisible;
		boolean southFaceVisibleI = world.getBlockState(pos.south()).isAir();;
		boolean northFaceVisible;
		boolean northFaceVisibleI = world.getBlockState(pos.north()).isAir();;
		boolean upFaceVisible;
		boolean upFaceVisibleI = world.getBlockState(pos.above()).isAir();;
		boolean downFaceVisible;
		boolean downFaceVisibleI = world.getBlockState(pos.below()).isAir();;

		boolean cullFluidTop = false;

		int lightXPos = world.getRawBrightness(pos.east(), world.getSkyDarken()) * 16;
		int lightXNeg = world.getRawBrightness(pos.west(), world.getSkyDarken()) * 16;
		int lightZPos = world.getRawBrightness(pos.south(), world.getSkyDarken()) * 16;
		int lightZNeg = world.getRawBrightness(pos.north(), world.getSkyDarken()) * 16;
		int lightYPos = world.getRawBrightness(pos.above(), world.getSkyDarken()) * 16;
		int lightYNeg = world.getRawBrightness(pos.below(), world.getSkyDarken()) * 16;
		int fluidColor = 16777215;	// Default for untinted fluids, which is every fluid but water right now.

		int fillLevel = entity.getFillLevel();

		float fY = Math.min((float)fillLevel / SimpleFluidTanks.MAX_FILL_LEVEL, 1f);


		if (!entity.isPartOfTank())
		{
			eastTexture  = normalTexture;
			westTexture  = normalTexture;
			southTexture = normalTexture;
			northTexture = normalTexture;
			upTexture    = normalTexture;
			downTexture  = normalTexture;;

			eastTextureI  = normalTexture;
			westTextureI  = normalTexture;
			southTextureI = normalTexture;
			northTextureI = normalTexture;

			TankBlockEntity eastTank = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.east());
			eastFaceVisible = ((eastTank == null) || eastTank.isPartOfTank());
			TankBlockEntity westTank = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.west());
			westFaceVisible = ((westTank == null) || westTank.isPartOfTank());
			TankBlockEntity southTank = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.south());
			southFaceVisible = ((southTank == null) || southTank.isPartOfTank());
			TankBlockEntity northTank = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.north());
			northFaceVisible = ((northTank == null) || northTank.isPartOfTank());
			TankBlockEntity upTank = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.above());
			upFaceVisible = ((upTank == null) || upTank.isPartOfTank());
			TankBlockEntity downTank = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.below());
			downFaceVisible = ((downTank == null) || downTank.isPartOfTank());
		}
		else
		{
			int eastIndex  = entity.getTextureIndex(Direction.EAST);
			int westIndex  = entity.getTextureIndex(Direction.WEST);
			int southIndex = entity.getTextureIndex(Direction.SOUTH);
			int northIndex = entity.getTextureIndex(Direction.NORTH);
			int upIndex    = entity.getTextureIndex(Direction.UP);
			int downIndex  = entity.getTextureIndex(Direction.DOWN);

			eastTexture  = (eastIndex != -1)  ? textures[eastIndex]  : normalTexture;
			westTexture  = (westIndex != -1)  ? textures[westIndex]  : normalTexture;
			southTexture = (southIndex != -1) ? textures[southIndex] : normalTexture;
			northTexture = (northIndex != -1) ? textures[northIndex] : normalTexture;
			upTexture    = (upIndex != -1)    ? textures[upIndex]    : normalTexture;
			downTexture  = (downIndex != -1)  ? textures[downIndex]  : normalTexture;

			eastTextureI  = westTexture;
			westTextureI  = eastTexture;
			southTextureI = northTexture;
			northTextureI = southTexture;

			if (!entity.isEmpty())
			{
				Fluid tankFluid = entity.getFluid();
				FluidRenderHandler handler = FluidRenderHandlerRegistryImpl.INSTANCE.get(tankFluid);
				if (handler != null)
				{
					TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(world, pos, tankFluid.defaultFluidState());
					if ((fluidSprites != null) && (fluidSprites.length > 0))
					{
						fluidTexture = fluidSprites[0];
					}
				}

				if (fluidTexture == null)
				{
					// Default to water still texture.
					fluidTexture = FluidRenderHandlerRegistryImpl.INSTANCE.get(Fluids.WATER).getFluidSprites(null, null, Fluids.WATER.defaultFluidState())[0];
				}

				TankBlockEntity tankAbove = Utils.getBlockEntityAt(world, TankBlockEntity.class, pos.above());
				cullFluidTop = ((tankAbove != null) && (entity.isPartOfSameTank(tankAbove)) && (tankAbove.getFillLevel() > 0));

				if (tankFluid == Fluids.WATER) fluidColor = BiomeColors.getAverageWaterColor(world, pos);
			}

			eastFaceVisible  = !entity.isConnected(Direction.EAST);
			westFaceVisible  = !entity.isConnected(Direction.WEST);
			southFaceVisible = !entity.isConnected(Direction.SOUTH);
			northFaceVisible = !entity.isConnected(Direction.NORTH);
			upFaceVisible    = !entity.isConnected(Direction.UP);
			downFaceVisible  = !entity.isConnected(Direction.DOWN);
		}

		// Note: Vertices are defined counter-clockwise, starting at the bottom left corner of the quad.
		// 		 UV coordinates are specified in DirectX style with the origin (0,0) being the top left
		//		 (with OpenGl you'd expect the origin to be in the lower left, which is not the case).

		// Fluid
		VertexConsumer translucentBuffer = bufferSource.getBuffer(RenderType.translucent());

		if ((fluidTexture != null) && (fillLevel > 0))
		{
			float fluidColorR = (float)(fluidColor >> 16 & 255) / 255.0F;
			float fluidColorG = (float)(fluidColor >> 8 & 255) / 255.0F;
			float fluidColorB = (float)(fluidColor & 255) / 255.0F;

			float fluidColorXR = fluidColorR * xLightFactor;
			float fluidColorXG = fluidColorG * xLightFactor;
			float fluidColorXB = fluidColorB * xLightFactor;

			float fluidColorZR = fluidColorR * zLightFactor;
			float fluidColorZG = fluidColorG * zLightFactor;
			float fluidColorZB = fluidColorB * zLightFactor;

			float fluidColorYR = fluidColorR * yLightFactor;
			float fluidColorYG = fluidColorG * yLightFactor;
			float fluidColorYB = fluidColorB * yLightFactor;

			// east (x+)
			if (eastFaceVisible)
			{
				translucentBuffer.vertex(worldMatrix, 1f - EPSILON, 0f, 1f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV0()).uv2(lightXPos).normal(1f, 0f, 0f).endVertex();	// botton left
				translucentBuffer.vertex(worldMatrix, 1f - EPSILON, 0f, 0f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV0()).uv2(lightXPos).normal(1f, 0f, 0f).endVertex();	// bottom right
				translucentBuffer.vertex(worldMatrix, 1f - EPSILON, fY, 0f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV(fillLevel)).uv2(lightXPos).normal(1f, 0f, 0f).endVertex();			// top right
				translucentBuffer.vertex(worldMatrix, 1f - EPSILON, fY, 1f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV(fillLevel)).uv2(lightXPos).normal(1f, 0f, 0f).endVertex();			// top lef
			}

			// west (x-)
			if (westFaceVisible)
			{
				translucentBuffer.vertex(worldMatrix, 0f + EPSILON, 0f, 0f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV0()).uv2(lightXNeg).normal(-1f, 0f, 0f).endVertex();	// botton left
				translucentBuffer.vertex(worldMatrix, 0f + EPSILON, 0f, 1f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV0()).uv2(lightXNeg).normal(-1f, 0f, 0f).endVertex();	// bottom right
				translucentBuffer.vertex(worldMatrix, 0f + EPSILON, fY, 1f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV(fillLevel)).uv2(lightXNeg).normal(-1f, 0f, 0f).endVertex();	// top right
				translucentBuffer.vertex(worldMatrix, 0f + EPSILON, fY, 0f).color(fluidColorXR, fluidColorXG, fluidColorXB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV(fillLevel)).uv2(lightXNeg).normal(-1f, 0f, 0f).endVertex();	// top lef
			}

			// south (z+)
			if (southFaceVisible)
			{
				translucentBuffer.vertex(worldMatrix, 0f, 0f, 1f - EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV0()).uv2(lightZPos).normal(0f, 0f, 1f).endVertex();	// botton left
				translucentBuffer.vertex(worldMatrix, 1f, 0f, 1f - EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV0()).uv2(lightZPos).normal(0f, 0f, 1f).endVertex();	// bottom right
				translucentBuffer.vertex(worldMatrix, 1f, fY, 1f - EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV(fillLevel)).uv2(lightZPos).normal(0f, 0f, 1f).endVertex();	// top right
				translucentBuffer.vertex(worldMatrix, 0f, fY, 1f - EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV(fillLevel)).uv2(lightZPos).normal(0f, 0f, 1f).endVertex();	// top lef
			}

			// north (z-)
			if (northFaceVisible)
			{
				translucentBuffer.vertex(worldMatrix, 1f, 0f, 0f + EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV0()).uv2(lightZNeg).normal(0f, 0f, -1f).endVertex();	// botton left
				translucentBuffer.vertex(worldMatrix, 0f, 0f, 0f + EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV0()).uv2(lightZNeg).normal(0f, 0f, -1f).endVertex();	// bottom right
				translucentBuffer.vertex(worldMatrix, 0f, fY, 0f + EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV(fillLevel)).uv2(lightZNeg).normal(0f, 0f, -1f).endVertex();			// top right
				translucentBuffer.vertex(worldMatrix, 1f, fY, 0f + EPSILON).color(fluidColorZR, fluidColorZG, fluidColorZB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV(fillLevel)).uv2(lightZNeg).normal(0f, 0f, -1f).endVertex();			// top lef
			}

			// up (y+)
			if (!cullFluidTop)
			{
				translucentBuffer.vertex(worldMatrix, 0f, fY - EPSILON, 1f).color(fluidColorR, fluidColorG, fluidColorB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV0()).uv2(lightYPos).normal(0f, 1f, 0f).endVertex();	// botton left
				translucentBuffer.vertex(worldMatrix, 1f, fY - EPSILON, 1f).color(fluidColorR, fluidColorG, fluidColorB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV0()).uv2(lightYPos).normal(0f, 1f, 0f).endVertex();	// bottom right
				translucentBuffer.vertex(worldMatrix, 1f, fY - EPSILON, 0f).color(fluidColorR, fluidColorG, fluidColorB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV1()).uv2(lightYPos).normal(0f, 1f, 0f).endVertex();	// top right
				translucentBuffer.vertex(worldMatrix, 0f, fY - EPSILON, 0f).color(fluidColorR, fluidColorG, fluidColorB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV1()).uv2(lightYPos).normal(0f, 1f, 0f).endVertex();	// top lef
			}

			// down (y-)
			if (downFaceVisible)
			{
				translucentBuffer.vertex(worldMatrix, 0f, 0f + EPSILON, 0f).color(fluidColorYR, fluidColorYG, fluidColorYB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV0()).uv2(lightYNeg).normal(0f, -1f, 0f).endVertex();	// botton left
				translucentBuffer.vertex(worldMatrix, 1f, 0f + EPSILON, 0f).color(fluidColorYR, fluidColorYG, fluidColorYB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV0()).uv2(lightYNeg).normal(0f, -1f, 0f).endVertex();	// bottom right
				translucentBuffer.vertex(worldMatrix, 1f, 0f + EPSILON, 1f).color(fluidColorYR, fluidColorYG, fluidColorYB, 1f).uv(fluidTexture.getU1(), fluidTexture.getV1()).uv2(lightYNeg).normal(0f, -1f, 0f).endVertex();	// top right
				translucentBuffer.vertex(worldMatrix, 0f, 0f + EPSILON, 1f).color(fluidColorYR, fluidColorYG, fluidColorYB, 1f).uv(fluidTexture.getU0(), fluidTexture.getV1()).uv2(lightYNeg).normal(0f, -1f, 0f).endVertex();	// top lef
			}
		}

		// Frame
		VertexConsumer cutoutBuffer = bufferSource.getBuffer(RenderType.cutoutMipped());

		// east (x+)
		if (eastFaceVisible)
		{
			cutoutBuffer.vertex(worldMatrix, 1f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTexture.getU0(), eastTexture.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// botton left
			cutoutBuffer.vertex(worldMatrix, 1f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTexture.getU1(), eastTexture.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// bottom right
			cutoutBuffer.vertex(worldMatrix, 1f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTexture.getU1(), eastTexture.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top right
			cutoutBuffer.vertex(worldMatrix, 1f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTexture.getU0(), eastTexture.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top lef

			if (eastFaceVisibleI)
			{
				cutoutBuffer.vertex(worldMatrix, 1f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTextureI.getU0(), eastTextureI.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// botton left
				cutoutBuffer.vertex(worldMatrix, 1f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTextureI.getU1(), eastTextureI.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// bottom right
				cutoutBuffer.vertex(worldMatrix, 1f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTextureI.getU1(), eastTextureI.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top right
				cutoutBuffer.vertex(worldMatrix, 1f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(eastTextureI.getU0(), eastTextureI.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top lef
			}
		}

		// west (x-)
		if (westFaceVisible)
		{
			cutoutBuffer.vertex(worldMatrix, 0f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTexture.getU0(), westTexture.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// botton left
			cutoutBuffer.vertex(worldMatrix, 0f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTexture.getU1(), westTexture.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// bottom right
			cutoutBuffer.vertex(worldMatrix, 0f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTexture.getU1(), westTexture.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top right
			cutoutBuffer.vertex(worldMatrix, 0f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTexture.getU0(), westTexture.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top lef

			if (westFaceVisibleI)
			{
				cutoutBuffer.vertex(worldMatrix, 0f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTextureI.getU0(), westTextureI.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// botton left
				cutoutBuffer.vertex(worldMatrix, 0f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTextureI.getU1(), westTextureI.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// bottom right
				cutoutBuffer.vertex(worldMatrix, 0f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTextureI.getU1(), westTextureI.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top right
				cutoutBuffer.vertex(worldMatrix, 0f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 1f).uv(westTextureI.getU0(), westTextureI.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top lef
			}
		}

		// south (z+)
		if (southFaceVisible)
		{
			cutoutBuffer.vertex(worldMatrix, 0f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTexture.getU0(), southTexture.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// botton left
			cutoutBuffer.vertex(worldMatrix, 1f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTexture.getU1(), southTexture.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// bottom right
			cutoutBuffer.vertex(worldMatrix, 1f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTexture.getU1(), southTexture.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top right
			cutoutBuffer.vertex(worldMatrix, 0f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTexture.getU0(), southTexture.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top lef

			if (southFaceVisibleI)
			{
				cutoutBuffer.vertex(worldMatrix, 1f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTextureI.getU0(), southTextureI.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// botton left
				cutoutBuffer.vertex(worldMatrix, 0f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTextureI.getU1(), southTextureI.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// bottom right
				cutoutBuffer.vertex(worldMatrix, 0f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTextureI.getU1(), southTextureI.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top right
				cutoutBuffer.vertex(worldMatrix, 1f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(southTextureI.getU0(), southTextureI.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top lef
			}
		}

		// north (z-)
		if (northFaceVisible)
		{
			cutoutBuffer.vertex(worldMatrix, 1f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTexture.getU0(), northTexture.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// botton left
			cutoutBuffer.vertex(worldMatrix, 0f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTexture.getU1(), northTexture.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// bottom right
			cutoutBuffer.vertex(worldMatrix, 0f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTexture.getU1(), northTexture.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top right
			cutoutBuffer.vertex(worldMatrix, 1f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTexture.getU0(), northTexture.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top lef

			if (northFaceVisibleI)
			{
				cutoutBuffer.vertex(worldMatrix, 0f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTextureI.getU0(), northTextureI.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// botton left
				cutoutBuffer.vertex(worldMatrix, 1f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTextureI.getU1(), northTextureI.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// bottom right
				cutoutBuffer.vertex(worldMatrix, 1f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTextureI.getU1(), northTextureI.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top right
				cutoutBuffer.vertex(worldMatrix, 0f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 1f).uv(northTextureI.getU0(), northTextureI.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top lef
			}
		}

		// up (y+)
		if (upFaceVisible)
		{
			cutoutBuffer.vertex(worldMatrix, 1f, 1f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU0(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top right
			cutoutBuffer.vertex(worldMatrix, 0f, 1f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU1(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top lef
			cutoutBuffer.vertex(worldMatrix, 0f, 1f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU1(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// botton left
			cutoutBuffer.vertex(worldMatrix, 1f, 1f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU0(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// bottom right

			if (upFaceVisibleI)
			{
				cutoutBuffer.vertex(worldMatrix, 0f, 1f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU1(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// botton left
				cutoutBuffer.vertex(worldMatrix, 1f, 1f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU0(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// bottom right
				cutoutBuffer.vertex(worldMatrix, 1f, 1f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU0(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top right
				cutoutBuffer.vertex(worldMatrix, 0f, 1f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(upTexture.getU1(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top lef
			}
		}

		// down (y-)
		if (downFaceVisible)
		{
			cutoutBuffer.vertex(worldMatrix, 0f, 0f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU1(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// botton left
			cutoutBuffer.vertex(worldMatrix, 1f, 0f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU0(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// bottom right
			cutoutBuffer.vertex(worldMatrix, 1f, 0f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU0(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top right
			cutoutBuffer.vertex(worldMatrix, 0f, 0f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU1(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top lef

			if (downFaceVisibleI)
			{
				cutoutBuffer.vertex(worldMatrix, 1f, 0f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU0(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top right
				cutoutBuffer.vertex(worldMatrix, 0f, 0f, 0f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU1(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top lef
				cutoutBuffer.vertex(worldMatrix, 0f, 0f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU1(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// botton left
				cutoutBuffer.vertex(worldMatrix, 1f, 0f, 1f).color(yLightFactor, yLightFactor, yLightFactor, 1f).uv(downTexture.getU0(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// bottom right
			}
		}

		poses.popPose();
	}
}