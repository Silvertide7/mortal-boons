package net.silvertide.mortal_boons.client;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.silvertide.mortal_boons.MortalBoons;
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
    private static final int CARD_ROW_LEFT = 44;
    private static final int CARD_GAP = 5;
    private static final int CARD_SLOT_SPACING = BoonCard.WIDTH + CARD_GAP;
    private static final int CARD_AREA_TOP = 75;
    private static final int CARD_AREA_HEIGHT = 67;
    private static final int CARD_Y = CARD_AREA_TOP + (CARD_AREA_HEIGHT - BoonCard.HEIGHT) / 2 - 1;
    private static final int FLAME_FRAME_TIME_MS = 150;
    private static final int BIG_FLAME_ROW_V = 130;
    private static final int BIG_FLAME_WIDTH = 6;
    private static final int BIG_FLAME_HEIGHT = 10;
    private static final int BIG_FLAME_FRAME_COUNT = 8;
    private static final int LEFT_BIG_CANDLE_X = 32;
    private static final int RIGHT_BIG_CANDLE_X = 193;
    private static final int BIG_CANDLE_Y = 39;
    private static final int LEFT_BIG_CANDLE_FRAME_OFFSET = 1;
    private static final int RIGHT_BIG_CANDLE_FRAME_OFFSET = 4;
    private static final int SMALL_FLAME_ROW_V = 140;
    private static final int SMALL_FLAME_WIDTH = 4;
    private static final int SMALL_FLAME_HEIGHT = 8;
    private static final int SMALL_FLAME_FRAME_COUNT = 8;
    private static final int LEFT_SMALL_CANDLE_X = 17;
    private static final int RIGHT_SMALL_CANDLE_X = 210;
    private static final int SMALL_CANDLE_Y = 171;
    private static final int LEFT_SMALL_CANDLE_FRAME_OFFSET = 2;
    private static final int RIGHT_SMALL_CANDLE_FRAME_OFFSET = 6;

    private final AltarScreenPayload data;
    private final List<BoonCard> cards = new ArrayList<>();
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
        cards.clear();
        for (int slotIndex = 0; slotIndex < data.slots().size(); slotIndex++) {
            cards.add(new BoonCard(data, slotIndex,
                    menuLeft + CARD_ROW_LEFT + slotIndex * CARD_SLOT_SPACING, menuTop + CARD_Y));
        }
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
        drawCandleFlames(guiGraphics);
        int hoveredIndex = -1;
        for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
            if (cards.get(cardIndex).isHovered(mouseX, mouseY)) {
                hoveredIndex = cardIndex;
            } else {
                cards.get(cardIndex).render(guiGraphics, font, mouseX, mouseY, false);
            }
        }
        if (hoveredIndex >= 0) {
            BoonCard hoveredCard = cards.get(hoveredIndex);
            hoveredCard.render(guiGraphics, font, mouseX, mouseY, true);
            guiGraphics.renderComponentTooltip(font, hoveredCard.tooltipLines(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (BoonCard card : cards) {
                if (card.isHovered(mouseX, mouseY) && card.mouseClicked(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean handled = false;
            for (BoonCard card : cards) {
                handled |= card.mouseReleased(mouseX, mouseY);
            }
            if (handled) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void drawCandleFlames(GuiGraphics guiGraphics) {
        if (!data.candlesLit()) {
            return;
        }
        long animationTick = Util.getMillis() / FLAME_FRAME_TIME_MS;
        drawBigFlame(guiGraphics, LEFT_BIG_CANDLE_X, animationTick, LEFT_BIG_CANDLE_FRAME_OFFSET);
        drawBigFlame(guiGraphics, RIGHT_BIG_CANDLE_X, animationTick, RIGHT_BIG_CANDLE_FRAME_OFFSET);
        drawSmallFlame(guiGraphics, LEFT_SMALL_CANDLE_X, animationTick, LEFT_SMALL_CANDLE_FRAME_OFFSET);
        drawSmallFlame(guiGraphics, RIGHT_SMALL_CANDLE_X, animationTick, RIGHT_SMALL_CANDLE_FRAME_OFFSET);
    }

    private void drawBigFlame(GuiGraphics guiGraphics, int menuX, long animationTick, int candleSeed) {
        int frame = randomizedFrame(animationTick, candleSeed, BIG_FLAME_FRAME_COUNT);
        guiGraphics.blit(COMPONENTS, menuLeft + menuX, menuTop + BIG_CANDLE_Y,
                frame * (BIG_FLAME_WIDTH + 1), BIG_FLAME_ROW_V,
                BIG_FLAME_WIDTH, BIG_FLAME_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawSmallFlame(GuiGraphics guiGraphics, int menuX, long animationTick, int candleSeed) {
        int frame = randomizedFrame(animationTick, candleSeed, SMALL_FLAME_FRAME_COUNT);
        guiGraphics.blit(COMPONENTS, menuLeft + menuX, menuTop + SMALL_CANDLE_Y,
                frame * (SMALL_FLAME_WIDTH + 1), SMALL_FLAME_ROW_V,
                SMALL_FLAME_WIDTH, SMALL_FLAME_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static int randomizedFrame(long animationTick, int candleSeed, int frameCount) {
        int frame = scrambledFrame(animationTick, candleSeed, frameCount);
        int previousFrame = scrambledFrame(animationTick - 1, candleSeed, frameCount);
        return frame == previousFrame ? (frame + 1) % frameCount : frame;
    }

    private static int scrambledFrame(long animationTick, int candleSeed, int frameCount) {
        long mixed = animationTick * 6364136223846793005L + candleSeed * 1442695040888963407L;
        mixed ^= mixed >>> 33;
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= mixed >>> 33;
        return (int) Math.floorMod(mixed, frameCount);
    }
}
