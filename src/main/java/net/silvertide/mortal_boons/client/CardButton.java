package net.silvertide.mortal_boons.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.mortal_boons.MortalBoons;

public class CardButton {
    public static final int WIDTH = 34;
    public static final int HEIGHT = 11;

    private static final ResourceLocation COMPONENTS = MortalBoons.id("textures/gui/menu_components.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int NORMAL_U = 0;
    private static final int HOVERED_U = 35;
    private static final int PRESSED_U = 70;
    private static final int ROW_BASE_V = 60;
    private static final int ROW_STRIDE_V = 12;
    private static final int TEXT_AREA_X = 3;
    private static final int TEXT_AREA_Y = 2;
    private static final int TEXT_AREA_WIDTH = 28;
    private static final int TEXT_AREA_HEIGHT = 6;
    private static final int TEXT_GLYPH_HEIGHT = 8;
    private static final int TEXT_COLOR = 0xEED3AB;

    private final int x;
    private final int y;
    private final int tier;
    private final Component label;
    private final Runnable onPress;
    private boolean pressed;

    public CardButton(int x, int y, int tier, Component label, Runnable onPress) {
        this.x = x;
        this.y = y;
        this.tier = tier;
        this.label = label;
        this.onPress = onPress;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
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
        int stateU = pressed && over ? PRESSED_U : over ? HOVERED_U : NORMAL_U;
        int rowV = ROW_BASE_V + (tier - 1) * ROW_STRIDE_V;
        guiGraphics.blit(COMPONENTS, x, y, stateU, rowV, WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        drawLabel(guiGraphics, font);
    }

    private void drawLabel(GuiGraphics guiGraphics, Font font) {
        float scale = Math.min(TEXT_AREA_WIDTH / (float) font.width(label),
                TEXT_AREA_HEIGHT / (float) TEXT_GLYPH_HEIGHT);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + TEXT_AREA_X + TEXT_AREA_WIDTH / 2.0F,
                y + TEXT_AREA_Y + TEXT_AREA_HEIGHT / 2.0F, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, label, -font.width(label) / 2, -TEXT_GLYPH_HEIGHT / 2, TEXT_COLOR, false);
        guiGraphics.pose().popPose();
    }
}
