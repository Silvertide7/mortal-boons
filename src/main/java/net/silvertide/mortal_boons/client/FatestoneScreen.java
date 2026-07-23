package net.silvertide.mortal_boons.client;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.silvertide.mortal_boons.MortalBoons;
import net.silvertide.mortal_boons.boon.OfferingManager;
import net.silvertide.mortal_boons.menu.FatestoneMenu;
import net.silvertide.mortal_boons.network.FatestoneScreenPayload;

import java.util.ArrayList;
import java.util.List;

public class FatestoneScreen extends AbstractContainerScreen<FatestoneMenu> {
    private static final ResourceLocation BACKGROUND = MortalBoons.id("textures/gui/menu_altar.png");
    private static final ResourceLocation BACKGROUND_BEACON = MortalBoons.id("textures/gui/menu_altar_beacon.png");
    private static final ResourceLocation COMPONENTS = MortalBoons.id("textures/gui/menu_components.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int MENU_WIDTH = 232;
    private static final int MENU_HEIGHT = 246;
    private static final int OFFERING_SPRITE_U = 0;
    private static final int OFFERING_SPRITE_V = 167;
    private static final int OFFERING_SPRITE_WIDTH = 20;
    private static final int OFFERING_SPRITE_HEIGHT = 21;
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

    private FatestoneScreenPayload data;
    private final List<BoonCard> cards = new ArrayList<>();

    public FatestoneScreen(FatestoneMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.data = menu.getSnapshot();
        imageWidth = MENU_WIDTH;
        imageHeight = MENU_HEIGHT;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        rebuildCards();
    }

    public void updateData(FatestoneScreenPayload payload) {
        data = payload;
        menu.updateFromSnapshot(payload);
        rebuildCards();
    }

    private void rebuildCards() {
        cards.clear();
        int temptFateIndex = -1;
        for (int slotIndex = 0; slotIndex < data.slots().size(); slotIndex++) {
            if (data.slots().get(slotIndex).tier() == 0 && slotIndex < data.power()) {
                temptFateIndex = slotIndex;
                break;
            }
        }
        for (int slotIndex = 0; slotIndex < data.slots().size(); slotIndex++) {
            cards.add(new BoonCard(data, menu, slotIndex,
                    leftPos + FatestoneMenu.cardColumnX(FatestoneMenu.displayColumn(slotIndex)),
                    topPos + FatestoneMenu.CARD_Y, slotIndex == temptFateIndex));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation background = data.beaconBelow() ? BACKGROUND_BEACON : BACKGROUND;
        guiGraphics.blit(background, leftPos, topPos, 0.0F, 0.0F, MENU_WIDTH, MENU_HEIGHT,
                TEXTURE_SIZE, TEXTURE_SIZE);
        int offeringColumn = menu.getActiveOfferingColumn();
        if (offeringColumn >= 0) {
            guiGraphics.blit(COMPONENTS,
                    leftPos + FatestoneMenu.cardColumnX(offeringColumn) + FatestoneMenu.OFFERING_SPRITE_X_ON_CARD,
                    topPos + FatestoneMenu.OFFERING_SPRITE_Y,
                    OFFERING_SPRITE_U, OFFERING_SPRITE_V,
                    OFFERING_SPRITE_WIDTH, OFFERING_SPRITE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> tooltip = super.getTooltipFromContainerItem(stack);
        if (hoveredSlot != null && hoveredSlot.container == menu.getOfferingContainer()) {
            OfferingManager.matching(stack).ifPresent(offering -> tooltip.addAll(offering.describe()));
        }
        return tooltip;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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
            List<Component> tooltipLines = hoveredCard.tooltipLinesAt(mouseX, mouseY);
            if (!tooltipLines.isEmpty()) {
                guiGraphics.renderComponentTooltip(font, tooltipLines, mouseX, mouseY);
            }
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
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
        guiGraphics.blit(COMPONENTS, leftPos + menuX, topPos + BIG_CANDLE_Y,
                frame * (BIG_FLAME_WIDTH + 1), BIG_FLAME_ROW_V,
                BIG_FLAME_WIDTH, BIG_FLAME_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private void drawSmallFlame(GuiGraphics guiGraphics, int menuX, long animationTick, int candleSeed) {
        int frame = randomizedFrame(animationTick, candleSeed, SMALL_FLAME_FRAME_COUNT);
        guiGraphics.blit(COMPONENTS, leftPos + menuX, topPos + SMALL_CANDLE_Y,
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
