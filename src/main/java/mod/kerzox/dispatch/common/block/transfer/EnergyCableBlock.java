package mod.kerzox.dispatch.common.block.transfer;

import mod.kerzox.dispatch.common.block.DefaultPipeBlock;
import mod.kerzox.dispatch.common.entity.EnergyCable;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import static mod.kerzox.dispatch.registry.DispatchRegistry.BlockEntities.ENERGY_CABLE;

public class EnergyCableBlock extends DefaultPipeBlock<EnergyCable> {

    public EnergyCableBlock(Properties properties) {
        super(ENERGY_CABLE.getType(), properties);
    }

    public static class Item extends BlockItem {
        public Item(Properties pProperties) {
            super(DispatchRegistry.Blocks.ENERGY_CABLE_BLOCK.get(), pProperties);
        }
    }
}
