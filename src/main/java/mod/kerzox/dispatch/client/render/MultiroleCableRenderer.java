package mod.kerzox.dispatch.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.entity.MultirolePipe;
import mod.kerzox.dispatch.common.util.PipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MultiroleCableRenderer implements BlockEntityRenderer<MultirolePipe> {

    public static final ResourceLocation CONNECTED = new ResourceLocation(Dispatch.MODID, "block/generic_connected");
    public static final ResourceLocation CONNECTED_BIG = new ResourceLocation(Dispatch.MODID, "block/bigger_cable_connected");
    public static final ResourceLocation CORE_BIG = new ResourceLocation(Dispatch.MODID, "block/bigger_cable");

    public MultiroleCableRenderer(BlockEntityRendererProvider.Context pContext) {

    }

    @Override
    public void render(MultirolePipe pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        WrappedPose pose = WrappedPose.of(pPoseStack);

        AtomicReference<float[]> colours = new AtomicReference<>();
        colours.set(RenderingUtil.convertColor(0xf14f4f));

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(CONNECTED);
        BakedModel model1 = Minecraft.getInstance().getModelManager().getModel(CONNECTED_BIG);
        BakedModel model2 = Minecraft.getInstance().getModelManager().getModel(CORE_BIG);

        BlockPos pos = pBlockEntity.getBlockPos();
        int blockLight = pBlockEntity.getLevel().getBrightness(LightLayer.BLOCK, pos.above());
        int skyLight = pBlockEntity.getLevel().getBrightness(LightLayer.SKY, pos.above());
        VertexConsumer builder = pBufferSource.getBuffer(RenderType.cutout());

        if (pBlockEntity.getSubtypes().size() <= 1) {
            pose.push();
            for (PipeTypes subtype : pBlockEntity.getSubtypes()) {
                colours.set(RenderingUtil.convertColor(subtype.getTint()));
            }
            renderCore(pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder);
            pBlockEntity.getVisualConnectionMap().forEach((connectedDirection, connected) -> {
                renderPipeConnection(pBlockEntity, pPoseStack, pPackedOverlay, pose, colours, model, pPackedLight, pPackedOverlay, builder, connectedDirection, connected);
            });
            pose.pop();
        } else {
            colours.set(RenderingUtil.convertColor(0xf14f4f));
            pose.push();
            renderCore(pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder, model2);
            pBlockEntity.getVisualConnectionMap().forEach((connectedDirection, connected) -> {
                renderPipeConnection(pBlockEntity, pPoseStack, pPackedOverlay, pose, colours, model1, pPackedLight, pPackedOverlay, builder, connectedDirection, connected);
            });
            pose.pop();
        }

    }

    private void renderCore(MultirolePipe pBlockEntity, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, int blockLight, int skyLight, VertexConsumer builder, BakedModel model2) {
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = getQuads(model2, pBlockEntity, direction, RenderType.cutout());
            RenderingUtil.renderModelBrightnessColorQuads(pose.last(), builder, colours.get()[0], colours.get()[1], colours.get()[2], 1f,
                    quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
        }
    }

    private void renderCore(MultirolePipe pBlockEntity, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, int blockLight, int skyLight, VertexConsumer builder) {
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = getQuads(Minecraft.getInstance().getBlockRenderer().getBlockModel(pBlockEntity.getBlockState()), pBlockEntity, direction, RenderType.cutout());
            RenderingUtil.renderModelBrightnessColorQuads(pose.last(), builder, colours.get()[0], colours.get()[1], colours.get()[2], 1f,
                    quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
        }
    }


    private void renderPipeConnection(MultirolePipe pBlockEntity, PoseStack pPoseStack, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, BakedModel model, int blockLight, int skyLight, VertexConsumer builder, Direction connectedDirection, Boolean connected) {
        if (connected) {
            pose.push();

            if (connectedDirection == Direction.UP) {
                pose.rotateX(90);
            }
            else if (connectedDirection == Direction.DOWN) {
                pose.rotateX(-90);
            }
            else if (connectedDirection == Direction.SOUTH) {
                pose.rotateX(-180);
            }
            else if (connectedDirection == Direction.WEST) {
                pose.rotateY(90);
            }
            else if (connectedDirection == Direction.EAST) {
                pose.rotateY(-90);
            }

            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = getQuads(model, pBlockEntity, direction, RenderType.cutout());
                RenderingUtil.renderModelBrightnessColorQuads(pPoseStack.last(), builder, colours.get()[0], colours.get()[1], colours.get()[2], 1f,
                        quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
            }

            pose.pop();
        }
    }

    private List<BakedQuad> getQuads(BakedModel model, BlockEntity tile, Direction side, RenderType type) {
        return model.getQuads(null, null, RandomSource.create(Mth.getSeed(tile.getBlockPos())), ModelData.EMPTY, type);
    }

}
