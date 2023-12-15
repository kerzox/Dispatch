package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class TexturedWidgetComponent extends NewWidgetComponent {

    protected int u, v;
    protected ResourceLocation texture;

    public TexturedWidgetComponent(ICustomScreen screen,
                                   int x,
                                   int y,
                                   int width,
                                   int height,
                                   int u,
                                   int v,
                                   ResourceLocation texture,
                                   Component message) {
        super(screen, x, y, width, height, message);
        this.texture = texture;
        this.u = u;
        this.v = v;
    }

    protected void draw(GuiGraphics graphics) {
        this.draw(graphics, getCorrectX(), getCorrectY(), getWidth(), getHeight(), 256, 256);
    }

    protected void draw(GuiGraphics graphics, int width, int height) {
        this.draw(graphics, getCorrectX(), getCorrectY(), width, height, 256, 256);
    }

    protected void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        this.draw(graphics, getCorrectX(), getCorrectY(), width, height, 256, 256);
    }

    protected void draw(GuiGraphics graphics, int pX, int pY, int pWidth, int pHeight, int texHeight, int texWidth) {
        graphics.blit(getTexture(), pX, pY, this.u, this.v, pWidth, pHeight, texHeight, texWidth);
    }

    protected void setTextureOffset(int u, int v) {
        this.u = u;
        this.v = v;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
