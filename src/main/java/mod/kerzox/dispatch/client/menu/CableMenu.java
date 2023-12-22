package mod.kerzox.dispatch.client.menu;

import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNetworkHandler;
import mod.kerzox.dispatch.common.capability.LevelNode;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CableMenu extends AbstractContainerMenu {

    private List<AbstractSubNetwork> subNetworks;
    private BlockPos pos;
    private Inventory playerInventory;
    private LevelNode node;

    private boolean refreshOps;

    public CableMenu(int windowId, Inventory inv, Player player, BlockPos pos, List<AbstractSubNetwork> subnetsFrom) {
        super(DispatchRegistry.Menus.CABLE_MENU.get(), windowId);
        this.pos = pos;
        this.subNetworks = subnetsFrom;
        this.playerInventory = inv;
        // do layout of inventory + hotbar
        layoutPlayerInventorySlots(8, 100);
        this.node = LevelNode.of(pos);
    }

    public void layoutPlayerInventorySlots(int leftCol, int topRow) {
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    public int addSlotRange(Container handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new Slot(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    public int addSlotBox(Container handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    public LevelNode getNode() {
        return node;
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return null;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return !LevelNetworkHandler.getHandler(p_38874_.level()).getSubnetsFrom(LevelNode.of(pos)).isEmpty();
    }

    public void refreshOperationList() {
        refreshOps = true;
    }

    public boolean isRefreshOps() {
        return refreshOps;
    }

    public void setRefreshOps(boolean refreshOps) {
        this.refreshOps = refreshOps;
    }
}
