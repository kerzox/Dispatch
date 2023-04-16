package mod.kerzox.dispatch.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.List;
import java.util.Objects;

public class RenderingUtil {

    public static void renderBlockModel(PoseStack poseStack, MultiBufferSource buffer, BlockState blockState, RenderType type, int brightness) {

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState,
                poseStack,
                buffer,
                brightness,
                0,
                ModelData.EMPTY,
                type);
    }

    public static void renderBlockModelWithColor(BakedModel bakedmodel, BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, RenderType type, int brightness, float r, float g, float b) {
        RenderShape rendershape = pState.getRenderShape();
        if (rendershape != RenderShape.INVISIBLE) {
            switch (rendershape) {
                case MODEL:
                    for (RenderType rt : bakedmodel.getRenderTypes(pState, RandomSource.create(42), ModelData.EMPTY))
                        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pPoseStack.last(), pBufferSource.getBuffer(type != null ? type : net.minecraftforge.client.RenderTypeHelper.getEntityRenderType(rt, false)), pState, bakedmodel, r, g, b, brightness, 0, ModelData.EMPTY, rt);
                    break;
                case ENTITYBLOCK_ANIMATED:
                    ItemStack stack = new ItemStack(pState.getBlock());
                    net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, pPoseStack, pBufferSource, brightness, 0);
            }

        }
    }

    public static void renderBlockModelWithColor(BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, RenderType type, int brightness, float r, float g, float b) {
        RenderShape rendershape = pState.getRenderShape();
        if (rendershape != RenderShape.INVISIBLE) {
            switch (rendershape) {
                case MODEL:
                    BakedModel bakedmodel = Minecraft.getInstance().getBlockRenderer().getBlockModel(pState);
                    for (RenderType rt : bakedmodel.getRenderTypes(pState, RandomSource.create(42), ModelData.EMPTY))
                        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(pPoseStack.last(), pBufferSource.getBuffer(type != null ? type : net.minecraftforge.client.RenderTypeHelper.getEntityRenderType(rt, false)), pState, bakedmodel, r, g, b, brightness, 0, ModelData.EMPTY, rt);
                    break;
                case ENTITYBLOCK_ANIMATED:
                    ItemStack stack = new ItemStack(pState.getBlock());
                    net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, pPoseStack, pBufferSource, brightness, 0);
            }

        }
    }

    public static void renderModelFromHit(PoseStack poseStack, Level level, BlockHitResult block, BlockPos pos, MultiBufferSource source, RenderType type, int brightness) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateWithAO(
                Objects.requireNonNull(level),
                Minecraft.getInstance().getBlockRenderer().getBlockModel(level.getBlockState(block.getBlockPos())),
                level.getBlockState(block.getBlockPos()),
                pos,
                poseStack,
                source.getBuffer(type),
                false,
                RandomSource.create(),
                0,
                brightness, ModelData.EMPTY, type);
    }

    public static void renderSolidModel(PoseStack pMatrixStack, MultiBufferSource pBuffer, BakedModel model, BlockEntity te, BlockPos pos, int overlay) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateWithAO(
                Objects.requireNonNull(te.getLevel()),
                model,
                te.getBlockState(),
                pos,
                pMatrixStack,
                pBuffer.getBuffer(RenderType.solid()),
                false,
                RandomSource.create(),
                0,
                overlay, ModelData.EMPTY, RenderType.solid());
    }

    public static void addVertex(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(u, v)
                .uv2(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static void addVertex(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v, int color) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(color)
                .uv(u, v)
                .uv2(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static float[] convertColor(int color) {
        float alpha = ((color >> 24) & 0xFF) / 255F;
        float red = ((color >> 16) & 0xFF) / 255F;
        float green = ((color >> 8) & 0xFF) / 255F;
        float blue = ((color) & 0xFF) / 255F;
        return new float[] { red, green, blue, alpha };
    }

    public static void drawSpriteGrid(PoseStack mStack, int xPos, int yPos, int blitOffset, int xSize, int ySize, TextureAtlasSprite sprite, int repeatX, int repeatY) {
        for (int iX = 0; iX < repeatX; iX++) {
            for (int iY = 0; iY < repeatY; iY++) {
                Gui.blit(mStack, xPos + (xSize * iX), yPos + (ySize * iY), blitOffset, xSize, ySize, sprite);
            }
        }
    }

    public static void drawSpriteAsQuads(PoseStack pPoseStack, VertexConsumer vertexConsumer, TextureAtlasSprite sprite, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int tint) {
        drawSpriteAsQuads(pPoseStack, vertexConsumer, sprite, minX, minY, minZ, maxX, maxY, maxZ, tint, false, false);
    }

    public static void drawSpriteAsQuads(PoseStack pPoseStack, VertexConsumer vertexConsumer, TextureAtlasSprite sprite, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int tint, boolean drawTop, boolean drawBottom) {

        // north
        addVertex(vertexConsumer, pPoseStack, minX, maxY, minZ, sprite.getU(0), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, maxY, minZ, sprite.getU(16), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, minY, minZ, sprite.getU(16), sprite.getV(0), tint);
        addVertex(vertexConsumer, pPoseStack, minX, minY, minZ, sprite.getU(0), sprite.getV(0), tint);

        // south
        addVertex(vertexConsumer, pPoseStack, maxX, maxY, maxZ, sprite.getU(0), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, maxY, maxZ, sprite.getU(16), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, minY, maxZ, sprite.getU(16), sprite.getV(0), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, minY, maxZ, sprite.getU(0), sprite.getV(0), tint);

        // east
        addVertex(vertexConsumer, pPoseStack, maxX, maxY, minZ, sprite.getU(0), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, maxY, maxZ, sprite.getU(16), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, minY, maxZ, sprite.getU(16), sprite.getV(0), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, minY, minZ, sprite.getU(0), sprite.getV(0), tint);

        // west
        addVertex(vertexConsumer, pPoseStack, minX, maxY, maxZ, sprite.getU(0), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, maxY, minZ, sprite.getU(16), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, minY, minZ, sprite.getU(16), sprite.getV(0), tint);
        addVertex(vertexConsumer, pPoseStack, minX, minY, maxZ, sprite.getU(0), sprite.getV(0), tint);

        // top
        addVertex(vertexConsumer, pPoseStack, maxX, maxY, minZ, sprite.getU(0), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, maxY, minZ, sprite.getU(16), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, maxY, maxZ, sprite.getU(16), sprite.getV(0), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, maxY, maxZ, sprite.getU(0), sprite.getV(0), tint);

        // bottom
        addVertex(vertexConsumer, pPoseStack, maxX, minY, minZ, sprite.getU(0), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, minY, minZ, sprite.getU(16), sprite.getV(16), tint);
        addVertex(vertexConsumer, pPoseStack, minX, minY, maxZ, sprite.getU(16), sprite.getV(0), tint);
        addVertex(vertexConsumer, pPoseStack, maxX, minY, maxZ, sprite.getU(0), sprite.getV(0), tint);

    }

    public static QuadBakingVertexConsumer addVertex(QuadBakingVertexConsumer baker, Vector3f pos, float u, float v, int color, Direction direction) {
        baker.vertex(pos.x(),pos.y(),pos.z());
        baker.color(color);
        baker.uv(u, v);
        baker.uv2(0, 240);
        baker.normal(1, 0, 0);
        baker.setDirection(direction);
        baker.endVertex();
        return baker;
    }

    public static Vector3f[] getVerticesFromDirection(Direction direction, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        switch (direction) {
            case NORTH -> {
                return new Vector3f[]{new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, minY, minZ), new Vector3f(minX, minY, minZ)};
            }
            case SOUTH -> {
                return new Vector3f[]{new Vector3f(maxX, maxY, maxZ), new Vector3f(minX, maxY, maxZ), new Vector3f(minX, minY, maxZ), new Vector3f(maxX, minY, maxZ)};
            }
            case EAST -> {
                return new Vector3f[]{new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, maxY, maxZ), new Vector3f(maxX, minY, maxZ), new Vector3f(maxX, minY, minZ)};
            }
            case WEST -> {
                return new Vector3f[]{new Vector3f(minX, maxY, maxZ), new Vector3f(minX, maxY, minZ), new Vector3f(minX, minY, minZ), new Vector3f(minX, minY, maxZ)};
            }
            case UP -> {
                return new Vector3f[]{new Vector3f(maxX, maxY, minZ), new Vector3f(minX, maxY, minZ), new Vector3f(minX, maxY, maxZ), new Vector3f(maxX, maxY, maxZ)};
            }
            case DOWN -> {
                return new Vector3f[]{new Vector3f(maxX, minY, minZ), new Vector3f(minX, minY, minZ), new Vector3f(minX, minY, maxZ), new Vector3f(maxX, minY, maxZ)};
            }
        }
        return null;
    }

    public static BakedQuad bakeQuad(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float u1, float v1, float u2, float v2, TextureAtlasSprite sprite, int tint, Direction direction) {
        BakedQuad[] quad = new BakedQuad[1];
        QuadBakingVertexConsumer baker = new QuadBakingVertexConsumer(q -> quad[0] = q);
        baker.setSprite(sprite);
        Vector3f[] vertices = getVerticesFromDirection(direction, minX, minY, minZ, maxX, maxY, maxZ);
        if (vertices == null) return null;
        addVertex(baker, vertices[0], u1, v2, tint, direction);
        addVertex(baker, vertices[1], u2, v2, tint, direction);
        addVertex(baker, vertices[2], u2, v1, tint, direction);
        addVertex(baker, vertices[3], u1, v1, tint, direction);
        return quad[0];
    }

    // stolen from direwolf hehe

    public static void renderModelBrightnessColorQuads(PoseStack.Pose matrixEntry, VertexConsumer builder, float red, float green, float blue, float alpha, List<BakedQuad> listQuads, int combinedLightsIn, int combinedOverlayIn) {
        for(BakedQuad bakedquad : listQuads) {
            float f;
            float f1;
            float f2;

            f = red * 1f;
            f1 = green * 1f;
            f2 = blue * 1f;

            builder.putBulkData(matrixEntry, bakedquad, f, f1, f2, alpha, combinedLightsIn, combinedOverlayIn, true);
        }
    }

}
