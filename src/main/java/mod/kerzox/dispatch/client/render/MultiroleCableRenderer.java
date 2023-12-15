package mod.kerzox.dispatch.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.entity.DynamicTilingEntity;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MultiroleCableRenderer implements BlockEntityRenderer<DynamicTilingEntity> {

    public static final ResourceLocation CONNECTED = new ResourceLocation(Dispatch.MODID, "block/generic_connected");
    public static final ResourceLocation CONNECTED_BIG = new ResourceLocation(Dispatch.MODID, "block/bigger_cable_connected");
    public static final ResourceLocation CORE_BIG = new ResourceLocation(Dispatch.MODID, "block/bigger_cable");
    public static final ResourceLocation CORE = new ResourceLocation(Dispatch.MODID, "block/cable");

    public static final ResourceLocation CORE_ENERGY = new ResourceLocation(Dispatch.MODID, "block/energy_cable");

    public MultiroleCableRenderer(BlockEntityRendererProvider.Context pContext) {

    }

    @Override
    public void render(DynamicTilingEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
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

        pBlockEntity.getLevel().getCapability(LevelNetworkHandler.NETWORK).ifPresent(cap -> {
            if (cap instanceof LevelNetworkHandler networkHandler) {

                List<AbstractSubNetwork> list = networkHandler.getSubnetsFrom(LevelNode.of(pos));

                if (list.isEmpty()) {
                    PacketHandler.sendToServer(new LevelNetworkPacket(new CompoundTag()));
                    return;
                }

                if (list.size() == 1) {
                    pose.push();
                    colours.set(RenderingUtil.convertColor(list.get(0).getRenderingColour()));
                    renderCore(list.get(0).getCapability(), pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder);
                    pBlockEntity.getConnectedSides().forEach((connectedDirection, face) -> {
                        renderPipeConnection(pBlockEntity, pPoseStack, pPackedOverlay, pose, colours, model, pPackedLight, pPackedOverlay, builder, connectedDirection, face == DynamicTilingEntity.Face.CONNECTION);
                    });
                    pose.pop();
                } else {
                    colours.set(RenderingUtil.convertColor(0xFF323232));
                    pose.push();
                    renderCore(pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder, model2);
                    pBlockEntity.getConnectedSides().forEach((connectedDirection, face) -> {
                        renderPipeConnection(pBlockEntity, pPoseStack, pPackedOverlay, pose, colours, model1, pPackedLight, pPackedOverlay, builder, connectedDirection, face == DynamicTilingEntity.Face.CONNECTION);
                    });
                    pose.pop();
                }

            }
        });

//        if (pBlockEntity.getSubtypes().size() <= 1) {
//            pose.push();
//            for (PipeTypes subtype : pBlockEntity.getSubtypes()) {
//                colours.set(RenderingUtil.convertColor(subtype.getTint()));
//            }
//            renderCore(pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder);
//            pBlockEntity.getVisualConnectionMap().forEach((connectedDirection, connected) -> {
//                renderPipeConnection(pBlockEntity, pPoseStack, pPackedOverlay, pose, colours, model, pPackedLight, pPackedOverlay, builder, connectedDirection, connected);
//            });
//            pose.pop();
//        } else {
//            colours.set(RenderingUtil.convertColor(0xf14f4f));
//            pose.push();
//            renderCore(pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder, model2);
//            pBlockEntity.getVisualConnectionMap().forEach((connectedDirection, connected) -> {
//                renderPipeConnection(pBlockEntity, pPoseStack, pPackedOverlay, pose, colours, model1, pPackedLight, pPackedOverlay, builder, connectedDirection, connected);
//            });
//            pose.pop();
//        }

    }

    private void renderCore(DynamicTilingEntity pBlockEntity, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, int blockLight, int skyLight, VertexConsumer builder, BakedModel model2) {
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = getQuads(model2, pBlockEntity, direction, RenderType.cutout());
            RenderingUtil.renderQuads(pose.last(), builder, colours.get()[0], colours.get()[1], colours.get()[2], 1f,
                    quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
        }
    }

    private void renderCore(Capability<?> capability, DynamicTilingEntity pBlockEntity, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, int blockLight, int skyLight, VertexConsumer builder) {
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = getQuads(capability == ForgeCapabilities.ENERGY ? Minecraft.getInstance().getModelManager().getModel(CORE_ENERGY) : Minecraft.getInstance().getModelManager().getModel(CORE), pBlockEntity, direction, RenderType.cutout());
            RenderingUtil.renderQuads(pose.last(), builder, colours.get()[0], colours.get()[1], colours.get()[2], 1f,
                    quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
        }
    }


    private void renderPipeConnection(DynamicTilingEntity pBlockEntity, PoseStack pPoseStack, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, BakedModel model, int blockLight, int skyLight, VertexConsumer builder, Direction connectedDirection, Boolean connected) {
        if (connected) {
            pose.push();

            if (connectedDirection == Direction.UP) {
                pose.rotateX(90);
            } else if (connectedDirection == Direction.DOWN) {
                pose.rotateX(-90);
            } else if (connectedDirection == Direction.SOUTH) {
                pose.rotateX(-180);
            } else if (connectedDirection == Direction.WEST) {
                pose.rotateY(90);
            } else if (connectedDirection == Direction.EAST) {
                pose.rotateY(-90);
            }

            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = getQuads(model, pBlockEntity, direction, RenderType.cutout());
                RenderingUtil.renderQuads(pPoseStack.last(), builder, colours.get()[0], colours.get()[1], colours.get()[2], 1f,
                        quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
            }

            pose.pop();
        }
    }

    private List<BakedQuad> getQuads(BakedModel model, BlockEntity tile, Direction side, RenderType type) {
        return model.getQuads(null, null, RandomSource.create(Mth.getSeed(tile.getBlockPos())), ModelData.EMPTY, type);
    }

}
