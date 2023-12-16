package mod.kerzox.dispatch.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.component.CapabilityTabButton;
import mod.kerzox.dispatch.client.component.IOHandlerButton;
import mod.kerzox.dispatch.client.component.ToggleButtonComponent;
import mod.kerzox.dispatch.common.capability.AbstractNetwork;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
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

    private Direction[] getDirectionFromFacing(Direction facing) {
        Direction[] dir = new Direction[Direction.values().length];
        if (facing == Direction.SOUTH) {
            dir[0] = Direction.NORTH;
            dir[1] = Direction.WEST;
            dir[2] = Direction.SOUTH;
            dir[3] = Direction.EAST;
        }
        if (facing == Direction.NORTH) {
            dir[0] = Direction.SOUTH;
            dir[1] = Direction.EAST;
            dir[2] = Direction.NORTH;
            dir[3] = Direction.WEST;
        }
        if (facing == Direction.EAST) {
            dir[0] = Direction.WEST;
            dir[1] = Direction.SOUTH;
            dir[2] = Direction.EAST;
            dir[3] = Direction.NORTH;
        }
        if (facing == Direction.WEST) {
            dir[0] = Direction.EAST;
            dir[1] = Direction.NORTH;
            dir[2] = Direction.WEST;
            dir[3] = Direction.SOUTH;
        }
        dir[4] = Direction.UP;
        dir[5] = Direction.DOWN;
        return dir;
    }


    public static void draw(BlockPos node) {
        Minecraft.getInstance().setScreen(new CableScreen(LevelNode.of(node)));
    }

    @Override
    protected void init() {
        this.left = (this.width - this.imageWidth) / 2;
        this.top = (this.height - this.imageHeight) / 2;

        Direction[] dir = getDirectionFromFacing(Minecraft.getInstance().player.getDirection());

        int ioX = 20;
        int ioY = 8;

        int tabX = 5;
        int tabY = 59;
        int spacer = 0;

        for (AbstractSubNetwork network : LevelNetworkHandler.getHandler(level).getSubnetsFrom(node)) {

            capabilityButtons.add(
                    new CapabilityTabButton(this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"),
                            tabX + spacer, tabY, 15, 15, node, network, (button, i) -> displayTab((CapabilityTabButton)button, i, network))
            );

            spacer += 17;

            ioButtons.put(network.getCapability(), new ArrayList<>(){
                {
                    add(new Pair<>(Direction.UP, new IOHandlerButton(CableScreen.this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"), ioX + 12, ioY, 12, 12,
                            Component.literal("Up"), Direction.UP, (button, i) -> handleButtonClick((IOHandlerButton) button, Direction.UP, i))));
                    add(new Pair<>(dir[3],new IOHandlerButton(CableScreen.this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"), ioX, ioY + 12, 12, 12,
                            Component.literal("Left Side"), dir[3], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[3], i))));
                    add(new Pair<>(dir[2],new IOHandlerButton(CableScreen.this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"), ioX, ioY + (12 * 2), 12, 12,
                            Component.literal("Front"), dir[2], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[2], i))));
                    add(new Pair<>(dir[0],new IOHandlerButton(CableScreen.this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"), ioX + 12, ioY + 12, 12, 12,
                            Component.literal("Right Side"), dir[0], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[0], i))));
                    add(new Pair<>(Direction.DOWN,new IOHandlerButton(CableScreen.this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"), ioX + 12, ioY + (12 * 2), 12, 12,
                            Component.literal("Bottom"), Direction.DOWN, (button, i) -> handleButtonClick((IOHandlerButton) button, Direction.DOWN, i))));
                    add(new Pair<>(dir[1],new IOHandlerButton(CableScreen.this, new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png"), ioX + (12 * 2), ioY + 12, 12, 12,
                            Component.literal("Back"), dir[1], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[1], i))));
                }
            });
        }

        for (ToggleButtonComponent capabilityButton : capabilityButtons) {
            addRenderableWidget(capabilityButton);
        }

        capabilityButtons.get(0).setState(true);
        currentSubNetworkActive = capabilityButtons.get(0).getSubNetwork();

        for (List<Pair<Direction, IOHandlerButton>> buttons : ioButtons.values()) {
            buttons.forEach(button-> {
                addRenderableWidget(button.getSecond());
                button.getSecond().setVisible(false);
                LevelNetworkHandler.getHandler(level).getSubnetFromPos(currentSubNetworkActive.getCapability(), node).ifPresent(subNetwork -> {
                    button.getSecond().setCurrentSetting(subNetwork.getNodeByPosition(node.getPos()).getDirectionalIO().get(button.getFirst()));
                });
            });
        }

        for (Pair<Direction, IOHandlerButton> buttonPair : ioButtons.get(capabilityButtons.get(0).getSubNetwork().getCapability())) {
            buttonPair.getSecond().setVisible(true);
        }

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
        if (buttonClick == 0) {
            int index = (button.getCurrentSetting().ordinal() + 1) % LevelNode.IOTypes.values().length;
            button.setCurrentSetting(LevelNode.IOTypes.values()[index]);
        } else {
            int index = (button.getCurrentSetting().ordinal() - 1 + LevelNode.IOTypes.values().length) % LevelNode.IOTypes.values().length;
            button.setCurrentSetting(LevelNode.IOTypes.values()[index]);
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
}
