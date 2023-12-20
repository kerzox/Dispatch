package mod.kerzox.dispatch.client.gui;

import com.mojang.datafixers.util.Pair;
import mod.kerzox.dispatch.Config;
import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.component.*;
import mod.kerzox.dispatch.client.menu.CableMenu;
import mod.kerzox.dispatch.client.render.RenderingUtil;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CableContainerScreen extends AbstractContainerScreen<CableMenu> implements ICustomScreen {

    private Level level = Minecraft.getInstance().level;

    public static final int DEFAULT_X_POS = 0;
    public static final int DEFAULT_Y_POS = 0;
    public static final int DEFAULT_WIDTH = 176;
    public static final int DEFAULT_HEIGHT = 166;

    private ResourceLocation texture;
    protected int guiX;
    protected int guiY;

    private Map<Capability<?>, List<Pair<Direction, IOHandlerButton>>> ioButtons = new HashMap<>();
    private List<CapabilityTabButton> capabilityButtons = new ArrayList<>();
    private LevelNode node;
    private AbstractSubNetwork currentSubNetworkActive;
    private ConfigurateOperation operation = new ConfigurateOperation(this, 0 ,0);
    private List<ConfigurateOperation> operations= new ArrayList<>();

    private ButtonComponent openNewOperation = new ButtonComponent(this,
            new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
            84, 64, 75, 12, 0, 148, 0, 160, Component.literal("New Operation"), this::openOperation){
        @Override
        public void drawComponent(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.drawComponent(graphics, pMouseX, pMouseY, pPartialTick);
            int i = 16777215;
            this.renderString(graphics, Minecraft.getInstance().font, i | Mth.ceil(this.alpha * 255.0F) << 24);
        }
    };

    public CableContainerScreen(CableMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        guiX = DEFAULT_X_POS;
        guiY = DEFAULT_Y_POS;
        this.width = 176;
        this.height = 182;
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.texture = new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png");
        this.node = getMenu().getNode();
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
    protected void init() {
        super.init();

        Direction[] dir = getDirectionFromFacing(Minecraft.getInstance().player.getDirection());

        int ioX = 20;
        int ioY = 8;

        int tabX = 5;
        int tabY = 61;
        int spacer = 0;

        for (AbstractSubNetwork network : LevelNetworkHandler.getHandler(level).getSubnetsFrom(node)) {

            capabilityButtons.add(
                    new CapabilityTabButton(this, new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
                            tabX + spacer, tabY, 15, 15, node, network, (button, i) -> displayTab((CapabilityTabButton) button, i, network))
            );

            spacer += 17;

            ioButtons.put(network.getCapability(), new ArrayList<>() {
                {
                    add(new Pair<>(Direction.UP, new IOHandlerButton(CableContainerScreen.this, network, network.getNodeByPosition(node.getPos()), new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), ioX + 12, ioY, 12, 12,
                            Component.literal("Up"), Direction.UP, (button, i) -> handleButtonClick((IOHandlerButton) button, Direction.UP, i))));
                    add(new Pair<>(dir[3], new IOHandlerButton(CableContainerScreen.this, network, network.getNodeByPosition(node.getPos()), new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), ioX, ioY + 12, 12, 12,
                            Component.literal("Left Side"), dir[3], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[3], i))));
                    add(new Pair<>(dir[2], new IOHandlerButton(CableContainerScreen.this, network, network.getNodeByPosition(node.getPos()), new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), ioX, ioY + (12 * 2), 12, 12,
                            Component.literal("Front"), dir[2], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[2], i))));
                    add(new Pair<>(dir[0], new IOHandlerButton(CableContainerScreen.this, network, network.getNodeByPosition(node.getPos()), new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), ioX + 12, ioY + 12, 12, 12,
                            Component.literal("Right Side"), dir[0], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[0], i))));
                    add(new Pair<>(Direction.DOWN, new IOHandlerButton(CableContainerScreen.this, network, network.getNodeByPosition(node.getPos()), new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), ioX + 12, ioY + (12 * 2), 12, 12,
                            Component.literal("Bottom"), Direction.DOWN, (button, i) -> handleButtonClick((IOHandlerButton) button, Direction.DOWN, i))));
                    add(new Pair<>(dir[1], new IOHandlerButton(CableContainerScreen.this, network, network.getNodeByPosition(node.getPos()), new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), ioX + (12 * 2), ioY + 12, 12, 12,
                            Component.literal("Back"), dir[1], (button, i) -> handleButtonClick((IOHandlerButton) button, dir[1], i))));
                }
            });

            for (Pair<Direction, IOHandlerButton> buttonPair : ioButtons.get(network.getCapability())) {
                addRenderableWidget(buttonPair.getSecond());
                buttonPair.getSecond().setVisible(false);
                LevelNetworkHandler.getHandler(level).getSubnetFromPos(network.getCapability(), node).ifPresent(subNetwork -> {
                    buttonPair.getSecond().setCurrentSetting(subNetwork.getNodeByPosition(node.getPos()).getDirectionalIO().get(buttonPair.getFirst()));
                });
            }

        }

        for (ToggleButtonComponent capabilityButton : capabilityButtons) {
            addRenderableWidget(capabilityButton);
        }

        capabilityButtons.get(0).setState(true);
        currentSubNetworkActive = capabilityButtons.get(0).getSubNetwork();

        for (Pair<Direction, IOHandlerButton> buttonPair : ioButtons.get(capabilityButtons.get(0).getSubNetwork().getCapability())) {
            buttonPair.getSecond().setVisible(true);
        }

        FilterList list = new FilterList(this, 84, 5);

        addRenderableWidget(list);
        addRenderableWidget(list.getScrollBarComponent());
        operation.setVisible(false);
        operation.setVisible(false);
        addRenderableWidget(operation);
        addRenderableWidget(operation.getButton());
        addRenderableWidget(openNewOperation);

        for (GuiEventListener child : children()) {
            if (child instanceof NewWidgetComponent component) component.onInit();
        }

    }

    private void openOperation(ButtonComponent buttonComponent, int i) {
        if (!operation.visible) {
            openNewOperation.setVisible(false);
            operation.recreatePage();

            for (CapabilityTabButton button : capabilityButtons) {
                button.setVisible(false);
            }
            ioButtons.forEach((capability, pairs) ->
                    pairs.forEach(directionIOHandlerButtonPair -> directionIOHandlerButtonPair.getSecond().setVisible(false)));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getChildAt(mouseX, mouseY).map(p -> p.mouseDragged(mouseX, mouseY, button, dragX, dragY)).orElse(false)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        p_281635_.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, 89, 4210752, false);
    }

    @Override
    public void render(GuiGraphics graphics, int p_283661_, int p_281248_, float p_281886_) {
        super.renderBackground(graphics);
        super.render(graphics, p_283661_, p_281248_, p_281886_);
        this.renderTooltip(graphics, p_283661_, p_281248_);

        if (Config.DEBUG_MODE && currentSubNetworkActive != null) {

            LevelNetworkHandler.getHandler(level).getSubnetFromPos(currentSubNetworkActive.getCapability(), LevelNode.of(node.getPos())).ifPresent(subNetwork -> {
                int y = 25;
                int x = getGuiLeft() - 180;
                RenderingUtil.drawText(
                        "Current Network Subnets: " + LevelNetworkHandler.getHandler(level).getNetworkByCapability(subNetwork.getCapability()).getSubNetworks().size(),
                        graphics,
                        x,
                        y,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Current Subnet Capability: " + subNetwork.getCapability().getName(),
                        graphics,
                        x,
                        y += 10,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Cable Length: " + subNetwork.getNodes().size(),
                        graphics,
                        x,
                        y += 10,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Subnet Tier: " + subNetwork.getTier().getSerializedName().substring(0, 1).toUpperCase() + subNetwork.getTier().getSerializedName().substring(1),
                        graphics,
                        x,
                        y += 10,
                        0xff6565
                );

                RenderingUtil.drawText(
                        "Subnet Data: " + subNetwork.write(),
                        graphics,
                        x,
                        y += 10,
                        0xff6565
                );
            });
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2 + guiX;
        int j = (this.height - this.imageHeight) / 2 + guiY;
        graphics.blit(texture, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

    }



}
