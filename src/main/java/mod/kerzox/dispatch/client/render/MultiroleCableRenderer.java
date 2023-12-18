package mod.kerzox.dispatch.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.capability.energy.EnergySubNetwork;
import mod.kerzox.dispatch.common.entity.DispatchNetworkEntity;
import mod.kerzox.dispatch.common.item.DispatchItem;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.common.util.DispatchUtil;
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

public class MultiroleCableRenderer implements BlockEntityRenderer<DispatchNetworkEntity> {

    public static final ResourceLocation CONNECTED = new ResourceLocation(Dispatch.MODID, "block/generic_connected");
    public static final ResourceLocation CONNECTED_BIG = new ResourceLocation(Dispatch.MODID, "block/bigger_cable_connected");
    public static final ResourceLocation CORE_BIG = new ResourceLocation(Dispatch.MODID, "block/bigger_cable");
    public static final ResourceLocation CORE = new ResourceLocation(Dispatch.MODID, "block/cable");

    public static final ResourceLocation CORE_ENERGY = new ResourceLocation(Dispatch.MODID, "block/energy_cable");
    public static final ResourceLocation ENERGY_CONNECTED = new ResourceLocation(Dispatch.MODID, "block/energy_cable_connected");
    public static final ResourceLocation ENERGY_BEAM = new ResourceLocation(Dispatch.MODID, "block/energy_beam");

    public static final ResourceLocation CORE_FLUID = new ResourceLocation(Dispatch.MODID, "block/fluid_cable");
    public static final ResourceLocation CORE_ITEM = new ResourceLocation(Dispatch.MODID, "block/item_cable");


    public static final ResourceLocation BORDER = new ResourceLocation(Dispatch.MODID, "block/border");

    public MultiroleCableRenderer(BlockEntityRendererProvider.Context pContext) {

    }

    @Override
    public void render(DispatchNetworkEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
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
                    renderCore(list.get(0).getCapability(), list.get(0).getTier(), pBufferSource, pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder);
                    pBlockEntity.getConnectedSides().forEach((connectedDirection, face) -> {
                        renderPipeConnection(pBlockEntity, list.get(0), pose.asStack(), pPackedOverlay, pose, colours, getConnectionModel(list.get(0)), pPackedLight, pPackedOverlay, builder, connectedDirection, face == DispatchNetworkEntity.Face.CONNECTION);
                    });
//                    pBlockEntity.getConnectedSides().forEach((connectedDirection, face) -> {
//
//                    });

                    pose.pop();
                } else {
                    colours.set(RenderingUtil.convertColor(0xFF323232));
                    pose.push();
                    renderCore(pBlockEntity, pPackedOverlay, pose, colours, pPackedLight, pPackedOverlay, builder, model2);
                    pBlockEntity.getConnectedSides().forEach((connectedDirection, face) -> {
                        renderPipeConnection(pBlockEntity, null, pPoseStack, pPackedOverlay, pose, colours, model1, pPackedLight, pPackedOverlay, builder, connectedDirection, face == DispatchNetworkEntity.Face.CONNECTION);
                    });
                    pose.pop();
                }

            }
        });

    }

    private BakedModel getConnectionModel(AbstractSubNetwork subNetwork) {
        if(subNetwork.getCapability() == ForgeCapabilities.ENERGY) {
            return Minecraft.getInstance().getModelManager().getModel(ENERGY_CONNECTED);
        }
        return Minecraft.getInstance().getModelManager().getModel(CONNECTED);
    }

    private void renderCore(DispatchNetworkEntity pBlockEntity, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, int blockLight, int skyLight, VertexConsumer builder, BakedModel model2) {
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = getQuads(model2, pBlockEntity, direction, RenderType.cutout());
            RenderingUtil.renderQuads(pose.last(), builder, 1, 1, 1, 1f,
                    quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
        }
    }

    private void renderCore(Capability<?> capability,
                            DispatchItem.Tiers tier,
                            MultiBufferSource pBufferSource, DispatchNetworkEntity pBlockEntity,
                            int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, int blockLight, int skyLight, VertexConsumer builder) {

        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = getQuads(getCoreFromType(capability), pBlockEntity, direction, RenderType.cutout());
            RenderingUtil.renderQuads(pose.last(), builder, 1, 1, 1, 1f,
                    quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);

//            float[] bcolour = RenderingUtil.convertColor(DispatchUtil.getTintFromTier(tier));
//
//            List<BakedQuad> quads2 = getQuads(Minecraft.getInstance().getModelManager().getModel(BORDER), pBlockEntity, direction, RenderType.cutout());
//            RenderingUtil.renderQuads(pose.last(), builder, bcolour[0], bcolour[1], bcolour[2], 1f,
//                    quads2, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
        }

    }

    private BakedModel getCoreFromType(Capability<?> capability) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return Minecraft.getInstance().getModelManager().getModel(CORE_ITEM);
        if (capability == ForgeCapabilities.ENERGY) return Minecraft.getInstance().getModelManager().getModel(CORE_ENERGY);
        if (capability == ForgeCapabilities.FLUID_HANDLER) return Minecraft.getInstance().getModelManager().getModel(CORE_FLUID);
        else return Minecraft.getInstance().getModelManager().getMissingModel();
    }


    private void renderPipeConnection(DispatchNetworkEntity pBlockEntity, AbstractSubNetwork subNetwork, PoseStack pPoseStack, int pPackedOverlay, WrappedPose pose, AtomicReference<float[]> colours, BakedModel model, int blockLight, int skyLight, VertexConsumer builder, Direction connectedDirection, Boolean connected) {
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

            float[] c = subNetwork != null ? RenderingUtil.convertColor(DispatchUtil.getTintFromTier(subNetwork.getTier())) : RenderingUtil.convertColor(0xffffff);

            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = getQuads(model, pBlockEntity, direction, RenderType.cutout());
                RenderingUtil.renderQuads(pPoseStack.last(), builder,  c[0], c[1], c[2], 1f,
                        quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
            }

            float[] energy = RenderingUtil.convertColor(RenderingUtil.custom("0x49ff7d", 45));

            if (subNetwork instanceof EnergySubNetwork energySubNetwork) {
                pose.push();
                if (energySubNetwork.isEnergyFlowing()) {
                    for (Direction direction : Direction.values()) {
                        List<BakedQuad> quads = getQuads(Minecraft.getInstance().getModelManager().getModel(ENERGY_BEAM), pBlockEntity, direction, RenderType.cutout());
                        RenderingUtil.renderQuads(pPoseStack.last(), builder,  energy[0], energy[1], energy[2], 1f,
                                quads, LightTexture.pack(blockLight, skyLight), pPackedOverlay);
                    }
//                                for (Direction direction : Direction.values()) {
//                                    if (connectedDirection.getAxis() == Direction.Axis.Z) {
//                                        RenderingUtil.drawQuad(pose.asStack(), pBufferSource.getBuffer(DispatchRenderTypes.SOLID_COLOUR), direction, pPackedLight, 7.5f / 16f, 7.5f / 16f, .5f, 8.5f / 16f, 8.5f / 16f, 1, RenderingUtil.custom("0x49ff7d", 45));
//                                    }
//                                }
                }
                pose.pop();
            }

            pose.pop();
        }
    }

    private List<BakedQuad> getQuads(BakedModel model, BlockEntity tile, Direction side, RenderType type) {
        return model.getQuads(null, null, RandomSource.create(Mth.getSeed(tile.getBlockPos())), ModelData.EMPTY, type);
    }

}
