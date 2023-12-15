package mod.kerzox.dispatch.client.component;

import mod.kerzox.dispatch.client.gui.ICustomScreen;
import mod.kerzox.dispatch.common.capability.AbstractSubNetwork;
import mod.kerzox.dispatch.common.capability.LevelNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityTabButton extends ToggleButtonComponent {

    private Map<Capability<?>, int[]> textureCoords = new HashMap<>()
    {{
        put(ForgeCapabilities.ENERGY, new int[]{0, 82, 0, 97});
        put(ForgeCapabilities.FLUID_HANDLER, new int[]{16, 82, 16, 97});
        put(ForgeCapabilities.ITEM_HANDLER, new int[]{32, 82, 32, 97});
    }};

    private AbstractSubNetwork subNetwork;

    public CapabilityTabButton(ICustomScreen screen, ResourceLocation texture, int x, int y, int width, int height, LevelNode node, AbstractSubNetwork subNetwork, IPressable btn) {
        super(screen, texture, x, y, width, height, 0, 82, 0, 97,
                Component.literal(subNetwork.getCapability().getName()), btn);
        int[] coords = textureCoords.get(subNetwork.getCapability());
        this.u1 = coords[0];
        this.v1 = coords[1];
        this.u2 = coords[2];
        this.v2 = coords[3];
        this.subNetwork = subNetwork;
    }

    public AbstractSubNetwork getSubNetwork() {
        return subNetwork;
    }

    @Override
    protected List<Component> getComponents() {
        if (ForgeCapabilities.ENERGY == subNetwork.getCapability()) {
            return List.of(
                    Component.literal("Energy Config tab")
            );
        }
        if (ForgeCapabilities.ITEM_HANDLER == subNetwork.getCapability()) {
            return List.of(
                    Component.literal("Item Config tab")
            );
        }
        if (ForgeCapabilities.FLUID_HANDLER == subNetwork.getCapability()) {
            return List.of(
                    Component.literal("Fluid Config tab")
            );
        }
        return new ArrayList<>();
    }
}
