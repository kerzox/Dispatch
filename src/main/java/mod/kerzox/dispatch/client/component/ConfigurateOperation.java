package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.gui.CableContainerScreen;
import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.client.render.RenderingUtil;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.capability.item.ItemNodeOperation;
import mod.kerzox.dispatch.common.capability.item.ItemSubNetwork;
import mod.kerzox.dispatch.common.network.LevelNetworkPacket;
import mod.kerzox.dispatch.common.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigurateOperation extends TexturedWidgetComponent {

    private AbstractSubNetwork subNetwork;

    private ItemNodeOperation itemNodeOperation = new ItemNodeOperation(new ArrayList<>(), false, false, 0, null);

    private List<Component> component = new ArrayList<>() {{
        add(Component.literal("Cable Operations"));
        add(Component.literal(""));
    }};

    private FakeItemButton button = new FakeItemButton(screen, 6, 6, 18, 18, Component.literal("Chosen item")) {
        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            ItemStack carrying = screen.getMenu().getCarried();
            if (carrying.is(stack.getItem())) {
                if (button == 0 && stack.getCount() < 64) stack.grow(1);
                else if (button == 2) stack = stack.copyWithCount(64);
                else if (stack.getCount() > 1) stack.shrink(1);
                else setStack(ItemStack.EMPTY);
            } else {
                if (button == 1 && stack.getCount() > 1 && !stack.isEmpty()) {
                    stack.shrink(1);
                } else
                    setStack(screen.getMenu().getCarried().copyWithCount(button == 2 ? 64 : 1));
            }
            itemNodeOperation = ItemNodeOperation.from(stack, itemNodeOperation.isBlackList(), itemNodeOperation.shouldMatchNBT(), itemNodeOperation.getPriority(), itemNodeOperation.getDirection());
        }
    };

    private ButtonComponent directionButton = new ButtonComponent(screen, new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"), 9, 29, 12, 12, 36, 112, 36, 112, Component.literal("Button"),
            this::onDirectionalButton) {

        @Override
        protected List<Component> getComponents() {
            if (screen instanceof CableContainerScreen cableContainerScreen) {
                return List.of(
                        Component.literal(buttonDirection.getSerializedName().substring(0, 1).toUpperCase() + buttonDirection.getSerializedName().substring(1)).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal(cableContainerScreen.getNodeProper().getDirectionalIO().get(buttonDirection).getSerializedName().substring(0, 1).toUpperCase() + cableContainerScreen.getNodeProper().getDirectionalIO().get(buttonDirection).getSerializedName().substring(1) + " Mode")
                );
            }
            return super.getComponents();
        }

        @Override
        public void drawComponent(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            this.draw(graphics);
        }
    };

    private ButtonComponent saveButton = new ButtonComponent(screen, new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
            128, 65, 42, 12, 0, 172, 0, 0, Component.literal("Save"),
            this::saveOperation) {

        @Override
        protected List<Component> getComponents() {
            if (screen instanceof CableContainerScreen cableContainerScreen) {

            }
            return super.getComponents();
        }

        @Override
        public void drawComponent(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            this.draw(graphics);
            int i = 16777215;
            this.renderString(graphics, Minecraft.getInstance().font, i | Mth.ceil(this.alpha * 255.0F) << 24);
        }
    };

    private TextBoxComponent textBoxComponent
            = new TextBoxComponent(screen, 28, 44, 125, 18, 19, false, Component.literal("Input tag/resource location"),
            this::checkForResourceLocation)
            .addPadding(-2, 2);

    private void checkForResourceLocation(String text) {
        try {
            ResourceLocation location = new ResourceLocation(text);
            TagKey<Item> tag = ItemTags.create(location);
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                ItemStack stack = new ItemStack(holder.get());
                if (!stack.isEmpty()) {
                    component.set(1, Component.translatable(stack.getDescriptionId()).withStyle(ChatFormatting.GREEN));
                    if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER) {
                        itemNodeOperation = ItemNodeOperation.from(tag, itemNodeOperation.isBlackList(), itemNodeOperation.shouldMatchNBT(), itemNodeOperation.getPriority(), itemNodeOperation.getDirection());
                    }
                    textBoxComponent.setText("");
                    textBoxComponent.setFocused(false);
                    break;
                }
            }
            if (itemNodeOperation.getItemStacks().isEmpty())
                component.set(1, Component.literal("Resource location is invalid").withStyle(ChatFormatting.RED));
        } catch (ResourceLocationException e) {
            component.set(1, Component.literal("Resource location is invalid").withStyle(ChatFormatting.RED));
        }
    }

    private TextBoxComponent priorityText = new TextBoxComponent(screen, 7, 47, 16, 15, 2, false, Component.literal("Priority"),
            this::savePriority) {
        @Override
        protected boolean isCharValid(char c) {
            return Character.isDigit(c);
        }

        @Override
        protected List<Component> getComponents() {
            if (!isFocused()) {
                return List.of(
                        Component.literal("Priority Setting").withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("Priority determines when this operation will take place, goes from highest to lowest in order of operations.").withStyle(ChatFormatting.GRAY),
                        Component.literal("Currently set to: ").append(Component.literal("" + savePriority(priorityText.getText())).withStyle(ChatFormatting.GOLD)));
            }
            return new ArrayList<>();
        }
    }.addPadding(-2, 3);

    private int savePriority(String text) {
        try {
            int parsed = Integer.parseInt(text);
            if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER) {
                itemNodeOperation.setPriority(parsed);
            }
            priorityText.setFocused(false);
            return parsed;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private ButtonComponent downArrowButton = new ButtonComponent(screen, new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
            153, 48, 14, 14, 0, 0, 0, 0, Component.literal("∨"),
            (button, i) -> textBoxComponent.doComplete()) {

        @Override
        public void drawComponent(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            this.draw(graphics);
            int i = 16777215;
            graphics.pose().pushPose();
            this.renderString(graphics, Minecraft.getInstance().font, i | Mth.ceil(this.alpha * 255.0F) << 24);
            graphics.pose().popPose();
        }
    };

    private List<ButtonComponent> optionButtons = new ArrayList<>() {
        {
            add(new ToggleButtonComponent(screen,
                    new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
                    6, 65, 12, 12, 84, 124, 84, 124 + 12, Component.literal("Blacklist Button"), ConfigurateOperation.this::toggleBlackList) {
                @Override
                protected List<Component> getComponents() {
                    return List.of(Component.literal("Blacklist").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal("Toggle to either blacklist chosen items or whitelist."),
                            Component.literal("Current Mode: " + (getState() ? "Blacklisted" : "Whitelisted")));
                }
            });
            add(new ToggleButtonComponent(screen,
                    new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
                    6 + 12, 65, 12, 12, 72, 124, 72, 124 + 12, Component.literal("Match NBT Button"), ConfigurateOperation.this::toggleNBT) {
                @Override
                protected List<Component> getComponents() {
                    return List.of(Component.literal("Match NBT").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal("Toggle match nbt data"),
                            Component.literal("Current Mode: " + (getState() ? "Matching NBT" : "Not Matching NBT")));
                }
            });
            add(new ToggleButtonComponent(screen,
                    new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
                    6 + 12 + 12, 65, 12, 12, 60, 124, 60, 124 + 12, Component.literal("Check Size Button"), ConfigurateOperation.this::toggleMatchSize) {
                @Override
                protected List<Component> getComponents() {
                    return List.of(Component.literal("Match Size").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal("Toggle to match the itemstack size, useful for either only inserting/extract n amount"),
                            Component.literal("Current Mode: " + (getState() ? "Matching Size" : "Not Matching Size")));
                }
            });
            add(new ButtonComponent(screen,
                    new ResourceLocation(Dispatch.MODID, "textures/gui/widgets.png"),
                    116, 65, 12, 12, 24, 124, 24, 124 + 12, Component.literal("Clear Button"), ConfigurateOperation.this::clearScreen) {
                @Override
                protected List<Component> getComponents() {
                    return List.of(Component.literal("Clear Operation").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            });
        }
    };

    private void clearScreen(ButtonComponent buttonComponent, int i) {
        if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER)
            itemNodeOperation = new ItemNodeOperation(new ArrayList<>(), false, false, 0, null);
    }

    private void toggleMatchSize(ButtonComponent button, int i) {
        if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER) itemNodeOperation.setMatchSize(button.state);
    }

    private void toggleNBT(ButtonComponent button, int i) {
        if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER) itemNodeOperation.setMatchNBT(button.state);
    }

    private void toggleBlackList(ButtonComponent button, int i) {
        if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER) itemNodeOperation.setBlackList(button.state);
    }

    private void saveOperation(ButtonComponent buttonComponent, int i) {

        if (screen instanceof CableContainerScreen cableContainerScreen) {
            if (subNetwork.getCapability() == ForgeCapabilities.ITEM_HANDLER) {
                itemNodeOperation.setDirection(buttonDirection);

                LevelNetworkHandler.getHandler(Minecraft.getInstance().level).getSubnetFromPos(cableContainerScreen.getCurrentSubnet().getCapability(), cableContainerScreen.getNodeProper()).ifPresent(subNetwork1 -> {
                    LevelNode node = cableContainerScreen.getNodeProper();
                    node.addOperation(itemNodeOperation);
                    PacketHandler.sendToServer(LevelNetworkPacket.of(subNetwork1.getCapability(), node));
                });

            }
        }
        closeOperationPage();

    }

    public void closeOperationPage() {
        button.setVisible(false);
        saveButton.setVisible(false);
        directionButton.setVisible(false);
        textBoxComponent.setVisible(false);
        downArrowButton.setVisible(false);
        for (ButtonComponent optionButton : optionButtons) {
            optionButton.setVisible(false);
        }
        setVisible(false);
        if (screen instanceof CableContainerScreen screen1) {
            for (CapabilityTabButton button : screen1.getCapabilityButtons()) {
                button.setVisible(true);
            }
            screen1.getIoButtons().forEach((capability, pairs) ->
                    pairs.forEach(directionIOHandlerButtonPair -> directionIOHandlerButtonPair.getSecond().setVisible(true)));
            screen1.getOpenOperationButton().setVisible(true);
            screen1.getDeleteFilter().setVisible(true);
            for (ButtonComponent availableFilter : screen1.getList().getAvailableFilters()) {
                availableFilter.setVisible(true);
            }
        }

    }

    private Direction buttonDirection = Direction.NORTH;

    public ConfigurateOperation(ICustomScreen screen, int x, int y) {
        super(screen, x, y, 176, 83, 0, 0, new ResourceLocation(
                Dispatch.MODID, "textures/gui/config_design.png"
        ), Component.literal("Operation"));
    }

    public FakeItemButton getButton() {
        return button;
    }

    public ButtonComponent getDirectionButton() {
        return directionButton;
    }

    public ButtonComponent getSaveButton() {
        return saveButton;
    }

    public TextBoxComponent getTextBoxComponent() {
        return textBoxComponent;
    }

    public ButtonComponent getDownArrowButton() {
        return downArrowButton;
    }

    public List<ButtonComponent> getOptionButtons() {
        return optionButtons;
    }

    public TextBoxComponent getPriorityText() {
        return priorityText;
    }

    @Override
    public void onInit() {
        button.setVisible(false);
        saveButton.setVisible(false);
        getDirectionButton().setVisible(false);
        textBoxComponent.setVisible(false);
        downArrowButton.setVisible(false);
        for (ButtonComponent optionButton : optionButtons) {
            optionButton.setVisible(false);
        }
    }

    private void onDirectionalButton(ButtonComponent buttonComponent, int i) {
        if (screen instanceof CableContainerScreen cableContainerScreen) {
            if (i == 0) {
                int dirIndex = (buttonDirection.ordinal() + 1) % Direction.values().length;
                buttonDirection = Direction.values()[dirIndex];
                int[] colour = IOHandlerButton.getColourFromIndex(cableContainerScreen.getNodeProper().getDirectionalIO().get(buttonDirection));
                buttonComponent.setTextureOffset(colour[0], colour[1]);
            } else {
                int dirIndex = (buttonDirection.ordinal() - 1 + Direction.values().length) % Direction.values().length;
                buttonDirection = Direction.values()[dirIndex];
                int[] colour = IOHandlerButton.getColourFromIndex(cableContainerScreen.getNodeProper().getDirectionalIO().get(buttonDirection));
                buttonComponent.setTextureOffset(colour[0], colour[1]);
            }
        }


    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return false;
    }

    @Override
    public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
        return false;
    }

    @Override
    public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_) {
        return false;
    }

    @Override
    protected void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(texture, getCorrectX(), getCorrectY(), u, v, width, height, width, height);
        int y = getCorrectY() + 9;
        int x = getCorrectX() + 30;

        for (Component component1 : component) {
            RenderingUtil.drawText(
                    component1,
                    graphics,
                    x,
                    y,
                    ChatFormatting.WHITE.getColor()
            );
            y += 10;
        }

    }

    public void recreatePage(AbstractSubNetwork currentSubNetworkActive) {

        // set the operation card to the correct capability so item and fluid
        subNetwork = currentSubNetworkActive;
        if (currentSubNetworkActive.getCapability() == ForgeCapabilities.ITEM_HANDLER) {
            setVisible(true);
            setActive(true);
            button.setVisible(true);
            textBoxComponent.setVisible(true);
            getDirectionButton().setVisible(true);
            downArrowButton.setVisible(true);
            saveButton.setVisible(true);

            for (ButtonComponent optionButton : optionButtons) {
                optionButton.setVisible(true);
            }
            if (screen instanceof CableContainerScreen screen1) {
                for (CapabilityTabButton button : screen1.getCapabilityButtons()) {
                    button.setVisible(false);
                }
                screen1.getIoButtons().forEach((capability, pairs) ->
                        pairs.forEach(directionIOHandlerButtonPair -> directionIOHandlerButtonPair.getSecond().setVisible(false)));
                screen1.getOpenOperationButton().setVisible(false);
                screen1.getDeleteFilter().setVisible(false);
                for (ButtonComponent availableFilter : screen1.getList().getAvailableFilters()) {
                    availableFilter.setVisible(false);
                }
            }
        }
    }
}
