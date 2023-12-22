package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.common.util.DispatchUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

public class IOHandlerButton extends ButtonComponent {

    public static int[][] colours = {
            {36, 112},        // defaultColour
            {24, 112},        // outputColour
            {12, 112},        // inputColour
            {0, 112},         // combinedColour
            {48, 112}         // NoneColour
    };

//    public static int[][] colours = {
//            {36, 112},        // defaultColour
//            {24, 112},        // red
//            {12, 112},        // blue
//            {0, 112},         // purple
//            {48, 112},         // black
//            {60, 112} ,        // yellow
//            {72, 112} ,        // green
//    };

    private LevelNode.IOTypes currentSetting = LevelNode.IOTypes.DEFAULT;
    private Direction direction = Direction.NORTH;
    private LevelNode node;
    private AbstractSubNetwork subNetwork;

    public IOHandlerButton(ICustomScreen screen, AbstractSubNetwork subNetwork,
                           LevelNode node, ResourceLocation texture, int x, int y, int width, int height, Component component, Direction direction, IPressable btn) {
        super(screen, texture, x, y, width, height, 0,0,0,0, component, btn);
        setTextureOffset(colours[currentSetting.ordinal()][0], colours[currentSetting.ordinal()][1]);
        this.direction = direction;
        this.node = node;
        this.subNetwork = subNetwork;
    }

    public static int[] getColourFromIndex(LevelNode.IOTypes ioTypes) {
        return colours[ioTypes.ordinal()];
    }

    @Override
    protected List<Component> getComponents() {
        return List.of(
                Component.literal(direction.getSerializedName().substring(0, 1).toUpperCase() + direction.getSerializedName().substring(1)).withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.literal(currentSetting.getSerializedName().substring(0, 1).toUpperCase() + currentSetting.getSerializedName().substring(1) + " Mode")
        );
    }

    public AbstractSubNetwork getSubNetwork() {
        return subNetwork;
    }

    public LevelNode getNode() {
        return node;
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

        BlockState state = Minecraft.getInstance().level.getBlockState(node.getPos().relative(direction));
        if (currentSetting != LevelNode.IOTypes.NONE) {
            if (DispatchUtil.getCapabilityFromDirection(Minecraft.getInstance().level, node.getPos(), direction, subNetwork.getCapability()).isPresent()) {
                if (!state.isAir()) {
                    graphics.pose().pushPose();
                    float scaled = .5f / .5f;
                    graphics.pose().scale(.5f, .5f, .5f);
                    graphics.pose().translate(getCorrectX() * scaled, getCorrectY() * scaled, 0);
                    graphics.renderItem(new ItemStack(state.getBlock().asItem()), (int) (getCorrectX() * scaled) + 4, (int) (getCorrectY() * scaled) + 4);

                    graphics.pose().popPose();
                }
            }
        }

        this.draw(graphics);
    }

}
