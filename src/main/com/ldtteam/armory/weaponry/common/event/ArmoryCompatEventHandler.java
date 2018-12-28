package com.ldtteam.armory.weaponry.common.event;

import com.ldtteam.armory.weaponry.Weaponry;
import com.ldtteam.armory.weaponry.common.config.WeaponryConfigs;
import com.ldtteam.armory.weaponry.util.References;
import com.ldtteam.armory.api.IArmoryAPI;
import com.ldtteam.armory.api.common.capability.armor.IArmorAbsorptionRatioCapability;
import com.ldtteam.armory.api.common.capability.armor.IArmorDefenceCapability;
import com.ldtteam.armory.api.common.capability.armor.IArmorDurabilityCapability;
import com.ldtteam.armory.api.common.capability.armor.IArmorToughnessCapability;
import com.ldtteam.armory.api.common.helpers.IMaterialConstructionHelper;
import com.ldtteam.armory.api.common.material.armor.IAddonArmorMaterial;
import com.ldtteam.armory.api.common.material.armor.ICoreArmorMaterial;
import com.ldtteam.armory.api.util.references.ModCapabilities;
import com.ldtteam.smithscore.util.common.helper.ItemStackHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.events.TinkerRegisterEvent;
import slimeknights.tconstruct.library.materials.Material;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ldtteam.armory.api.util.references.ModData.Materials.Iron.*;


/**
 * Created by Orion
 * Created on 01.06.2015
 * 11:07
 * <p/>
 * Copyrighted according to Project specific license
 */

@Mod.EventBusSubscriber(modid = References.General.MOD_ID)
public class ArmoryCompatEventHandler
{

