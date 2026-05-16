package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

public class DisenchantRecipe extends StandardProcessingRecipe<RecipeWrapper> {

    private final int experience;

    public DisenchantRecipe(ProcessingRecipeParams params) {
        super(CeiRecipeTypes.DISENCHANTING, params);
        // Validate during construction - recipe ID not available here in 1.21+
        if (getFluidResults().isEmpty()) {
            this.experience = 0;
        } else {
            FluidStack fluid = getFluidResults().get(0);
            this.experience = fluid.getAmount();
        }
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level pLevel) {
        return getIngredients().get(0).test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return false;
    }

    public boolean hasNoResult() {
        return getRollableResults().isEmpty();
    }

    public int getExperience() {
        return experience;
    }
}
