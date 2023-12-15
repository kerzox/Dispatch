package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToggleButtonComponent extends ButtonComponent {

    public ToggleButtonComponent(ICustomScreen screen, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int u2, int v2, Component component, IPressable btn) {
        super(screen, texture, x, y, width, height, u, v, u2, v2, component, btn);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.state = !state;
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
        if (!state) {
            setTextureOffset(u1, v1);
            this.draw(graphics);
        } else {
            setTextureOffset(u2, v2);
            this.draw(graphics);
        }
    }

}
