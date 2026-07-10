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

public class AltarScreen extends Screen {
    private static final ResourceLocation BACKGROUND = MortalBoons.id("textures/gui/altar_screen.png");
    private static final int BACKGROUND_TEXTURE_SIZE = 512;
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 240;
    private static final int PANEL_PADDING_TOP = 16;
    private static final int TITLE_COLOR = 0xFFD75F;
    private static final int SLOT_TITLE_COLOR = 0xFFFFFF;
    private static final int LINE_COLOR = 0xA0A0A0;
    private static final int LINE_HEIGHT = 10;
    private static final int SLOT_TITLE_HEIGHT = 14;
    private static final int SLOT_GAP = 8;
    private static final int BUTTON_WIDTH = 66;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 5;
    private static final int BUTTON_BOTTOM_MARGIN = 14;

    private final AltarScreenPayload data;
    private int panelLeft;
    private int panelTop;

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
        panelLeft = (width - PANEL_WIDTH) / 2;
        panelTop = (height - PANEL_HEIGHT) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - BUTTON_HEIGHT - BUTTON_BOTTOM_MARGIN;
        int rowWidth = BUTTON_WIDTH * 3 + BUTTON_GAP * 2;
        int rowStartX = panelLeft + (PANEL_WIDTH - rowWidth) / 2;
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
        guiGraphics.blit(BACKGROUND, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT,
                0.0F, 0.0F, BACKGROUND_TEXTURE_SIZE, BACKGROUND_TEXTURE_SIZE,
                BACKGROUND_TEXTURE_SIZE, BACKGROUND_TEXTURE_SIZE);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int centerX = width / 2;
        int y = panelTop + PANEL_PADDING_TOP;
        guiGraphics.drawCenteredString(font, title, centerX, y, TITLE_COLOR);
        y += SLOT_TITLE_HEIGHT;
        guiGraphics.drawCenteredString(font,
                Component.translatable("mortal_boons.screen.power", data.altarPower()), centerX, y, LINE_COLOR);
        y += SLOT_TITLE_HEIGHT + SLOT_GAP;
        for (AltarScreenPayload.SlotDisplay slot : data.slots()) {
            guiGraphics.drawCenteredString(font, slot.title(), centerX, y, SLOT_TITLE_COLOR);
            y += SLOT_TITLE_HEIGHT;
            for (Component line : slot.lines()) {
                guiGraphics.drawCenteredString(font, line, centerX, y, LINE_COLOR);
                y += LINE_HEIGHT;
            }
            y += SLOT_GAP;
        }
    }
}
