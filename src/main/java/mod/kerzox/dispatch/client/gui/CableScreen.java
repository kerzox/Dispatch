package mod.kerzox.dispatch.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import mod.kerzox.dispatch.Config;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.component.CapabilityTabButton;
import mod.kerzox.dispatch.client.component.IOHandlerButton;
import mod.kerzox.dispatch.client.component.ToggleButtonComponent;
import mod.kerzox.dispatch.client.menu.CableMenu;
import mod.kerzox.dispatch.client.render.RenderingUtil;
import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.common.util.DispatchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CableScreen extends Screen implements ICustomScreen {

    private Level level = Minecraft.getInstance().level;

    protected int left;
    protected int top;
    protected int xSize;
    protected int ySize;
    protected int imageWidth = 77;
    protected int imageHeight = 79;

    private ResourceLocation GUI;
    private int backgroundColour;

    private Map<Capability<?>, List<Pair<Direction, IOHandlerButton>>> ioButtons = new HashMap<>();
    private List<CapabilityTabButton> capabilityButtons = new ArrayList<>();
    private LevelNode node;

    private AbstractSubNetwork currentSubNetworkActive;

    protected CableScreen(LevelNode node) {
        super(Component.literal("Cable Config"));
        this.GUI = new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png");
        this.backgroundColour = 0;
        this.xSize = 77;
        this.ySize = 79;
        this.node = node;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void draw(BlockPos node) {
        Minecraft.getInstance().setScreen(new CableScreen(LevelNode.of(node)));
    }

    @Override
    protected void init() {
        this.left = (this.width - this.imageWidth) / 2;
        this.top = (this.height - this.imageHeight) / 2;


    }

    private void displayTab(CapabilityTabButton capabilityTabButton, int button, AbstractSubNetwork network) {

        if (capabilityTabButton.getState()) currentSubNetworkActive = network;
        else currentSubNetworkActive = null;

        for (ToggleButtonComponent component : capabilityButtons) {
            if (component != capabilityTabButton) component.setState(false);
        }

        ioButtons.forEach((capability, ioHandlerButtons) -> {
            for (Pair<Direction, IOHandlerButton> ioHandlerButton : ioHandlerButtons) {
                ioHandlerButton.getSecond().setVisible(capabilityTabButton.getState() && capability == network.getCapability());
            }
        });
    }

    private void handleButtonClick(IOHandlerButton button, Direction direction, int buttonClick) {

        if (LevelNetworkHandler.getHandler(level).getSubnetFromPos(currentSubNetworkActive.getCapability(), LevelNode.of(node.getPos().relative(direction))).isPresent()) {

            if (button.getCurrentSetting() != LevelNode.IOTypes.NONE) {
                button.setCurrentSetting(LevelNode.IOTypes.NONE);
            } else {
                button.setCurrentSetting(LevelNode.IOTypes.DEFAULT);
            }

        } else {
            if (buttonClick == 0) {
                int index = (button.getCurrentSetting().ordinal() + 1) % LevelNode.IOTypes.values().length;
                button.setCurrentSetting(LevelNode.IOTypes.values()[index]);
            } else {
                int index = (button.getCurrentSetting().ordinal() - 1 + LevelNode.IOTypes.values().length) % LevelNode.IOTypes.values().length;
                button.setCurrentSetting(LevelNode.IOTypes.values()[index]);
            }
        }

        LevelNetworkHandler.getHandler(level).getSubnetFromPos(currentSubNetworkActive.getCapability(), node).ifPresent(subNetwork -> {

            // get the node from the level
            LevelNode node1 = new LevelNode(subNetwork.getNodeByPosition(node.getPos()).serialize());
            node1.getDirectionalIO().put(direction, button.getCurrentSetting());

            if (currentSubNetworkActive != null) {
                PacketHandler.sendToServer(LevelNetworkPacket.of(currentSubNetworkActive.getCapability(), node1));
            }
        });


    }

    @Override
    public void tick() {

    }

    @Override
    public boolean mouseClicked(double p_94695_, double p_94696_, int p_94697_) {
        return super.mouseClicked(p_94695_, p_94696_, p_94697_);
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float partialTick) {
        super.renderBackground(graphics);
        if (GUI != null) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            graphics.blit(GUI, i, j, 0, 0, 77, 79, 256, 256);
        }
        super.render(graphics, pMouseX, pMouseY, partialTick);

        if (Config.DEBUG_MODE) {

            LevelNetworkHandler.getHandler(level).getSubnetFromPos(currentSubNetworkActive.getCapability(), LevelNode.of(node.getPos())).ifPresent(subNetwork -> {
                int y = 25;
                RenderingUtil.drawText(
                        "Current Network Subnets: " + LevelNetworkHandler.getHandler(level).getNetworkByCapability(subNetwork.getCapability()).getSubNetworks().size(),
                        graphics,
                        getGuiLeft() - 150,
                        y,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Current Subnet Capability: " + subNetwork.getCapability().getName(),
                        graphics,
                        getGuiLeft() - 150,
                        y += 10,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Cable Length: " + subNetwork.getNodes().size(),
                        graphics,
                        getGuiLeft() - 150,
                        y += 10,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Subnet Tier: " + subNetwork.getTier().getSerializedName().substring(0, 1).toUpperCase() + subNetwork.getTier().getSerializedName().substring(1),
                        graphics,
                        getGuiLeft() - 150,
                        y += 10,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Subnet Data: " + subNetwork.write(),
                        graphics,
                        getGuiLeft() - 150,
                        y += 10,
                        0xff6565
                );
            });
        }
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_96552_, p_96553_);
        if (super.keyPressed(p_96552_, p_96553_, p_96554_)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public int getGuiLeft() {
        return left;
    }

    @Override
    public int getGuiTop() {
        return top;
    }

    @Override
    public CableMenu getMenu() {
        return null;
    }
}
