package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

import javax.annotation.Nullable;

public class Disenchanting {

    private static final ItemStackHandler HANDLER = new ItemStackHandler(1);
    private static final RecipeWrapper WRAPPER = new RecipeWrapper(HANDLER);

    public static ItemStack disenchantAndInsert(DisenchanterBlockEntity be, ItemStack itemStack, boolean simulate) {
        Level level = be.getLevel();
        if (level == null)
            return itemStack;
        HANDLER.setStackInSlot(0, itemStack);
        return CeiRecipeTypes.DISENCHANTING.<RecipeWrapper, DisenchantRecipe>find(WRAPPER, be.getLevel())
                .map(recipe -> {
                    if (!recipe.hasNoResult())
                        return itemStack;
                    var tank = be.getInternalTank();
                    tank.allowInsertion();
                    int amount = recipe.getExperience();
                    var fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), itemStack.getCount() * amount);
                    int inserted = tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) / amount;
                    ItemStack ret = itemStack.copy();
                    if (!simulate) {
                        fluidStack = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), inserted * amount);
                        tank.getPrimaryHandler().fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                    }
                    ret.shrink(inserted);
                    tank.forbidInsertion();
                    return ret;
                }).orElse(itemStack);
    }

    // Produce result only. Do not modify stack.
    // stack always has count of 1.
    @Nullable
    public static Pair<FluidStack, ItemStack> disenchantResult(ItemStack itemStack, Level level) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        boolean hasNonCurse = enchantments.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().is(EnchantmentTags.CURSE));

        if (hasNonCurse) {
            var xp = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), getDisenchantExperience(itemStack));
            ItemStack result = disenchant(itemStack);
            return Pair.of(xp, result);
        }

        HANDLER.setStackInSlot(0, itemStack);
        var recipe = CeiRecipeTypes.DISENCHANTING.<RecipeWrapper, DisenchantRecipe>find(WRAPPER, level).orElse(null);
        if (recipe != null && !recipe.hasNoResult()) {
            var xp = new FluidStack(CeiFluids.EXPERIENCE.get().getSource(), recipe.getExperience());
            var result = recipe.getResultItem(level.registryAccess()).copy();
            return Pair.of(xp, result);
        }
        return null;
    }

    public static ItemStack disenchant(ItemStack itemStack) {
        ItemStack result = itemStack.copy();

        // Get current enchantments
        ItemEnchantments currentEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);

        // Build new enchantments with only curses
        ItemEnchantments.Mutable curses = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (var entry : currentEnchantments.entrySet()) {
            if (entry.getKey().is(EnchantmentTags.CURSE)) {
                curses.set(entry.getKey(), entry.getIntValue());
            }
        }

        if (result.is(Items.ENCHANTED_BOOK) && curses.toImmutable().isEmpty()) {
            // Convert enchanted book to regular book
            result = new ItemStack(Items.BOOK);
        } else {
            // Set only curses back
            EnchantmentHelper.setEnchantments(result, curses.toImmutable());

            // Reset repair cost
            result.set(DataComponents.REPAIR_COST, 0);

            // Recalculate repair cost based on curses
            int curseCount = (int) curses.toImmutable().entrySet().stream().count();
            for (int i = 0; i < curseCount; ++i) {
                int currentCost = result.getOrDefault(DataComponents.REPAIR_COST, 0);
                result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(currentCost));
            }
        }
        return result;
    }

    private static int getDisenchantExperience(ItemStack itemStack) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
        int xp = 0;
        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!enchantment.is(EnchantmentTags.CURSE)) {
                xp += enchantment.value().getMinCost(entry.getIntValue());
            }
        }
        return xp == 0 ? 0 : Mth.ceil(xp * 0.75);
    }
}
