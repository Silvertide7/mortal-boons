package net.silvertide.mortal_boons.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.network.AltarActionPayload;
import net.silvertide.mortal_boons.network.AltarScreenPayload;

import java.util.ArrayList;
import java.util.List;

public class AltarScreen extends Screen {
    private static final ResourceLocation BACKGROUND = MortalBoons.id("textures/gui/menu_altar.png");
    private static final ResourceLocation BACKGROUND_BEACON = MortalBoons.id("textures/gui/menu_altar_beacon.png");
    private static final ResourceLocation COMPONENTS = MortalBoons.id("textures/gui/menu_components.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int MENU_WIDTH = 232;
    private static final int MENU_HEIGHT = 246;
    private static final int MENU_CENTER_X = 116;
    private static final int CARD_WIDTH = 43;
    private static final int CARD_HEIGHT = 57;
    private static final int[] CARD_U_BY_TIER = {0, 45, 90, 135};
    private static final int CARD_V = 0;
    private static final int CARD_AREA_TOP = 75;
    private static final int CARD_AREA_HEIGHT = 67;
    private static final int CARD_ROW_LEFT = 45;
    private static final int CARD_GAP = 6;
    private static final int CARD_SLOT_SPACING = CARD_WIDTH + CARD_GAP;
    private static final int CARD_Y = CARD_AREA_TOP + (CARD_AREA_HEIGHT - CARD_HEIGHT) / 2;
    private static final float HOVERED_CARD_SCALE = 1.05F;
    private static final float CARD_SHADOW_ALPHA = 0.35F;
    private static final int CARD_SHADOW_OFFSET = 2;
    private static final int BUTTON_WIDTH = 66;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 5;
    private static final int BUTTON_TOP = 155;

    private final AltarScreenPayload data;
    private int menuLeft;
    private int menuTop;

    public AltarScreen(AltarScreenPayload data) {
        super(Component.translatable("mortal_boons.screen.title"));
        this.data = data;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        menuLeft = (width - MENU_WIDTH) / 2;
        menuTop = (height - MENU_HEIGHT) / 2;
        int buttonY = menuTop + BUTTON_TOP;
        int rowWidth = BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
        int rowStartX = menuLeft + MENU_CENTER_X - rowWidth / 2;
        addRenderableWidget(actionButton("mortal_boons.screen.button.roll", AltarActionPayload.Action.ROLL,
                rowStartX, buttonY, true));
        addRenderableWidget(actionButton("mortal_boons.screen.button.reforge", AltarActionPayload.Action.REFORGE,
                rowStartX + BUTTON_WIDTH + BUTTON_GAP, buttonY, data.altarPower() >= 2));
        addRenderableWidget(actionButton("mortal_boons.screen.button.reroll", AltarActionPayload.Action.REROLL,
                rowStartX + (BUTTON_WIDTH + BUTTON_GAP) * 2, buttonY, data.altarPower() >= 3));
    }

    private Button actionButton(String translationKey, AltarActionPayload.Action action, int x, int y,
                                boolean enabled) {
        Button button = Button.builder(Component.translatable(translationKey),
                        pressed -> PacketDistributor.sendToServer(new AltarActionPayload(data.altarPos(), action)))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        button.active = enabled;
        return button;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        ResourceLocation background = data.beaconBelow() ? BACKGROUND_BEACON : BACKGROUND;
        guiGraphics.blit(background, menuLeft, menuTop, 0.0F, 0.0F, MENU_WIDTH, MENU_HEIGHT,
                TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int hoveredIndex = -1;
        for (int slotIndex = 0; slotIndex < data.slots().size(); slotIndex++) {
            if (isMouseOverCard(mouseX, mouseY, cardX(slotIndex), menuTop + CARD_Y)) {
                hoveredIndex = slotIndex;
            } else {
                drawCard(guiGraphics, slotIndex, false);
            }
        }
        if (hoveredIndex >= 0) {
            drawCard(guiGraphics, hoveredIndex, true);
            guiGraphics.renderComponentTooltip(font, tooltipLines(data.slots().get(hoveredIndex)), mouseX, mouseY);
        }
    }

    private int cardX(int slotIndex) {
        return menuLeft + CARD_ROW_LEFT + slotIndex * CARD_SLOT_SPACING;
    }

    private void drawCard(GuiGraphics guiGraphics, int slotIndex, boolean hovered) {
        int tier = data.slots().get(slotIndex).tier();
        if (tier < 1 || tier > 4) {
            return;
        }
        int cardX = cardX(slotIndex);
        int cardY = menuTop + CARD_Y;
        guiGraphics.pose().pushPose();
        if (hovered) {
            float centerX = cardX + CARD_WIDTH / 2.0F;
            float centerY = cardY + CARD_HEIGHT / 2.0F;
            guiGraphics.pose().translate(centerX, centerY, 0);
            guiGraphics.pose().scale(HOVERED_CARD_SCALE, HOVERED_CARD_SCALE, 1.0F);
            guiGraphics.pose().translate(-centerX, -centerY, 0);
        }
        guiGraphics.setColor(0.0F, 0.0F, 0.0F, CARD_SHADOW_ALPHA);
        guiGraphics.blit(COMPONENTS, cardX + CARD_SHADOW_OFFSET, cardY + CARD_SHADOW_OFFSET,
                CARD_U_BY_TIER[tier - 1], CARD_V, CARD_WIDTH, CARD_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(COMPONENTS, cardX, cardY, CARD_U_BY_TIER[tier - 1], CARD_V,
                CARD_WIDTH, CARD_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.pose().popPose();
    }

    private static boolean isMouseOverCard(int mouseX, int mouseY, int cardX, int cardY) {
        return mouseX >= cardX && mouseX < cardX + CARD_WIDTH && mouseY >= cardY && mouseY < cardY + CARD_HEIGHT;
    }

    private static List<Component> tooltipLines(AltarScreenPayload.SlotDisplay slot) {
        List<Component> lines = new ArrayList<>();
        lines.add(slot.title());
        lines.addAll(slot.lines());
        return lines;
    }
}
