package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Enchanting {

    public static final TagKey<Item> UNENCHANTABLE =
            TagKey.create(Registries.ITEM, EnchantmentIndustry.genRL("unenchantable"));
    public static final List<Predicate<ItemStack>> UNENCHANTABLE_CONDITIONS = new ArrayList<>();

    @Nullable
    public static EnchantmentEntry getTargetEnchantment(ItemStack itemStack, boolean hyper) {
        if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
            var result = EnchantingGuideItem.getEnchantment(itemStack);
            if (!hyper || result == null)
                return result;
            else {
                var enchantment = result.getFirst();
                int level = result.getSecond() + 1;
                return EnchantmentEntry.of(enchantment, level);
            }
        } else
            throw new RuntimeException("TargetItem is not an enchanting guide for blaze!");
    }

    @Nullable
    public static EnchantmentEntry getValidEnchantment(ItemStack itemStack, ItemStack targetItem, boolean hyper) {

        if(itemStack.is(UNENCHANTABLE)) return null;
        if(!UNENCHANTABLE_CONDITIONS.isEmpty()){
            if(UNENCHANTABLE_CONDITIONS.stream()
                    .map(itemStackPredicate -> itemStackPredicate.test(itemStack))
                    .reduce((b1,b2)->b1||b2).get()) return null;
        }

        var entry = getTargetEnchantment(targetItem, hyper);
        if (entry == null || !entry.valid())
            return null;
        var enchantmentHolder = entry.getFirst();

        ItemStack toCheck = itemStack.copy();
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(toCheck);

        // Check if item already has this enchantment at the same or higher level
        int currentLevel = enchantments.getLevel(enchantmentHolder);
        if (currentLevel >= entry.getSecond()) {
            return null;
        }

        // Check if the enchantment can be applied to this item
        if (!enchantmentHolder.value().canEnchant(toCheck))
            return null;

        // Check compatibility with existing enchantments
        for (var e : enchantments.entrySet()) {
            if (!Enchantment.areCompatible(e.getKey(), enchantmentHolder))
                return null;
        }
        return entry;
    }

    public static void enchantItem(ItemStack itemStack, Pair<Holder<Enchantment>, Integer> enchantment) {
        itemStack.enchant(enchantment.getFirst(), enchantment.getSecond());
    }

    public static int expPointFromLevel(int level) {
        if (level > 31) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else {
            return level > 16
                ? (int) (2.5 * level * level - 40.5 * level + 360)
                : level * level + 6 * level;
        }
    }

    public static int expPointForNextLevel(int level) {
        if (level > 30) {
            return 9 * level - 158;
        } else {
            return level > 15
                ? 5 * level -38
                : 2 * level + 7;
        }
    }

    public static int getExperienceConsumption(Holder<Enchantment> enchantment, int level) {
        Enchantment ench = enchantment.value();
        int xpLevel = ench.getMinCost(level) + level * getAnvilCost(ench);
        return expPointForNextLevel(xpLevel);
    }

    // In 1.21, Enchantment.Rarity was replaced with anvilCost
    private static int getAnvilCost(Enchantment enchantment) {
        int cost = enchantment.getAnvilCost();
        if (cost <= 1) return 1;
        if (cost <= 2) return 2;
        if (cost <= 4) return 3;
        return 4;
    }

}
