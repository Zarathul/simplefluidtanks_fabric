package net.zarathul.simplefluidtanks.rendering;

import com.mojang.blaze3d.platform.Transparency;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.entities.TankBlockEntity;
import net.zarathul.simplefluidtanks.common.Utils;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class TankBlockStateModel extends WrapperBlockStateModel
{
	private TextureAtlasSprite[] tankSprites;

	public TankBlockStateModel(BlockStateModel model)
	{
		super(model);
	}

	private void init()
	{
		tankSprites = new TextureAtlasSprite[16];
		tankSprites[ 0] = getModTextureAtlasSprite("tank");
		tankSprites[ 1] = getModTextureAtlasSprite("tank_bottom");
		tankSprites[ 2] = getModTextureAtlasSprite("tank_bottom_left");
		tankSprites[ 3] = getModTextureAtlasSprite("tank_bottom_right");
		tankSprites[ 4] = getModTextureAtlasSprite("tank_left");
		tankSprites[ 5] = getModTextureAtlasSprite("tank_left_right");
		tankSprites[ 6] = getModTextureAtlasSprite("tank_left_right_bottom");
		tankSprites[ 7] = getModTextureAtlasSprite("tank_left_right_top");
		tankSprites[ 8] = getModTextureAtlasSprite("tank_none");
		tankSprites[ 9] = getModTextureAtlasSprite("tank_right");
		tankSprites[10] = getModTextureAtlasSprite("tank_top");
		tankSprites[11] = getModTextureAtlasSprite("tank_top_bottom");
		tankSprites[12] = getModTextureAtlasSprite("tank_top_bottom_left");
		tankSprites[13] = getModTextureAtlasSprite("tank_top_bottom_right");
		tankSprites[14] = getModTextureAtlasSprite("tank_top_left");
		tankSprites[15] = getModTextureAtlasSprite("tank_top_right");
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags()
	{
		return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED;
	}

	@Override
	public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag)
	{
		return (materialFlags() & flag) != 0;
	}

	@Override
	public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random)
	{
		return super.createGeometryKey(level, pos, state, random);
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random)
	{
		return materialFlags();
	}

	@Override
	public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag)
	{
		return hasMaterialFlag(flag);
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest)
	{
		if (tankSprites == null) init();

		var optionalTankBlockEntity = level.getBlockEntity(pos, SimpleFluidTanks.blockEntityTypeTank);
		if (optionalTankBlockEntity.isEmpty() || !optionalTankBlockEntity.get().isPartOfTank())
		{
			emitEmptyUnconnectedTank(emitter);
			return;
		}

		TankBlockEntity tankBlockEntity = optionalTankBlockEntity.get();
		int[] textureIndexes = tankBlockEntity.getTextureIndexes();

		ChunkSectionLayer layer = ChunkSectionLayer.byTransparency(Transparency.TRANSLUCENT);

		// Fluid

		if (!tankBlockEntity.isEmpty())
		{
			Minecraft mc = Minecraft.getInstance();
			Fluid tankFluid = tankBlockEntity.getFluid();
			TextureAtlasSprite fluidSprite = mc.getModelManager().getFluidStateModelSet().get(tankFluid.defaultFluidState()).stillMaterial().sprite();
			int fluidColor = FluidVariantRendering.getColor(FluidVariant.of(tankFluid));

			int fillLevel = tankBlockEntity.getFillLevel();
			float fillLevelFactor = Math.min((float)fillLevel / SimpleFluidTanks.MAX_FILL_LEVEL, 1f);

			var tankBlockEntityAbove = level.getBlockEntity(pos.above(), SimpleFluidTanks.blockEntityTypeTank);
			boolean cullFluidTop = (tankBlockEntityAbove.isPresent() && (tankBlockEntity.isPartOfSameTank(tankBlockEntityAbove.get())) && (tankBlockEntityAbove.get().getFillLevel() > 0));
			final float epsilon = 0.00001f;		// TODO: Apply epsilon only to sides with frame on top to avoid the tiny blank line in between blocks

			for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++)
			{
				Direction direction = ModelHelper.faceFromIndex(i);
				// Don't render faces inside the tank structure, except top, but only if the tank above is empty.
				if ((direction == Direction.UP && cullFluidTop) || (direction != Direction.UP && tankBlockEntity.isConnected(direction))) continue;

				float fY = (direction == Direction.UP || direction == Direction.DOWN) ? 1.0f : fillLevelFactor;
				float depth = (direction == Direction.UP) ? 1f - fillLevelFactor: 0f;

				emitter.square(direction, 0f + epsilon, 0f + epsilon, 1f - epsilon, fY - epsilon, depth + epsilon);
				emitter.uv(0, fluidSprite.getU0(), fluidSprite.getV(fY));
				emitter.uv(1, fluidSprite.getU0(), fluidSprite.getV0());
				emitter.uv(2, fluidSprite.getU1(), fluidSprite.getV0());
				emitter.uv(3, fluidSprite.getU1(), fluidSprite.getV(fY));
				emitter.color(fluidColor, fluidColor, fluidColor, fluidColor);
				emitter.chunkLayer(layer);
				emitter.itemRenderType(Sheets.translucentBlockItemSheet());		// Sheets.cutoutBlockItemSheet()
				emitter.ambientOcclusion(TriState.FALSE);
				emitter.shadeMode(ShadeMode.VANILLA);
				emitter.emit();
			}
		}

		// Frame

		for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++)
		{
			Direction direction = ModelHelper.faceFromIndex(i);
			if (tankBlockEntity.isConnected(direction)) continue;	// Don't render faces inside the tank structure.
			TextureAtlasSprite tankSprite = tankSprites[textureIndexes[direction.get3DDataValue()]];

			emitter.square(direction, 0f, 0f, 1f, 1f, 0f);
			emitter.uv(0, tankSprite.getU0(), tankSprite.getV0());
			emitter.uv(1, tankSprite.getU0(), tankSprite.getV1());
			emitter.uv(2, tankSprite.getU1(), tankSprite.getV1());
			emitter.uv(3, tankSprite.getU1(), tankSprite.getV0());
			emitter.ambientOcclusion(TriState.FALSE);
			emitter.shadeMode(ShadeMode.VANILLA);
			emitter.emit();
		}



