package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.client.render.RenderingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ConfigurateOperation extends TexturedWidgetComponent {

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

        }
    };

    public ConfigurateOperation(ICustomScreen screen, int x, int y) {
        super(screen, x, y, 176, 83, 0, 0, new ResourceLocation(
                Dispatch.MODID, "textures/gui/config_design.png"
        ), Component.literal("Operation"));
    }

    public FakeItemButton getButton() {
        return button;
    }

    @Override
    public void onInit() {
        button.setVisible(false);
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
    }

    public void recreatePage() {
        setVisible(true);
        setActive(true);
        button.setVisible(true);
    }
}
