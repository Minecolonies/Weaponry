
package com.ldtteam.armory.weaponry.client.compatibility;

import com.ldtteam.armory.weaponry.util.References;
import com.ldtteam.armory.api.common.material.armor.IAddonArmorMaterial;
import com.ldtteam.armory.api.common.material.armor.ICoreArmorMaterial;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;

import java.util.Set;

/**
 * Created by Orion
 * Created on 01.06.2015
 * 12:26
 * <p/>
 * Copyrighted according to Project specific license
 */

@Mod.EventBusSubscriber(modid = References.General.MOD_ID, value = Side.CLIENT)
public class ClientArmoryCompatEventHandler
{

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void RegisterCoreMaterials(@NotNull final RegistryEvent.Register<ICoreArmorMaterial> coreArmorMaterialRegister)
    {
        coreArmorMaterialRegister.getRegistry().getValuesCollection()
          .stream()
          .filter(iCoreArmorMaterial -> iCoreArmorMaterial.getRegistryName().getResourceDomain().equals(References.General.MOD_ID))
          .forEach(iCoreArmorMaterial -> {
              final String materialIdentifier = iCoreArmorMaterial.getRegistryName().getResourcePath().replace("tcon-core-", "");
              final Material material = TinkerRegistry.getMaterial(materialIdentifier);

              iCoreArmorMaterial.setRenderInfo(new TConTextureController(material.renderInfo));
          });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void RegisterAddonMaterials(@NotNull final RegistryEvent.Register<IAddonArmorMaterial> addonArmorMaterialRegister)
    {
        addonArmorMaterialRegister.getRegistry().getValuesCollection()
          .stream()
          .filter(iAddonArmorMaterial -> iAddonArmorMaterial.getRegistryName().getResourceDomain().equals(References.General.MOD_ID))
          .forEach(iAddonArmorMaterial -> {
              final String materialIdentifier = iAddonArmorMaterial.getRegistryName().getResourcePath().replace("tcon-addon-", "");
              final Material material = TinkerRegistry.getMaterial(materialIdentifier);

              iAddonArmorMaterial.setRenderInfo(new TConTextureController(material.renderInfo));
          });
    }
}

