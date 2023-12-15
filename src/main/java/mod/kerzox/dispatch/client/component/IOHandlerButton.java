package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.common.capability.LevelNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class IOHandlerButton extends ButtonComponent {

    private int[][] colours = {
            {36, 112},        // defaultColour
            {24, 112},        // outputColour
            {12, 112},        // inputColour
            {0, 112},         // combinedColour
            {48, 112}         // NoneColour
    };

    private LevelNode.IOTypes currentSetting = LevelNode.IOTypes.DEFAULT;
    private Direction direction = Direction.NORTH;

    public IOHandlerButton(ICustomScreen screen, ResourceLocation texture, int x, int y, int width, int height, Component component, Direction direction, IPressable btn) {
        super(screen, texture, x, y, width, height, 0,0,0,0, component, btn);
        setTextureOffset(colours[currentSetting.ordinal()][0], colours[currentSetting.ordinal()][1]);
        this.direction = direction;
    }

    @Override
    protected List<Component> getComponents() {
        return List.of(
                Component.literal(direction.getSerializedName().substring(0, 1).toUpperCase() + direction.getSerializedName().substring(1)).withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(currentSetting.getSerializedName().substring(0, 1).toUpperCase() + currentSetting.getSerializedName().substring(1) + " Mode")
        );
    }

    public void setCurrentSetting(LevelNode.IOTypes currentSetting) {
        this.currentSetting = currentSetting;
    }

    public LevelNode.IOTypes getCurrentSetting() {
        return currentSetting;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        playDownSound();
        this.button.onPress(this, button);
    }

    @Override
    public void tick() {

    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public void drawComponent(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        setTextureOffset(colours[currentSetting.ordinal()][0], colours[currentSetting.ordinal()][1]);
        this.draw(graphics);
    }

}
