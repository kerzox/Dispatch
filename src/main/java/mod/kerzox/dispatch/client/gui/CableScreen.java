package mod.kerzox.dispatch.client.gui;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.capability.LevelNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CableScreen extends Screen implements ICustomScreen {

    protected int left;
    protected int top;
    protected int xSize;
    protected int ySize;
    protected int imageWidth = 176;
    protected int imageHeight = 166;

    private ResourceLocation GUI;
    private int backgroundColour;


    protected CableScreen(LevelNode node) {
        super(Component.literal("Cable Config"));
        this.GUI = new ResourceLocation(Dispatch.MODID, "textures/gui/cable.png");
        this.backgroundColour = 0;
        this.xSize = 256;
        this.ySize = 256;
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


    public static void draw(LevelNode node) {
        Minecraft.getInstance().setScreen(new CableScreen(node));
    }

    @Override
    protected void init() {
        this.left = (this.width - this.imageWidth) / 2;
        this.top = (this.height - this.imageHeight) / 2;

    }

    @Override
    public void tick() {

    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float partialTick) {
        if (GUI != null) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            graphics.blit(GUI, i, j, 0, 0, this.imageWidth, this.imageHeight);
        }
        super.render(graphics, pMouseX, pMouseY, partialTick);
    }

    @Override
    public int getGuiLeft() {
        return 0;
    }

    @Override
    public int getGuiTop() {
        return 0;
    }
}