    private static final double LOG9_2 = 0.31546487678;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void RegisterCoreMaterials(@NotNull final RegistryEvent.Register<ICoreArmorMaterial> coreArmorMaterialRegister)
    {
        final long addedMaterialCount = TinkerRegistry.getAllMaterials()
          .stream()
          .map(tuple -> checkIfRegisterIsNeededForRecipeCore(tuple, coreArmorMaterialRegister.getRegistry()))
          .filter(Boolean::booleanValue)
          .count();
        
        Weaponry.logger.info(String.format("Added: %d materials to Armories core material registry.", addedMaterialCount));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void RegisterAddonMaterials(@NotNull final RegistryEvent.Register<IAddonArmorMaterial> addonArmorMaterialRegister)
    {
        final long addedMaterialCount = TinkerRegistry.getAllMaterials()
                                          .stream()
                                          .map(tuple -> checkIfRegisterIsNeededForRecipeAddon(tuple, addonArmorMaterialRegister.getRegistry()))
                                          .filter(Boolean::booleanValue)
                                          .count();

        Weaponry.logger.info(String.format("Added: %d materials to Armories addon material registry.", addedMaterialCount));
    }

    //Copy of the melting temp calculator of TCon.
    private static int calcTemperature(int temp) {
        int base = Material.VALUE_Block;
        int max_tmp = Math.max(0, temp - 300); // we use 0 as baseline, not 300
        double f = (double) 144 / (double) base;

        // we calculate 2^log9(f), which effectively gives us 2^(1 for each multiple of 9)
        // so 1 = 1, 9 = 2, 81 = 4, 1/9 = 1/2, 1/81 = 1/4 etc
        // we simplify it to f^log9(2) to make calculation simpler
        f = Math.pow(f, LOG9_2);

        return 300 + (int) (f * (double) max_tmp);
    }
    
    private static boolean checkIfRegisterIsNeededForRecipeCore(@NotNull final Material tconMaterial, @NotNull final IForgeRegistry<ICoreArmorMaterial> coreMaterialRegistry)
    {
        final IMaterialConstructionHelper helper = IArmoryAPI.Holder.getInstance().getHelpers().getMaterialConstructionHelper();

        if (coreMaterialRegistry.getValuesCollection().stream().anyMatch(material -> material.getOreDictionaryIdentifier().equalsIgnoreCase(tconMaterial.identifier)))
        {
            return false;
        }

        if (tconMaterial.getFluid() == null)
        {
            return false;
        }

        Weaponry.logger.info(String.format("Found metal ingot in TiC LiquidCasting: %s - Inserting it into Armory core material registry if possible!",
          tconMaterial.identifier));
    
        final float meltingPoint = Math.round(WeaponryConfigs.meltingPointOffset * (calcTemperature(tconMaterial.getFluid().getTemperature()) - 300));
        final float relevantToIronCoefficient = IRON_MELTINGPOINT / meltingPoint;

        final ICoreArmorMaterial material = helper.createMedievalCoreArmorMaterial(
          tconMaterial.getLocalizedName(),
          tconMaterial.getTextColor(),
          tconMaterial.identifier,
          meltingPoint,
          IRON_VAPORIZINGPOINT * relevantToIronCoefficient,
          (int) (IRON_MELTINGTIME * relevantToIronCoefficient),
          (int) (IRON_VAPORIZINGTIME * relevantToIronCoefficient),
          IRON_HEATCOEFFICIENT * relevantToIronCoefficient,
          builder ->
            builder
              .register(ModCapabilities.MOD_ARMOR_DURABILITY_CAPABILITY, IArmorDurabilityCapability.multiply(relevantToIronCoefficient))
              .register(ModCapabilities.MOD_ARMOR_ABSORPTION_RATIO_CAPABILITY, IArmorAbsorptionRatioCapability.multiply(relevantToIronCoefficient))
              .register(ModCapabilities.MOD_ARMOR_DEFENCE_CAPABILITY, IArmorDefenceCapability.multiply(relevantToIronCoefficient))
              .register(ModCapabilities.MOD_ARMOR_THOUGHNESS_CAPABILITY, IArmorToughnessCapability.multiply(relevantToIronCoefficient))
        ).setRegistryName(new ResourceLocation(References.General.MOD_ID, "tcon-core-" + tconMaterial.getIdentifier()));

        coreMaterialRegistry.register(material);

        return true;
    }

    private static boolean checkIfRegisterIsNeededForRecipeAddon(@NotNull final Material tconMaterial, @NotNull final IForgeRegistry<IAddonArmorMaterial> addonMaterialRegistry)
    {

        final IMaterialConstructionHelper helper = IArmoryAPI.Holder.getInstance().getHelpers().getMaterialConstructionHelper();

        if (addonMaterialRegistry.getValuesCollection().stream().anyMatch(material -> material.getOreDictionaryIdentifier().equalsIgnoreCase(tconMaterial.identifier)))
        {
            return false;
        }

        if (tconMaterial.getFluid() == null)
        {
            return false;
        }

        Weaponry.logger.info(String.format("Found metal ingot in TiC LiquidCasting: %s - Inserting it into Armory addon material registry if possible!",
          tconMaterial.identifier));

        final float meltingPoint = WeaponryConfigs.meltingPointOffset * calcTemperature(tconMaterial.getFluid().getTemperature());
        final float relevantToIronCoefficient = IRON_MELTINGPOINT / meltingPoint;

        final IAddonArmorMaterial material = helper.createMedievalAddonArmorMaterial(
          tconMaterial.getLocalizedName(),
          tconMaterial.getTextColor(),
          tconMaterial.identifier,
          meltingPoint,
          IRON_VAPORIZINGPOINT * relevantToIronCoefficient,
          (int) (IRON_MELTINGTIME * relevantToIronCoefficient),
          (int) (IRON_VAPORIZINGTIME * relevantToIronCoefficient),
          IRON_HEATCOEFFICIENT * relevantToIronCoefficient,
          builder ->
            builder
              .register(ModCapabilities.MOD_ARMOR_DURABILITY_CAPABILITY, IArmorDurabilityCapability.multiply(relevantToIronCoefficient))
              .register(ModCapabilities.MOD_ARMOR_ABSORPTION_RATIO_CAPABILITY, IArmorAbsorptionRatioCapability.multiply(relevantToIronCoefficient))
              .register(ModCapabilities.MOD_ARMOR_DEFENCE_CAPABILITY, IArmorDefenceCapability.multiply(relevantToIronCoefficient))
              .register(ModCapabilities.MOD_ARMOR_THOUGHNESS_CAPABILITY, IArmorToughnessCapability.multiply(relevantToIronCoefficient))
        ).setRegistryName(new ResourceLocation(References.General.MOD_ID, "tcon-addon-" + tconMaterial.getIdentifier()));

        addonMaterialRegistry.register(material);

        return true;
    }
}

