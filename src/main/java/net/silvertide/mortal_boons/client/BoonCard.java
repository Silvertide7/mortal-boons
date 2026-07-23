package net.silvertide.mortal_boons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.OfferingManager;
import net.silvertide.mortal_boons.config.BoonClientConfig;
import net.silvertide.mortal_boons.menu.FatestoneMenu;
import net.silvertide.mortal_boons.network.FatestoneActionPayload;
import net.silvertide.mortal_boons.network.FatestoneScreenPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private static final int TEMPT_FATE_BUTTON_TOP = 30;
    private static final int ICON_FRAME_SIZE = 20;
    private static final int ICON_FRAME_ROW_V = 108;
    private static final int ICON_FRAME_STRIDE_U = ICON_FRAME_SIZE + 1;
    private static final int ICON_FRAME_TOP = 5;
    private static final int ICON_SIZE = 16;
    private static final int ICON_INSET = (ICON_FRAME_SIZE - ICON_SIZE) / 2;
    private static final int NAME_TOP = ICON_FRAME_TOP + ICON_FRAME_SIZE + 2;
    private static final float NAME_SCALE = 0.5F;
    private static final int NAME_COLOR = 0x343129;
    private static final int NETHERITE_NAME_COLOR = 0xBCB2AF;
    private static final int EFFECTS_GAP = 2;
    private static final int NAME_MAX_LINES = 2;
    private static final int EFFECTS_MAX_LINES = 3;
    private static final float TYPES_SCALE = 0.375F;
    private static final int TYPES_MAX_LINES = 1;
    private static final float HOVERED_SCALE = 1.05F;
    private static final float SHADOW_ALPHA = 0.05F;
    private static final int SHADOW_OFFSET = 2;

    private final FatestoneScreenPayload.SlotDisplay slot;
    private final int x;
    private final int y;
    private final List<CardButton> buttons = new ArrayList<>();

    public BoonCard(FatestoneScreenPayload payload, FatestoneMenu menu, int slotIndex, int x, int y,
                    boolean temptFateSlot) {
        this.slot = payload.slots().get(slotIndex);
        this.x = x;
        this.y = y;
        if (hasCard()) {
            List<FatestoneActionPayload.Action> actions = allowedActions(payload);
            for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
                FatestoneActionPayload.Action action = actions.get(actionIndex);
                buttons.add(CardButton.action(buttonX(), buttonY(actionIndex, actions.size()), slot.tier(),
                        Component.translatable(labelKey(action)),
                        () -> PacketDistributor.sendToServer(new FatestoneActionPayload(
                                payload.pos(), action, slotIndex, payload.revision()))));
            }
        } else if (temptFateSlot) {
            buttons.add(CardButton.temptFate(x + (WIDTH - CardButton.TEMPT_FATE_WIDTH) / 2,
                    y + TEMPT_FATE_BUTTON_TOP,
                    Component.translatable(labelKey(FatestoneActionPayload.Action.TEMPT_FATE)),
                    () -> PacketDistributor.sendToServer(new FatestoneActionPayload(
                            payload.pos(), FatestoneActionPayload.Action.TEMPT_FATE, slotIndex,
                            payload.revision())),
                    () -> canAffordTemptFate(payload.temptFateXpCost())
                            && hasRequiredOffering(payload, menu),
                    () -> canAffordTemptFate(payload.temptFateXpCost())
                            ? Component.translatable("mortal_boons.roll.requires_offering")
                            : Component.translatable("mortal_boons.roll.not_enough_xp",
                            payload.temptFateXpCost())));
        }
    }

    private static boolean canAffordTemptFate(int xpLevelCost) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && (player.getAbilities().instabuild || player.experienceLevel >= xpLevelCost);
    }

    private static boolean hasRequiredOffering(FatestoneScreenPayload payload, FatestoneMenu menu) {
        if (!payload.offeringRequired()) {
            return true;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getAbilities().instabuild) {
            return true;
        }
        return OfferingManager.matches(menu.getOfferingItem());
    }

    private static List<FatestoneActionPayload.Action> allowedActions(FatestoneScreenPayload payload) {
        List<FatestoneActionPayload.Action> actions = new ArrayList<>();
        if (payload.allowedActions().reroll()) {
            actions.add(FatestoneActionPayload.Action.REROLL);
        }
        if (payload.allowedActions().reforge()) {
            actions.add(FatestoneActionPayload.Action.REFORGE);
        }
        if (payload.allowedActions().forsake()) {
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

    public List<Component> tooltipLinesAt(double mouseX, double mouseY) {
        for (CardButton button : buttons) {
            if (!button.isEnabled() && button.isMouseOver(mouseX, mouseY)) {
                return List.of(button.disabledReason());
            }
        }
        if (hasCard() || buttons.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            lines.add(slot.title());
            if (!slot.types().getString().isEmpty()) {
                lines.add(slot.types());
            }
            lines.addAll(slot.lines());
            return lines;
        }
        return List.of();
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
        } else {
            renderIcon(guiGraphics);
            renderName(guiGraphics, font);
        }
        guiGraphics.pose().popPose();
    }

    private void renderName(GuiGraphics guiGraphics, Font font) {
        int textColor = slot.tier() == 4 ? NETHERITE_NAME_COLOR : NAME_COLOR;
        String upperCaseName = slot.name().getString().toUpperCase(Locale.ROOT);
        int textY = y + NAME_TOP;
        textY += drawCenteredScaled(guiGraphics, font,
                limited(font.split(Component.literal(upperCaseName), wrapWidth(NAME_SCALE)), NAME_MAX_LINES),
                NAME_SCALE, textY, textColor);
        if (showsTypes()) {
            textY += drawCenteredScaled(guiGraphics, font,
                    limited(font.split(slot.types(), wrapWidth(TYPES_SCALE)), TYPES_MAX_LINES),
                    TYPES_SCALE, textY, textColor);
        }
        List<FormattedCharSequence> effectLines = new ArrayList<>();
        for (Component effect : slot.effects()) {
            effectLines.addAll(font.split(effect, wrapWidth(NAME_SCALE)));
        }
        drawCenteredScaled(guiGraphics, font, limited(effectLines, EFFECTS_MAX_LINES),
                NAME_SCALE, textY + EFFECTS_GAP, textColor);
    }

    private boolean showsTypes() {
        return !slot.types().getString().isEmpty() && BoonClientConfig.SHOW_BOON_TYPES.get();
    }

    private static int wrapWidth(float scale) {
        return (int) (CONTENT_WIDTH / scale);
    }

    private int drawCenteredScaled(GuiGraphics guiGraphics, Font font, List<FormattedCharSequence> lines,
                                   float scale, int top, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + WIDTH / 2.0F, top, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            FormattedCharSequence line = lines.get(lineIndex);
            guiGraphics.drawString(font, line, -font.width(line) / 2, lineIndex * font.lineHeight, color, false);
        }
        guiGraphics.pose().popPose();
        return Math.round(lines.size() * font.lineHeight * scale);
    }

    private static List<FormattedCharSequence> limited(List<FormattedCharSequence> lines, int maxLines) {
        return lines.size() <= maxLines ? lines : lines.subList(0, maxLines);
    }

    private void renderIcon(GuiGraphics guiGraphics) {
        int frameX = x + (WIDTH - ICON_FRAME_SIZE) / 2;
        int frameY = y + ICON_FRAME_TOP;
        guiGraphics.blit(COMPONENTS, frameX, frameY, (slot.tier() - 1) * ICON_FRAME_STRIDE_U, ICON_FRAME_ROW_V,
                ICON_FRAME_SIZE, ICON_FRAME_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        slot.icon().ifPresent(iconTexture -> guiGraphics.blit(iconTexture,
                frameX + ICON_INSET, frameY + ICON_INSET, 0.0F, 0.0F,
                ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE));
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
