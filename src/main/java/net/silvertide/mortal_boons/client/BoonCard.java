package net.silvertide.mortal_boons.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.network.AltarActionPayload;
import net.silvertide.mortal_boons.network.AltarScreenPayload;

import java.util.ArrayList;
import java.util.List;

public class BoonCard {
    public static final int WIDTH = 44;
    public static final int HEIGHT = 59;

    private static final ResourceLocation COMPONENTS = MortalBoons.id("textures/gui/menu_components.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int[] CARD_U_BY_TIER = {0, 45, 90, 135};
    private static final int CARD_V = 0;
    private static final int CONTENT_X = 3;
    private static final int CONTENT_WIDTH = 38;
    private static final int BUTTON_STACK_BOTTOM = 48;
    private static final int BUTTON_GAP = 1;
    private static final float HOVERED_SCALE = 1.05F;
    private static final float SHADOW_ALPHA = 0.15F;
    private static final int SHADOW_OFFSET = 2;

    private final AltarScreenPayload.SlotDisplay slot;
    private final int x;
    private final int y;
    private final List<CardButton> buttons = new ArrayList<>();

    public BoonCard(AltarScreenPayload payload, int slotIndex, int x, int y) {
        this.slot = payload.slots().get(slotIndex);
        this.x = x;
        this.y = y;
        if (hasCard()) {
            List<AltarActionPayload.Action> actions = allowedActions(payload.allowedActions());
            for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
                AltarActionPayload.Action action = actions.get(actionIndex);
                buttons.add(new CardButton(buttonX(), buttonY(actionIndex, actions.size()), slot.tier(),
                        Component.translatable(labelKey(action)),
                        () -> PacketDistributor.sendToServer(
                                new AltarActionPayload(payload.altarPos(), action, slotIndex))));
            }
        }
    }

    private static List<AltarActionPayload.Action> allowedActions(AltarScreenPayload.AllowedActions allowed) {
        List<AltarActionPayload.Action> actions = new ArrayList<>();
        if (allowed.reroll()) {
            actions.add(AltarActionPayload.Action.REROLL);
        }
        if (allowed.reforge()) {
            actions.add(AltarActionPayload.Action.REFORGE);
        }
        if (allowed.forsake()) {
            actions.add(AltarActionPayload.Action.FORSAKE);
        }
        return actions;
    }

    public boolean hasCard() {
        return slot.tier() >= 1 && slot.tier() <= 4;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        for (CardButton button : buttons) {
            if (button.mouseClicked(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY) {
        boolean handled = false;
        for (CardButton button : buttons) {
            handled |= button.mouseReleased(mouseX, mouseY);
        }
        return handled;
    }

    public List<Component> tooltipLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(slot.title());
        lines.addAll(slot.lines());
        return lines;
    }

    public void render(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, boolean hovered) {
        if (!hasCard()) {
            return;
        }
        guiGraphics.pose().pushPose();
        if (hovered) {
            float centerX = x + WIDTH / 2.0F;
            float centerY = y + HEIGHT / 2.0F;
            guiGraphics.pose().translate(centerX, centerY, 0);
            guiGraphics.pose().scale(HOVERED_SCALE, HOVERED_SCALE, 1.0F);
            guiGraphics.pose().translate(-centerX, -centerY, 0);
        }
        int cardU = CARD_U_BY_TIER[slot.tier() - 1];
        guiGraphics.setColor(0.0F, 0.0F, 0.0F, SHADOW_ALPHA);
        guiGraphics.blit(COMPONENTS, x + SHADOW_OFFSET, y + SHADOW_OFFSET, cardU, CARD_V,
                WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(COMPONENTS, x, y, cardU, CARD_V, WIDTH, HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        if (hovered) {
            for (CardButton button : buttons) {
                button.render(guiGraphics, font, mouseX, mouseY);
            }
        }
        guiGraphics.pose().popPose();
    }

    private static String labelKey(AltarActionPayload.Action action) {
        return switch (action) {
            case ROLL -> "mortal_boons.screen.button.roll";
            case REROLL -> "mortal_boons.screen.button.reroll";
            case REFORGE -> "mortal_boons.screen.button.reforge";
            case FORSAKE -> "mortal_boons.screen.button.forsake";
        };
    }

    private int buttonX() {
        return x + CONTENT_X + (CONTENT_WIDTH - CardButton.WIDTH) / 2;
    }

    private int buttonY(int buttonIndex, int buttonCount) {
        int buttonsBelow = buttonCount - buttonIndex;
        return y + BUTTON_STACK_BOTTOM - buttonsBelow * CardButton.HEIGHT
                - (buttonsBelow - 1) * BUTTON_GAP;
    }
}
