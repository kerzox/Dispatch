package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.CableContainerScreen;
import mod.kerzox.dispatch.client.gui.ICustomScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

import static mod.kerzox.dispatch.client.render.RenderingUtil.custom;

public class TextBoxComponent extends NewWidgetComponent {


    public interface ICompletableTextBox {
        void onEnter(String text);
    }

    private int paddingX = 0;
    private int paddingY = 0;

    // tick for each frame
    private int frameTick;

    // the actual text box
    private String text = "";

    // this will correspond to where the cursor is currently along the string
    private int cursorIndex = 0;

    /*
        This is for the wrapping text
     */
    private int startFrom = 0;


    private int maxBound = 19;

    private boolean isICursor;

    private ICompletableTextBox completableTextBox;

    private boolean checkTextLength;

    public TextBoxComponent(ICustomScreen screen, int x, int y, int width, int height, int maxBound, boolean checkTextLength, Component message, ICompletableTextBox completableTextBox) {
        super(screen, x, y, width, height, message);
        this.completableTextBox = completableTextBox;
        this.maxBound = maxBound;
        this.checkTextLength = checkTextLength;
    }

    public TextBoxComponent addPadding(int xPad, int yPad) {
        this.paddingX = xPad;
        this.paddingY = yPad;
        return this;
    }

    protected boolean isCharValid(char c) {
        return true;
    }

    @Override
    public void tick() {
        frameTick++;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean charTyped(char p_94732_, int p_94733_) {
        System.out.println(p_94732_ + " : " + p_94733_);
        if (isCharValid(p_94732_)) {
            addChar(p_94732_);
        }
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (isFocused()) {
            int index = getCursorIndexFromMouseX(mouseX);
            moveCursorToIndex(getIndexFromMouseX(mouseX));
            if (index >= text.length()) isICursor = false;
        }
    }

    private void moveCursorToIndex(int index) {
        cursorIndex = index;
        isICursor = true;
    }

    /*
    TODO
        - Add copy and paste
        - add highlight
     */

    @Override
    public boolean keyPressed(int key, int p_98101_, int p_98102_) {
        System.out.println(key + " : " + p_98101_ + " : " + p_98102_ + " : " + text.length());
        if (screen instanceof CableContainerScreen screen1) {
            if (key == 256 && screen1.shouldCloseOnEsc()) {
                screen1.onClose();
                return true;
            }
        }

        if (257 == key) {
            doComplete();
        }

        if (259 == key && text.length() > 0) {
            System.out.println(cursorIndex);
            removeChar(cursorIndex - 1);
        }

        // left arrow
        if (263 == key && text.length() > 0) {
            cursorIndex -= cursorIndex > 0 ? 1 : 0;
            isICursor = true;
            if (startFrom > 0) {
                startFrom--;
            }
        }

        // right arrow
        if (262 == key && text.length() > 0) {
            cursorIndex += cursorIndex >= text.length() ? 0 : 1;
            isICursor = true;
            if (cursorIndex >= startFrom + maxBound) {
                startFrom++;
            }
            if (cursorIndex >= text.length()) isICursor = false;
        }

        return true;
    }

    public void doComplete() {
        completableTextBox.onEnter(this.text);
    }

    private char getCharacterAtMouseX(double mouseX) {
        return this.text.charAt(getIndexFromMouseX(mouseX));
    }

    private int getCursorIndexFromMouseX(double mouseX) {
//        int x = (int) ((startFrom + mouseX / 6)) - getX();
        if (text.length() <= 0) return 0;
        int x = (int) (startFrom + (mouseX - getCorrectX()) / Minecraft.getInstance().font.width(String.valueOf(text.charAt(0))) - 1);
        System.out.println(x);
        if (x >= 0) return Math.min(x, text.length());
        else return 0;
    }

    private int getIndexFromMouseX(double mouseX) {
//        int x = (int) ((startFrom + mouseX / 6)) - getX();
        if (text.length() <= 0) return 0;
        int x = (int) (startFrom + (mouseX - getCorrectX()) / Minecraft.getInstance().font.width(String.valueOf(text.charAt(0))) - 1);
        System.out.println(x);
        if (x >= 0) return Math.min(x, text.length() <= 0 ? 0 :  text.length() - 1);
        else return 0;
    }

    private void removeChar(int index) {
        if (index <= 0 && index >= text.length()) return;
        StringBuilder stringBuilder = new StringBuilder(text);
        stringBuilder.deleteCharAt(Math.max(index, 0));
        text = stringBuilder.toString();
        if (cursorIndex <= 0) {
            cursorIndex = 0;
        } else cursorIndex--;
        if (cursorIndex > maxBound) {
            if (startFrom < 1) {
                startFrom = 0;
            } else startFrom--;
        } else {
            startFrom = 0;
        }
    }

    private void addChar(char character) {
        if (checkTextLength && text.length() >= maxBound) return;

        StringBuilder stringBuilder = new StringBuilder(text);
        stringBuilder.insert(cursorIndex, character);
        text = stringBuilder.toString();
        cursorIndex++;
        if (cursorIndex > maxBound) {
            startFrom++;
        }
    }

    public void setText(String s) {
        this.text = s;
    }


    @Override
    protected void drawComponent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int stringX = getCorrectX() + 4 + paddingX;
        int textHeight = paddingY;

        for (int i = 0; i < text.length(); i++) {
            if (i >= startFrom && i <= (startFrom + maxBound) - 1) {
                graphics.drawString(Minecraft.getInstance().font, String.valueOf(text.charAt(i)), stringX, getCorrectY() + (height / 2) - textHeight, 0xffffff, false);
                stringX += Minecraft.getInstance().font.getSplitter().stringWidth(String.valueOf(text.charAt(i)));
            }
        }

        if (isFocused()) {
            if (this.frameTick / 10 % 2 == 0) {

                int textXfromIndex = 4 + paddingX;

                if (!text.equals("")) {
                    textXfromIndex = (int) ((int) Minecraft.getInstance().font.getSplitter().stringWidth(text.substring(startFrom))) + 2;
                }

                if (isICursor) {
                    int newX = (int) Minecraft.getInstance().font.getSplitter().stringWidth(text.substring(cursorIndex == 0 ? cursorIndex : cursorIndex));
                    graphics.fill(getCorrectX() + textXfromIndex - newX,
                            getCorrectY() + (height / 2) - textHeight - 1,
                            getCorrectX() + 1 + textXfromIndex - newX,
                            getCorrectY() + (height / 2) - textHeight + 9, custom("ffffff", 100));
                } else {
                    graphics.drawString(Minecraft.getInstance().font, "_", getCorrectX() + textXfromIndex,
                            getCorrectY() + (height / 2) - textHeight, 0xffffff, false);
                }
            }
        }

    }
}