//		var tankSpriteId = Sheets.BLOCKS_MAPPER.apply(Utils.createModIdentifier("tank"));
//		var tankSprite = mc.getAtlasManager().get(tankSpriteId);
//
//		var waterSpriteId = Sheets.BLOCKS_MAPPER.apply(Identifier.withDefaultNamespace("water_still"));
//		var waterSprite = mc.getAtlasManager().get(waterSpriteId);
//		int waterColor = BiomeColors.getAverageWaterColor(level, pos);
//
//		for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++) {
//			Direction face = ModelHelper.faceFromIndex(i);
//
////			if (cullTest.test(face)) {
////				// Skip entire quad list if possible.
////				continue;
////			}
//
////			emitter.cullFace(face);
//
////			var waterMaterial = TextureMapping.getBlockTexture(Blocks.WATER);//.withForceTranslucent(true);
//			var bakedWaterMaterial = new Material.Baked(waterSprite, false);
//
//			emitter.ambientOcclusion(TriState.FALSE);
//			emitter.shadeMode(ShadeMode.VANILLA);
//
//			// Water
//			ChunkSectionLayer layer = ChunkSectionLayer.byTransparency(Transparency.TRANSLUCENT);
//			emitter.chunkLayer(layer);
//			emitter.itemRenderType(Sheets.translucentBlockItemSheet());		// Sheets.cutoutBlockItemSheet()
//
//			emitter.square(face, 0f, 0f, 1f, 1f, 0.0001f);
//			emitter.uv(0, waterSprite.getU0(), waterSprite.getV0());
//			emitter.uv(1, waterSprite.getU0(), waterSprite.getV1());
//			emitter.uv(2, waterSprite.getU1(), waterSprite.getV1());
//			emitter.uv(3, waterSprite.getU1(), waterSprite.getV0());
//			emitter.color(0, waterColor);
//			emitter.color(1, waterColor);
//			emitter.color(2, waterColor);
//			emitter.color(3, waterColor);
//			emitter.emit();
//
//			// Tank
//			emitter.square(face, 0f, 0f, 1f, 1f, 0f);
//			emitter.uv(0, tankSprite.getU0(), tankSprite.getV0());
//			emitter.uv(1, tankSprite.getU0(), tankSprite.getV1());
//			emitter.uv(2, tankSprite.getU1(), tankSprite.getV1());
//			emitter.uv(3, tankSprite.getU1(), tankSprite.getV0());
//			emitter.emit();
//		}
	}

	private void emitEmptyUnconnectedTank(QuadEmitter emitter)
	{
		var tankSprite = tankSprites[0];

		for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++)
		{
			Direction face = ModelHelper.faceFromIndex(i);

			emitter.square(face, 0f, 0f, 1f, 1f, 0f);
			emitter.uv(0, tankSprite.getU0(), tankSprite.getV0());
			emitter.uv(1, tankSprite.getU0(), tankSprite.getV1());
			emitter.uv(2, tankSprite.getU1(), tankSprite.getV1());
			emitter.uv(3, tankSprite.getU1(), tankSprite.getV0());
			emitter.emit();
		}
	}

	private static TextureAtlasSprite getModTextureAtlasSprite(String textureName)
	{
		return getTextureAtlasSprite(Utils.createModIdentifier(textureName));
	}

	private static TextureAtlasSprite getMcTextureAtlasSprite(String textureName)
	{
		return getTextureAtlasSprite(Identifier.withDefaultNamespace(textureName));
	}

	private static TextureAtlasSprite getTextureAtlasSprite(Identifier textureId)
	{
		var spriteId = Sheets.BLOCKS_MAPPER.apply(textureId);
		return Minecraft.getInstance().getAtlasManager().get(spriteId);
	}

//
//	private static int[] getTextureIndexes(BlockAndTintGetter level, BlockPos pos)
//	{
//
//		return optionalTankBlockEntity.get().getTextureIndexes();
//
//		var tankBlockEntity = optionalTankBlockEntity.get();
//
//		for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++)
//		{
//			textureIndexes[i] = tankBlockEntity.getTextureIndex(ModelHelper.faceFromIndex(i));
//		}
//
//		return textureIndexes;
//	}
}
