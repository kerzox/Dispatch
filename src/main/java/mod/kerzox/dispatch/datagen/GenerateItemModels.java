package mod.kerzox.dispatch.datagen;

import mod.kerzox.dispatch.Dispatch;
import mod.kerzox.dispatch.common.item.DispatchItem;
import mod.kerzox.dispatch.common.network.PacketHandler;
import mod.kerzox.dispatch.registry.DispatchRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class GenerateItemModels extends ItemModelProvider {

    public GenerateItemModels(PackOutput generator, ExistingFileHelper existingFileHelper) {
        super(generator, Dispatch.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        System.out.println("wdasdsad");
        for (Map<DispatchItem.Tiers, RegistryObject<Item>> map : DispatchRegistry.Items.DISPATCH_CABLES.values()) {
            map.forEach((tiers, itemRegistryObject) -> {
                withExistingParent(tiers.getSerializedName()+"_"+itemRegistryObject.getId(), itemRegistryObject.getId());
            });
        }
    }

}
