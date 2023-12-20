package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.client.render.RenderingUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScrollbarComponent extends TexturedWidgetComponent {

    protected int x1;
    protected int y1;
    protected int prevX, prevY;
    protected int minimum;
    protected int maximum;
    protected int initalY;
    protected int scrollbarWidth, scrollbarHeight;

    private ResourceLocation texture;
    private IScrollBarUpdatable updatable;
    private ICustomScreen screen;

    public ScrollbarComponent(ICustomScreen screen,
                              int x, int y,
                              int width, int height,
                              int scrollbarWidth, int scrollbarHeight,
                              int u, int v,
                              int minBound, int maxBound,
                              Component message, ResourceLocation texture, IScrollBarUpdatable updatable) {
        super(screen, x, y, width, height, u, v, texture, message);
        this.screen = screen;
        this.u = u;
        this.v = v;
        this.x1 = x;
        this.y1 = y;
        this.prevX = x;
        this.prevY = y;
        this.scrollbarWidth = scrollbarWidth;
        this.scrollbarHeight = scrollbarHeight;
        this.texture = texture;
        this.updatable = updatable;
        this.minimum = minBound;
        this.maximum = maxBound;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getPrevX() {
        return prevX;
    }

    public int getPrevY() {
        return prevY;
    }

    @Override
    protected boolean clicked(double p_93681_, double p_93682_) {
        return isMouseOver(p_93681_, p_93682_);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.initalY = (int) mouseY;
        setFocused(true);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double p_93638_, double p_93639_) {
        double minimumValue = y1 + minimum + screen.getGuiTop();
        double maximumValue = y1 + maximum + screen.getGuiTop();
        boolean min = ((minimumValue) < mouseY);
        boolean max = ((maximumValue) > mouseY);
        scroll(mouseX, Mth.clamp(mouseY, minimumValue + 2, maximumValue - 2));
    }

    public void setFocused(boolean p_265705_) {
        super.setFocused(p_265705_);
    }

    private double snapToNearest(double value, int stepSize)
    {
        int minValue = 0;
        int maxValue = 50;

        if(stepSize <= 0D)
            return Mth.clamp(value, 0D, 1D);

        value = Mth.lerp(Mth.clamp(value, 0D, 1D), minValue, maxValue);

        value = (stepSize * Math.round(value / stepSize));

        value = Mth.clamp(value, minValue, maxValue);

        return Mth.map(value, minValue, maxValue, 0D, 1D);
    }

    private void scroll(double mouseX, double mouseY) {
        int pos2 = initalY;
        int xOnScreen = this.getX() + screen.getGuiLeft();
        int yOnScreen = this.getY() + screen.getGuiTop();
        int y = (int) (Math.round(mouseY) - 2 - screen.getGuiTop());
        setPosition(getX(), (int) (Math.round(mouseY) - 2 - screen.getGuiTop()));
        updatable.onUpdate(0, y);
    }

    public void setBounds(int min, int max) {
        this.minimum = min;
        this.maximum = max;
    }


    public void setPosition2(int p_265617_, int p_265577_) {
        this.x1 = p_265617_;
        this.y1 = p_265577_;
    }

    @Override
    protected void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int xOnScreen = this.getX() + screen.getGuiLeft();
        int yOnScreen = this.getY() + screen.getGuiTop();
        graphics.fill(x1 + screen.getGuiLeft(),
                y1 + screen.getGuiTop(),
                this.scrollbarWidth + x1 + screen.getGuiLeft(),
                y1 + screen.getGuiTop() + this.scrollbarHeight, RenderingUtil.custom("262626", 100));
        graphics.blit(texture, xOnScreen, yOnScreen, u, v, width, height, 256, 256);
    }


}
