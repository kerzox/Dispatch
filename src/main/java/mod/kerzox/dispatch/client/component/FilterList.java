package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.client.render.RenderingUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class FilterList extends TexturedWidgetComponent {

    private ScrollbarComponent scrollBarComponent =
            new ScrollbarComponent(screen,
                    163, 6,
                    7, 9,
                    7, 55,
                    87, 0,
                    0, 50,
                    Component.literal("scrollbar"),
                    new ResourceLocation(Dispatch.MODID, "textures/gui/filter_list.png"), this::onScrollBar) {
                @Override
                protected void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                    int xOnScreen = this.getX() + screen.getGuiLeft();
                    int yOnScreen = this.getY() + screen.getGuiTop();
                    graphics.fill(x1 + screen.getGuiLeft(),
                            y1 + screen.getGuiTop(),
                            this.scrollbarWidth + x1 + screen.getGuiLeft(),
                            y1 + screen.getGuiTop() + this.scrollbarHeight, RenderingUtil.custom("d6d6d6", 100));
                    graphics.blit(texture, xOnScreen, yOnScreen, u, v, width, height, 150, 71);
                }
            };

    private int scrollIndex = 0;
    List<ButtonComponent> availableFilters = new ArrayList<>();

    private void onScrollBar(double notused, double y) {
        int scrollbarHeight = 45; // The height of the scrollbar
        int numberOfSteps = availableFilters.size() - (4 - 1);

        // Calculate the step size
        int stepSize = scrollbarHeight / Math.max(numberOfSteps, 1);

        // Assume the current scrollbar position
        int currentScrollPosition = (int) y - scrollBarComponent.getPrevY(); // Change this value as needed

        // Calculate the current step
        int currentStep = currentScrollPosition / stepSize;

        // Ensure the current step is within the valid range
        currentStep = Math.max(0, Math.min(numberOfSteps, currentStep));

        scrollIndex = currentStep;

    }

    public FilterList(ICustomScreen screen, int x, int y) {
        super(screen, x, y, 87, 71, 0, 0, new ResourceLocation(
                Dispatch.MODID, "textures/gui/filter_list.png"
        ), Component.literal("Filter list"));
    }

    @Override
    public void onInit() {

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

    public ScrollbarComponent getScrollBarComponent() {
        return scrollBarComponent;
    }

    @Override
    protected void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(texture, getCorrectX(), getCorrectY(), u, v, width, height, 150, height);
    }
}
