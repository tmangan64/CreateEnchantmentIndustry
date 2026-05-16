package plus.dragons.createenchantmentindustry.foundation.mixin.accessor;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Interface for accessing the experience fluid extractor from furnace block entities.
 * This is used for NeoForge capability registration.
 */
public interface FurnaceExpExtractorAccess {
    IFluidHandler createEnchantmentIndustry$getExpExtractor();
}
