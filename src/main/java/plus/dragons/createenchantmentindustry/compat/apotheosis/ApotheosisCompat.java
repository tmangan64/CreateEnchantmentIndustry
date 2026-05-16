package plus.dragons.createenchantmentindustry.compat.apotheosis;

import net.neoforged.fml.ModList;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.Enchanting;

public class ApotheosisCompat {
    // TODO: Re-implement potion mixing recipes for Create 6.0
    // The PotionMixingRecipes API has changed significantly in Create 6.0
    // PotionFluidHandler.BottleType, ProcessingRecipeBuilder, and addRecipe() all changed
    public static void addPotionMixingRecipes() {
        // Disabled until Create 6.0 API is properly integrated
    }

    public static void banTomeFromEnchanter(){
        if(ModList.get().isLoaded("apotheosis")){
            Enchanting.UNENCHANTABLE_CONDITIONS.add((itemStack)->{
                var id = itemStack.getItem().toString();
                return id.startsWith("apotheosis:") && id.contains("tome");
            });
        }
    }

}
