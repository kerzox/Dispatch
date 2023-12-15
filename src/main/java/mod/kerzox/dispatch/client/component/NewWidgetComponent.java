package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class NewWidgetComponent implements Renderable, GuiEventListener, NarratableEntry {

    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    private boolean focused;
    protected ICustomScreen screen;

    public NewWidgetComponent(ICustomScreen screen, int x, int y, int width, int height, Component message) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = message;
    }

    public void onInit() {

    }

    @Override
    public void render(GuiGraphics p_282421_, int p_93658_, int p_93659_, float p_93660_) {
        if (!this.visible) return;
        this.isHovered = isMouseOver(p_93658_, p_93659_);
        drawComponent(p_282421_, p_93658_, p_93659_, p_93660_);
        if (isHovered) {
            onHover(p_282421_, p_93658_, p_93659_, p_93660_);
        }
    }

    public Component getMessage() {
        return this.message;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered() || this.isFocused();
    }

    protected void onHover(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (getComponents().isEmpty()) return;
        else drawToolTips(graphics, mouseX, mouseY, getComponents());
    }

    protected List<Component> getComponents() {
        return new ArrayList<>();
    }

    protected abstract void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setVisible(boolean active) {
        this.visible = active;
    }

    public void playDownSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public void onRelease(double p_93669_, double p_93670_) {
    }

    protected void onDrag(double p_93636_, double p_93637_, double p_93638_, double p_93639_) {
    }

    protected void drawToolTips(GuiGraphics graphics, int mouseX, int mouseY, List<Component> component) {
        graphics.renderTooltip(Minecraft.getInstance().font, component, Optional.empty(), ItemStack.EMPTY, mouseX, mouseY);
    }

    protected void drawToolTips(GuiGraphics graphics, int mouseX, int mouseY, Component... component) {
        this.drawToolTips(graphics, mouseX, mouseY, List.of(component));
    }

    public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(p_93643_)) {
                boolean flag = this.clicked(p_93641_, p_93642_);
                if (flag) {
                    this.onClick(p_93641_, p_93642_, p_93643_);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double p_93684_, double p_93685_, int p_93686_) {
        if (this.isValidClickButton(p_93686_)) {
            this.onRelease(p_93684_, p_93685_);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int p_93652_) {
        return p_93652_ == 0;
    }

    public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_) {
        if (this.isValidClickButton(p_93647_)) {
            this.onDrag(p_93645_, p_93646_, p_93648_, p_93649_);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseMoved(double p_94758_, double p_94759_) {
        GuiEventListener.super.mouseMoved(p_94758_, p_94759_);
    }

    @Override
    public boolean mouseScrolled(double p_94734_, double p_94735_, double p_94736_) {
        return GuiEventListener.super.mouseScrolled(p_94734_, p_94735_, p_94736_);
    }

    @Override
    public boolean keyPressed(int p_94745_, int p_94746_, int p_94747_) {
        return GuiEventListener.super.keyPressed(p_94745_, p_94746_, p_94747_);
    }

    @Override
    public boolean keyReleased(int p_94750_, int p_94751_, int p_94752_) {
        return GuiEventListener.super.keyReleased(p_94750_, p_94751_, p_94752_);
    }

    @Override
    public boolean charTyped(char p_94732_, int p_94733_) {
        return GuiEventListener.super.charTyped(p_94732_, p_94733_);
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        boolean xBounds = ((x > getCorrectX()) && (x < getCorrectX() + this.width));
        boolean yBounds = ((y > getCorrectY()) && (y < getCorrectY() + this.height));
        return this.visible && this.active && xBounds && yBounds;
    }

    public boolean isMouseOverIgnoreVisible(double x, double y) {
        boolean xBounds = ((x > getCorrectX()) && (x < getCorrectX() + this.width));
        boolean yBounds = ((y > getCorrectY()) && (y < getCorrectY() + this.height));
        return this.active && xBounds && yBounds;
    }

    @Override
    public void setFocused(boolean p_265728_) {
        this.focused = p_265728_;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @javax.annotation.Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent p_265640_) {
        if (this.active && this.visible) {
            return !this.isFocused() ? ComponentPath.leaf(this) : null;
        } else {
            return null;
        }
    }


    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return GuiEventListener.super.getCurrentFocusPath();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return GuiEventListener.super.getRectangle();
    }

    protected boolean clicked(double p_93681_, double p_93682_) {
        return isMouseOver(p_93681_, p_93682_);
    }

    public int getCorrectX() {
        return this.getX() + screen.getGuiLeft();
    }

    public int getCorrectY() {
        return this.getY() + screen.getGuiTop();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getAlpha() {
        return alpha;
    }

    public ICustomScreen getScreen() {
        return screen;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarrationPriority.FOCUSED;
        } else {
            return this.isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }

    @Override
    public boolean isActive() {
        return NarratableEntry.super.isActive();
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }



    @Override
    public int getTabOrderGroup() {
        return GuiEventListener.super.getTabOrderGroup();
    }


    public void onClick(double mouseX, double mouseY, int button) {

    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void tick() {

    }
}
