package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.common.event.CommonEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ButtonComponent extends TexturedWidgetComponent {

    protected IPressable button;
    protected boolean state;
    protected int u1, v1, u2, v2;

    protected int tick;

    public ButtonComponent(ICustomScreen screen, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int u2, int v2, Component component, IPressable btn) {
        super(screen, x, y, width, height, u, v, texture, component);
        this.button = btn;
        this.u2 = u2;
        this.v2 = v2;
        this.u1 = u;
        this.v1 = v;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        playDownSound();
        this.button.onPress(this, button);
        tick = 1 + CommonEvents.getClientTick();
    }

    @Override
    protected boolean isValidClickButton(int p_93652_) {
        return true;
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

    @Override
    public void tick() {
        if (tick >= CommonEvents.getClientTick()) {
            state = true;
        } else {
            state = false;
            tick++;
        }
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean b) {
        this.state =b;
    }

    public interface IPressable {
        void onPress(ButtonComponent button, int i);
    }

}
