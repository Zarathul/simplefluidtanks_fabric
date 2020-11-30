package net.zarathul.simplefluidtanks.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
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
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;

import java.util.function.Function;

public class ValveBlockRenderer extends BlockEntityRenderer<ValveBlockEntity>
{
	private final float yPosLightFactor = 1.0f;
	private final float yNegLightFactor = 0.5f;
	private final float zLightFactor = 0.8f;
	private final float xLightFactor = 0.6f;

	public static TextureAtlasSprite normalTexture;
	public static TextureAtlasSprite grateTexture;
	public static TextureAtlasSprite ioTexture;

	static
	{
		Function<ResourceLocation, TextureAtlasSprite> spriteGetter = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
		normalTexture = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/valve"));
		grateTexture = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/valve_grate"));
		ioTexture = spriteGetter.apply(new ResourceLocation(SimpleFluidTanks.MOD_ID, "block/valve_io"));
	}

	public ValveBlockRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher)
	{
		super(blockEntityRenderDispatcher);
	}

	@Override
	public boolean shouldRenderOffScreen(ValveBlockEntity blockEntity)
	{
		return super.shouldRenderOffScreen(blockEntity);
	}

	@Override
	public void render(ValveBlockEntity entity, float tickDelta, PoseStack poses, MultiBufferSource bufferSource, int light, int overlay)
	{
		poses.pushPose();

		Level world = entity.getLevel();
		if (world == null) return;

		BlockPos pos = entity.getBlockPos();

		Minecraft.getInstance().getTextureManager().bind(InventoryMenu.BLOCK_ATLAS);

		Matrix4f worldMatrix = poses.last().pose();
		TextureAtlasSprite eastTexture, westTexture, southTexture, northTexture, upTexture, downTexture;

		//int combinedLight = 0x00f000f0;
		int lightXPos = world.getRawBrightness(pos.east(), world.getSkyDarken()) * 16;
		int lightXNeg = world.getRawBrightness(pos.west(), world.getSkyDarken()) * 16;
		int lightZPos = world.getRawBrightness(pos.south(), world.getSkyDarken()) * 16;
		int lightZNeg = world.getRawBrightness(pos.north(), world.getSkyDarken()) * 16;
		int lightYPos = world.getRawBrightness(pos.above(), world.getSkyDarken()) * 16;
		int lightYNeg = world.getRawBrightness(pos.below(), world.getSkyDarken()) * 16;

		if (entity.hasTanks())
		{
			eastTexture = (entity.isFacingTank(Direction.EAST)) ? grateTexture : ioTexture;
			westTexture = (entity.isFacingTank(Direction.WEST)) ? grateTexture : ioTexture;
			southTexture = (entity.isFacingTank(Direction.SOUTH)) ? grateTexture : ioTexture;
			northTexture = (entity.isFacingTank(Direction.NORTH)) ? grateTexture : ioTexture;
			upTexture = (entity.isFacingTank(Direction.UP)) ? grateTexture : ioTexture;
			downTexture = (entity.isFacingTank(Direction.DOWN)) ? grateTexture : ioTexture;
		}
		else
		{
			Direction facing = entity.getFacing();
			eastTexture = (facing == Direction.EAST) ? ioTexture : normalTexture;
			westTexture = (facing == Direction.WEST) ? ioTexture : normalTexture;
			southTexture = (facing == Direction.SOUTH) ? ioTexture : normalTexture;
			northTexture = (facing == Direction.NORTH) ? ioTexture : normalTexture;
			upTexture = grateTexture;
			downTexture = normalTexture;
		}

		VertexConsumer buffer = bufferSource.getBuffer(RenderType.solid());

		// east (x+)
		buffer.vertex(worldMatrix, 1f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU1(), eastTexture.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// bottom right
		buffer.vertex(worldMatrix, 1f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU1(), eastTexture.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top right
		buffer.vertex(worldMatrix, 1f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU0(), eastTexture.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top lef
		buffer.vertex(worldMatrix, 1f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU0(), eastTexture.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// botton left
		// west (x-)
		buffer.vertex(worldMatrix, 0f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU1(), westTexture.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// bottom right
		buffer.vertex(worldMatrix, 0f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU1(), westTexture.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top right
		buffer.vertex(worldMatrix, 0f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU0(), westTexture.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top lef
		buffer.vertex(worldMatrix, 0f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU0(), westTexture.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// botton left
		// south (z+)
		buffer.vertex(worldMatrix, 1f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU1(), southTexture.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// bottom right
		buffer.vertex(worldMatrix, 1f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU1(), southTexture.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top right
		buffer.vertex(worldMatrix, 0f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU0(), southTexture.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top lef
		buffer.vertex(worldMatrix, 0f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU0(), southTexture.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// botton left
		// north (z-)
		buffer.vertex(worldMatrix, 0f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU1(), northTexture.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// bottom right
		buffer.vertex(worldMatrix, 0f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU1(), northTexture.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top right
		buffer.vertex(worldMatrix, 1f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU0(), northTexture.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top lef
		buffer.vertex(worldMatrix, 1f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU0(), northTexture.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// botton left
		// up (y+)
		buffer.vertex(worldMatrix, 0f, 1f, 0f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU1(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// bottom right
		buffer.vertex(worldMatrix, 0f, 1f, 1f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU1(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top right
		buffer.vertex(worldMatrix, 1f, 1f, 1f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU0(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top lef
		buffer.vertex(worldMatrix, 1f, 1f, 0f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU0(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// botton left
		// down (y-)
		buffer.vertex(worldMatrix, 0f, 0f, 1f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU1(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// bottom right
		buffer.vertex(worldMatrix, 0f, 0f, 0f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU1(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top right
		buffer.vertex(worldMatrix, 1f, 0f, 0f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU0(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top lef
		buffer.vertex(worldMatrix, 1f, 0f, 1f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU0(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// botton left

		poses.popPose();
	}
}
