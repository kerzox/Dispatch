package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.client.gui.ICustomScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FakeItemButton extends NewWidgetComponent {

    protected ItemStack stack = ItemStack.EMPTY;

    public FakeItemButton(ICustomScreen screen, int x, int y, int width, int height, Component message) {
        super(screen, x, y, width, height, message);
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    protected List<Component> getComponents() {
        return stack.isEmpty() ?
                List.of(Component.literal("Filter ItemStack").withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("Size can determine how much to either insert or extract"))
        : List.of(Component.translatable(stack.getDescriptionId()).append(Component.literal(": " + stack.getCount())));
    }

    @Override
    protected void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!stack.isEmpty()) {
            graphics.pose().pushPose();
            float scaled = 1f;
            graphics.renderItem(stack, getCorrectX() + 1, getCorrectY() + 1);
            graphics.pose().popPose();
        }
    }
}
