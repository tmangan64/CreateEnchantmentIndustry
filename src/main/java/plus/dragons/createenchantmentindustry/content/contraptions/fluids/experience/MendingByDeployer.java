package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

public class MendingByDeployer {

    public static boolean canItemBeMended(Level level, ItemStack stack) {
        if (!stack.isDamaged()) return false;
        Optional<Holder.Reference<Enchantment>> mendingHolder = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.MENDING);
        return mendingHolder.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(mendingHolder.get(), stack) > 0;
    }
    
    public static int getRequiredAmountForItem(ItemStack stack) {
        return Mth.ceil(stack.getDamageValue() / stack.getXpRepairRatio());
    }
    public static int getNewXp(int xpAmount, ItemStack stack) {
        int requiredAmount = getRequiredAmountForItem(stack);
        int afterXp = 0;

        if(requiredAmount % 2 != 0) {
            requiredAmount -= 1;
        }

        if(requiredAmount == 1) {
            afterXp = xpAmount;
        }
        else if(requiredAmount > 1 && requiredAmount < xpAmount) {
            afterXp = xpAmount - requiredAmount;
        }

        return afterXp;

    }
    @Nullable
    public static ItemStack mendItem(int xpAmount, ItemStack stack) {
        int requiredAmount = getRequiredAmountForItem(stack);
        int damage = stack.getDamageValue();
        if(requiredAmount % 2 != 0) {
            requiredAmount -= 1;
        }
        damage -= xpAmount * 2;
        stack.setDamageValue(damage);
        return stack;
    }


}
