package net.silvertide.mortal_boons.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.silvertide.mortal_boons.block.FatestoneBlocks;
import net.silvertide.mortal_boons.boon.OfferingManager;
import net.silvertide.mortal_boons.network.FatestoneScreenPayload;

public class FatestoneMenu extends AbstractContainerMenu {
    public static final int CARD_ROW_LEFT = 45;
    public static final int CARD_SLOT_SPACING = 49;
    public static final int CARD_Y = 78;
    public static final int OFFERING_SPRITE_X_ON_CARD = 12;
    public static final int OFFERING_SPRITE_Y = CARD_Y + 8;

    private static final int INVENTORY_LEFT = 36;
    private static final int INVENTORY_TOP = 150;
    private static final int HOTBAR_TOP = 213;
    private static final int SLOT_PITCH = 18;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int MAIN_INVENTORY_SLOT_COUNT = INVENTORY_ROWS * INVENTORY_COLUMNS;
    private static final int PLAYER_SLOT_COUNT = MAIN_INVENTORY_SLOT_COUNT + INVENTORY_COLUMNS;
    private static final int OFFERING_SLOT_INDEX = PLAYER_SLOT_COUNT;
    private static final int CARD_COLUMN_COUNT = 3;
    private static final int OFFERING_SLOT_INSET = 2;

    private final FatestoneScreenPayload snapshot;
    private final ContainerLevelAccess access;
    private final SimpleContainer offeringContainer = new SimpleContainer(1);
    private boolean offeringSlotVisible;
    private int activeOfferingColumn = -1;
    private int revision;

    public FatestoneMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, FatestoneScreenPayload.STREAM_CODEC.decode(buf),
                ContainerLevelAccess.NULL);
    }

    public FatestoneMenu(int containerId, Inventory playerInventory, FatestoneScreenPayload snapshot,
                         ContainerLevelAccess access) {
        super(FatestoneMenus.FATESTONE_MENU.get(), containerId);
        this.snapshot = snapshot;
        this.access = access;
        for (int row = 0; row < INVENTORY_ROWS; row++) {
            for (int column = 0; column < INVENTORY_COLUMNS; column++) {
                addSlot(new Slot(playerInventory, INVENTORY_COLUMNS + row * INVENTORY_COLUMNS + column,
                        INVENTORY_LEFT + column * SLOT_PITCH, INVENTORY_TOP + row * SLOT_PITCH));
            }
        }
        for (int column = 0; column < INVENTORY_COLUMNS; column++) {
            addSlot(new Slot(playerInventory, column, INVENTORY_LEFT + column * SLOT_PITCH, HOTBAR_TOP));
        }
        for (int cardColumn = 0; cardColumn < CARD_COLUMN_COUNT; cardColumn++) {
            addSlot(new OfferingSlot(cardColumn));
        }
        updateFromSnapshot(snapshot);
    }

    public FatestoneScreenPayload getSnapshot() {
        return snapshot;
    }

    public int getRevision() {
        return revision;
    }

    public int bumpRevision() {
        return ++revision;
    }

    public ItemStack getOfferingItem() {
        return offeringContainer.getItem(0);
    }

    public SimpleContainer getOfferingContainer() {
        return offeringContainer;
    }

    public int getActiveOfferingColumn() {
        return offeringSlotVisible ? activeOfferingColumn : -1;
    }

    public void updateFromSnapshot(FatestoneScreenPayload payload) {
        offeringSlotVisible = payload.offeringsEnabled();
        activeOfferingColumn = -1;
        for (int slotIndex = 0; slotIndex < payload.slots().size(); slotIndex++) {
            if (payload.slots().get(slotIndex).tier() == 0 && slotIndex < payload.power()) {
                activeOfferingColumn = displayColumn(slotIndex);
                break;
            }
        }
    }

    public static int displayColumn(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> 1;
            case 1 -> 0;
            default -> 2;
        };
    }

    public static int cardColumnX(int column) {
        return CARD_ROW_LEFT + column * CARD_SLOT_SPACING;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, FatestoneBlocks.FATESTONE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, offeringContainer));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (slotIndex >= OFFERING_SLOT_INDEX) {
            moved = moveItemStackTo(stack, 0, PLAYER_SLOT_COUNT, false);
        } else {
            moved = getActiveOfferingColumn() >= 0 && OfferingManager.matches(stack)
                    && moveItemStackTo(stack, OFFERING_SLOT_INDEX, OFFERING_SLOT_INDEX + 1, false);
            if (!moved) {
                moved = slotIndex < MAIN_INVENTORY_SLOT_COUNT
                        ? moveItemStackTo(stack, MAIN_INVENTORY_SLOT_COUNT, PLAYER_SLOT_COUNT, false)
                        : moveItemStackTo(stack, 0, MAIN_INVENTORY_SLOT_COUNT, false);
            }
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private class OfferingSlot extends Slot {
        private final int cardColumn;

        OfferingSlot(int cardColumn) {
            super(offeringContainer, 0,
                    cardColumnX(cardColumn) + OFFERING_SPRITE_X_ON_CARD + OFFERING_SLOT_INSET,
                    OFFERING_SPRITE_Y + OFFERING_SLOT_INSET);
            this.cardColumn = cardColumn;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return OfferingManager.matches(stack);
        }

        @Override
        public boolean isActive() {
            return getActiveOfferingColumn() == cardColumn;
        }
    }
}
