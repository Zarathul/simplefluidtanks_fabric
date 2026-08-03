package net.zarathul.simplefluidtanks.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.ValveBlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ValveBlockRenderer implements BlockEntityRenderer<ValveBlockEntity, ValveBlockRenderer.ValveBlockEntityRenderState>
{
	private final float yPosLightFactor = 1.0f;
	private final float yNegLightFactor = 0.5f;
	private final float zLightFactor = 0.8f;
	private final float xLightFactor = 0.6f;

	private final SpriteGetter sprites;

	public ValveBlockRenderer(final BlockEntityRendererProvider.Context context)
	{
		sprites = context.sprites();
//		normalTexture = spriteGetter.apply(Identifier.fromNamespaceAndPath(SimpleFluidTanks.MOD_ID, "block/valve"));
//		grateTexture = spriteGetter.apply(Identifier.fromNamespaceAndPath(SimpleFluidTanks.MOD_ID, "block/valve_grate"));
//		ioTexture = spriteGetter.apply(Identifier.fromNamespaceAndPath(SimpleFluidTanks.MOD_ID, "block/valve_io"));
	}

	@Override
	public ValveBlockEntityRenderState createRenderState()
	{
		return new ValveBlockEntityRenderState();
	}

	@Override
	public void extractRenderState(ValveBlockEntity blockEntity, ValveBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
	{
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.facing = blockEntity.getFacing();
		state.tankFacingSides = blockEntity.getTankFacingSides();
	}

	@Override
	public void submit(ValveBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
	{
		poseStack.pushPose();

		var renderType = RenderType.create("", RenderSetup.builder(RenderPipelines.SOLID_BLOCK).createRenderSetup());
		List<BlockStateModelPart> parts = new ArrayList<>();
		submitNodeCollector.submitBlockModel(poseStack, renderType, parts, null, 0, 0, 0);
		poseStack.popPose();
	}

//
//	@Override
//	public boolean shouldRenderOffScreen(ValveBlockEntity blockEntity)
//	{
//		return super.shouldRenderOffScreen(blockEntity);
//	}

//	@Override
//	public void render(ValveBlockEntity entity, float tickDelta, PoseStack poses, MultiBufferSource bufferSource, int light, int overlay)
//	{
//		poses.pushPose();
//
//		Level world = entity.getLevel();
//		if (world == null) return;
//
//		BlockPos pos = entity.getBlockPos();
//
//		Minecraft.getInstance().getTextureManager().bind(InventoryMenu.BLOCK_ATLAS);
//
//		Matrix4f worldMatrix = poses.last().pose();
//		TextureAtlasSprite eastTexture, westTexture, southTexture, northTexture, upTexture, downTexture;
//
//		//int combinedLight = 0x00f000f0;
//		int lightXPos = world.getRawBrightness(pos.east(), world.getSkyDarken()) * 16;
//		int lightXNeg = world.getRawBrightness(pos.west(), world.getSkyDarken()) * 16;
//		int lightZPos = world.getRawBrightness(pos.south(), world.getSkyDarken()) * 16;
//		int lightZNeg = world.getRawBrightness(pos.north(), world.getSkyDarken()) * 16;
//		int lightYPos = world.getRawBrightness(pos.above(), world.getSkyDarken()) * 16;
//		int lightYNeg = world.getRawBrightness(pos.below(), world.getSkyDarken()) * 16;
//
//		if (entity.hasTanks())
//		{
//			eastTexture = (entity.isFacingTank(Direction.EAST)) ? grateTexture : ioTexture;
//			westTexture = (entity.isFacingTank(Direction.WEST)) ? grateTexture : ioTexture;
//			southTexture = (entity.isFacingTank(Direction.SOUTH)) ? grateTexture : ioTexture;
//			northTexture = (entity.isFacingTank(Direction.NORTH)) ? grateTexture : ioTexture;
//			upTexture = (entity.isFacingTank(Direction.UP)) ? grateTexture : ioTexture;
//			downTexture = (entity.isFacingTank(Direction.DOWN)) ? grateTexture : ioTexture;
//		}
//		else
//		{
//			Direction facing = entity.getFacing();
//			eastTexture = (facing == Direction.EAST) ? ioTexture : normalTexture;
//			westTexture = (facing == Direction.WEST) ? ioTexture : normalTexture;
//			southTexture = (facing == Direction.SOUTH) ? ioTexture : normalTexture;
//			northTexture = (facing == Direction.NORTH) ? ioTexture : normalTexture;
//			upTexture = grateTexture;
//			downTexture = normalTexture;
//		}
//
//		VertexConsumer buffer = bufferSource.getBuffer(RenderType.solid());
//
//		// east (x+)
//		buffer.vertex(worldMatrix, 1f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU1(), eastTexture.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// bottom right
//		buffer.vertex(worldMatrix, 1f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU1(), eastTexture.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top right
//		buffer.vertex(worldMatrix, 1f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU0(), eastTexture.getV1()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// top lef
//		buffer.vertex(worldMatrix, 1f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(eastTexture.getU0(), eastTexture.getV0()).uv2(lightXPos).overlayCoords(overlay).normal(1f, 0f, 0f).endVertex();	// botton left
//		// west (x-)
//		buffer.vertex(worldMatrix, 0f, 0f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU1(), westTexture.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// bottom right
//		buffer.vertex(worldMatrix, 0f, 1f, 1f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU1(), westTexture.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top right
//		buffer.vertex(worldMatrix, 0f, 1f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU0(), westTexture.getV1()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// top lef
//		buffer.vertex(worldMatrix, 0f, 0f, 0f).color(xLightFactor, xLightFactor, xLightFactor, 0f).uv(westTexture.getU0(), westTexture.getV0()).uv2(lightXNeg).overlayCoords(overlay).normal(-1f, 0f, 0f).endVertex();	// botton left
//		// south (z+)
//		buffer.vertex(worldMatrix, 1f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU1(), southTexture.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// bottom right
//		buffer.vertex(worldMatrix, 1f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU1(), southTexture.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top right
//		buffer.vertex(worldMatrix, 0f, 1f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU0(), southTexture.getV1()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// top lef
//		buffer.vertex(worldMatrix, 0f, 0f, 1f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(southTexture.getU0(), southTexture.getV0()).uv2(lightZPos).overlayCoords(overlay).normal(0f, 0f, 1f).endVertex();	// botton left
//		// north (z-)
//		buffer.vertex(worldMatrix, 0f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU1(), northTexture.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// bottom right
//		buffer.vertex(worldMatrix, 0f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU1(), northTexture.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top right
//		buffer.vertex(worldMatrix, 1f, 1f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU0(), northTexture.getV1()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// top lef
//		buffer.vertex(worldMatrix, 1f, 0f, 0f).color(zLightFactor, zLightFactor, zLightFactor, 0f).uv(northTexture.getU0(), northTexture.getV0()).uv2(lightZNeg).overlayCoords(overlay).normal(0f, 0f, -1f).endVertex();	// botton left
//		// up (y+)
//		buffer.vertex(worldMatrix, 0f, 1f, 0f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU1(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// bottom right
//		buffer.vertex(worldMatrix, 0f, 1f, 1f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU1(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top right
//		buffer.vertex(worldMatrix, 1f, 1f, 1f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU0(), upTexture.getV1()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// top lef
//		buffer.vertex(worldMatrix, 1f, 1f, 0f).color(yPosLightFactor, yPosLightFactor, yPosLightFactor, 0f).uv(upTexture.getU0(), upTexture.getV0()).uv2(lightYPos).overlayCoords(overlay).normal(0f, 1f, 0f).endVertex();	// botton left
//		// down (y-)
//		buffer.vertex(worldMatrix, 0f, 0f, 1f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU1(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// bottom right
//		buffer.vertex(worldMatrix, 0f, 0f, 0f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU1(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top right
//		buffer.vertex(worldMatrix, 1f, 0f, 0f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU0(), downTexture.getV1()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// top lef
//		buffer.vertex(worldMatrix, 1f, 0f, 1f).color(yNegLightFactor, yNegLightFactor, yNegLightFactor, 0f).uv(downTexture.getU0(), downTexture.getV0()).uv2(lightYNeg).overlayCoords(overlay).normal(0f, -1f, 0f).endVertex();	// botton left
//
//		poses.popPose();
//	}

	@Environment(EnvType.CLIENT)
	public class ValveBlockEntityRenderState extends BlockEntityRenderState
	{
		public Direction facing = Direction.NORTH;
		public byte tankFacingSides = -1;
	}
}
