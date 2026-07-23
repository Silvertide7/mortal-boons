package net.silvertide.mortal_boons.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.mortal_boons.MortalBoons;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class CardButton {
    public static final int ACTION_WIDTH = 34;
    public static final int ACTION_HEIGHT = 11;
    public static final int TEMPT_FATE_WIDTH = 42;
    public static final int TEMPT_FATE_HEIGHT = 17;

    private static final ResourceLocation COMPONENTS = MortalBoons.id("textures/gui/menu_components.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int ACTION_ROW_BASE_V = 60;
    private static final int ACTION_ROW_STRIDE_V = 12;
    private static final int ACTION_TEXT_CENTER_X = 3 + 28 / 2;
    private static final int ACTION_TEXT_CENTER_Y = 2 + 6 / 2;
    private static final int TEMPT_FATE_ROW_V = 149;
    private static final int TEXT_GLYPH_HEIGHT = 8;
    private static final float TEXT_SCALE = 0.6F;
    private static final int TEXT_COLOR = 0xEED3AB;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int normalU;
    private final int hoveredU;
    private final int pressedU;
    private final int disabledU;
    private final int rowV;
    private final float labelCenterX;
    private final float labelCenterY;
    private final Component label;
    private final Runnable onPress;
    private final BooleanSupplier enabled;
    private final Supplier<Component> disabledReason;
    private boolean pressed;

    private CardButton(int x, int y, int width, int height, int rowV, float labelCenterX, float labelCenterY,
                       Component label, Runnable onPress, BooleanSupplier enabled,
                       Supplier<Component> disabledReason) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.normalU = 0;
        this.hoveredU = width + 1;
        this.pressedU = (width + 1) * 2;
        this.disabledU = (width + 1) * 3;
        this.rowV = rowV;
        this.labelCenterX = labelCenterX;
        this.labelCenterY = labelCenterY;
        this.label = label;
        this.onPress = onPress;
        this.enabled = enabled;
        this.disabledReason = disabledReason;
    }

    public static CardButton action(int x, int y, int tier, Component label, Runnable onPress) {
        return new CardButton(x, y, ACTION_WIDTH, ACTION_HEIGHT,
                ACTION_ROW_BASE_V + (tier - 1) * ACTION_ROW_STRIDE_V,
                x + ACTION_TEXT_CENTER_X, y + ACTION_TEXT_CENTER_Y, label, onPress, () -> true, Component::empty);
    }

    public static CardButton temptFate(int x, int y, Component label, Runnable onPress, BooleanSupplier enabled,
                                       Supplier<Component> disabledReason) {
        return new CardButton(x, y, TEMPT_FATE_WIDTH, TEMPT_FATE_HEIGHT, TEMPT_FATE_ROW_V,
                x + TEMPT_FATE_WIDTH / 2.0F, y + TEMPT_FATE_HEIGHT / 2.0F, label, onPress, enabled, disabledReason);
    }

    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }

    public Component disabledReason() {
        return disabledReason.get();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (isEnabled() && isMouseOver(mouseX, mouseY)) {
            pressed = true;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY) {
        if (!pressed) {
            return false;
        }
        pressed = false;
        if (isMouseOver(mouseX, mouseY)) {
            onPress.run();
            return true;
        }
        return false;
    }

    public void render(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        boolean over = isMouseOver(mouseX, mouseY);
        int stateU = !isEnabled() ? disabledU : pressed && over ? pressedU : over ? hoveredU : normalU;
        guiGraphics.blit(COMPONENTS, x, y, stateU, rowV, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        drawLabel(guiGraphics, font);
    }

    private void drawLabel(GuiGraphics guiGraphics, Font font) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(labelCenterX, labelCenterY, 0);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        guiGraphics.drawString(font, label, -font.width(label) / 2, -TEXT_GLYPH_HEIGHT / 2, TEXT_COLOR, false);
        guiGraphics.pose().popPose();
    }
}
