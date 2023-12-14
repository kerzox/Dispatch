package mod.kerzox.dispatch.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.kerzox.dispatch.client.render.DispatchRenderTypes;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.capability.NetworkHandler;
import mod.kerzox.dispatch.common.capability.energy.EnergySubNetwork;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.lighting.ForgeModelBlockRenderer;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES;

public class RenderLevelNetworks {

    public HashSet<BlockPos> all_cables = new HashSet<>();

    public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        double deltaZ = z2 - z1;

        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        return distance;
    }

    private List<BakedQuad> getQuads(BakedModel model, BlockPos pos, Direction side, RenderType type) {
        return model.getQuads(null, null, RandomSource.create(Mth.getSeed(pos)), ModelData.EMPTY, type);
    }

    @SubscribeEvent
    public void onLevelRender(RenderLevelStageEvent event) {
        if (event.getStage() == AFTER_PARTICLES) {
            Player player = Minecraft.getInstance().player;
            PoseStack poseStack = event.getPoseStack();
            int renderTick = event.getRenderTick();
            ClientLevel level = Minecraft.getInstance().level;
            float partialTicks = event.getPartialTick();
            ItemStack item = player.getMainHandItem();
            Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 projectedView = renderInfo.getPosition();
            HitResult ray = event.getCamera().getEntity().pick(20.0D, 0.0F, false);

            if (player != null) {

                int radius = 25;

                event.getCamera().getBlockPosition();

                BlockPos position = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());

                PacketHandler.sendToServer(new LevelNetworkPacket(new CompoundTag()));

                level.getCapability(NetworkHandler.NETWORK).ifPresent(capability -> {
                    if (capability instanceof NetworkHandler handler) {
                        handler.getNetworkMap().forEach((type, network) -> {
                            for (AbstractSubNetwork subNetwork : network.getSubNetworks()) {
                                for (LevelNode node : subNetwork.getNodes()) {
                                    BlockPos cablePos = node.getPos();
                                    if (level.getBlockEntity(cablePos) != null) continue;
                                    if (calculateDistance(position.getX(), position.getY(), position.getZ(), cablePos.getX(), cablePos.getY(), cablePos.getZ()) < radius) {
                                        float red = subNetwork instanceof EnergySubNetwork ? 0 : 1;
                                        float green = subNetwork instanceof EnergySubNetwork ? 1 : 0;
                                        float blue = 0;
                                        ForgeModelBlockRenderer renderer = (ForgeModelBlockRenderer) Minecraft.getInstance().getBlockRenderer().getModelRenderer();
                                        Direction facing = player.getDirection();
                                        BlockPos relative = node.getPos().relative(player.getDirection()).above();
                                        Font fontRenderer = Minecraft.getInstance().font;
                                        Quaternionf cameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
                                        poseStack.pushPose();
                                        RenderSystem.enableDepthTest();
                                        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
                                        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                                        LevelRenderer.renderLineBox(poseStack,
                                                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(DispatchRenderTypes.NO_DEPTH_LINES),
                                                node.getPos().getX(),
                                                node.getPos().getY(),
                                                node.getPos().getZ(),
                                                node.getPos().getX() + 1,
                                                node.getPos().getY() + 1,
                                                node.getPos().getZ() + 1, red, green, blue, 1.0F);


                                        RenderSystem.disableDepthTest();
                                        buffer.endBatch(DispatchRenderTypes.NO_DEPTH_LINES);
                                        poseStack.popPose();

                                    }
                                }
                            }

                        });
                    }
                });
            }
        }
    }

}