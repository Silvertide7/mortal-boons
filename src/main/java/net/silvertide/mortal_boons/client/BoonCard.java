package net.silvertide.mortal_boons.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.network.FatestoneActionPayload;
import net.silvertide.mortal_boons.network.FatestoneScreenPayload;

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
    private static final int BUTTON_STACK_BOTTOM = 50;
    private static final int BUTTON_GAP = 1;
    private static final float HOVERED_SCALE = 1.05F;
    private static final float SHADOW_ALPHA = 0.15F;
    private static final int SHADOW_OFFSET = 2;

    private final FatestoneScreenPayload.SlotDisplay slot;
    private final int x;
    private final int y;
    private final List<CardButton> buttons = new ArrayList<>();

    public BoonCard(FatestoneScreenPayload payload, int slotIndex, int x, int y, boolean temptFateSlot) {
        this.slot = payload.slots().get(slotIndex);
        this.x = x;
        this.y = y;
        if (hasCard()) {
            List<FatestoneActionPayload.Action> actions = allowedActions(payload.allowedActions());
            for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
                FatestoneActionPayload.Action action = actions.get(actionIndex);
                buttons.add(CardButton.action(buttonX(), buttonY(actionIndex, actions.size()), slot.tier(),
                        Component.translatable(labelKey(action)),
                        () -> PacketDistributor.sendToServer(
                                new FatestoneActionPayload(payload.pos(), action, slotIndex))));
            }
        } else if (temptFateSlot) {
            buttons.add(CardButton.temptFate(x + (WIDTH - CardButton.TEMPT_FATE_WIDTH) / 2,
                    y + (HEIGHT - CardButton.TEMPT_FATE_HEIGHT) / 2,
                    Component.translatable(labelKey(FatestoneActionPayload.Action.TEMPT_FATE)),
                    () -> PacketDistributor.sendToServer(new FatestoneActionPayload(
                            payload.pos(), FatestoneActionPayload.Action.TEMPT_FATE, slotIndex))));
        }
    }

    private static List<FatestoneActionPayload.Action> allowedActions(FatestoneScreenPayload.AllowedActions allowed) {
        List<FatestoneActionPayload.Action> actions = new ArrayList<>();
        if (allowed.reroll()) {
            actions.add(FatestoneActionPayload.Action.REROLL);
        }
        if (allowed.reforge()) {
            actions.add(FatestoneActionPayload.Action.REFORGE);
        }
        if (allowed.forsake()) {
            actions.add(FatestoneActionPayload.Action.FORSAKE);
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

    public boolean showsTooltip() {
        return hasCard() || buttons.isEmpty();
    }

    public List<Component> tooltipLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(slot.title());
        lines.addAll(slot.lines());
        return lines;
    }

    public void render(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, boolean hovered) {
        if (!hasCard()) {
            for (CardButton button : buttons) {
                button.render(guiGraphics, font, mouseX, mouseY);
            }
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

    private static String labelKey(FatestoneActionPayload.Action action) {
        return switch (action) {
            case TEMPT_FATE -> "mortal_boons.screen.button.tempt_fate";
            case REROLL -> "mortal_boons.screen.button.reroll";
            case REFORGE -> "mortal_boons.screen.button.reforge";
            case FORSAKE -> "mortal_boons.screen.button.forsake";
        };
    }

    private int buttonX() {
        return x + CONTENT_X + (CONTENT_WIDTH - CardButton.ACTION_WIDTH) / 2;
    }

    private int buttonY(int buttonIndex, int buttonCount) {
        int buttonsBelow = buttonCount - buttonIndex;
        return y + BUTTON_STACK_BOTTOM - buttonsBelow * CardButton.ACTION_HEIGHT
                - (buttonsBelow - 1) * BUTTON_GAP;
    }
}
