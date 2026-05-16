package plus.dragons.createenchantmentindustry.compat.quark;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.api.PrintEntryRegisterEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrintEntry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.Printing;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.foundation.config.CeiConfigs;

import java.util.List;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

public class QuarkCompat {
    public static void registerPrintEntry(){
        if(ModList.get().isLoaded("quark")){
            NeoForge.EVENT_BUS.addListener(QuarkCompat::register);
        }
    }

    private static void register(PrintEntryRegisterEvent event){
        event.register(new PrintEntry() {

            private final ResourceLocation id = ResourceLocation.fromNamespaceAndPath("quark","ancient_tome");
            @Override
            public @NotNull ResourceLocation id() {
                return EnchantmentIndustry.genRL("ancient_tome");
            }

            @SuppressWarnings("all")
            @Override
            public boolean match(@NotNull ItemStack toPrint) {
                return BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(toPrint.getItem())).get().is(id);
            }

            @Override
            public boolean valid(@NotNull ItemStack target, @NotNull ItemStack tested) {
                return tested.is(Items.ENCHANTED_BOOK);
            }

            @SuppressWarnings("all")
            @Override
            public int requiredInkAmount(@NotNull ItemStack target) {
                var enchantmentHolder = getTomeEnchantment(target);
                if(enchantmentHolder == null) return 50;
                Enchantment enchantment = enchantmentHolder.value();
                // In 1.21, Enchantment.Rarity was replaced with anvilCost
                int anvilCost = enchantment.getAnvilCost();
                int rarityLevel = anvilCost <= 1 ? 1 : anvilCost <= 2 ? 2 : anvilCost <= 4 ? 3 : 4;
                return enchantment.getMinCost(1) + rarityLevel;
            }

            @Override
            public @NotNull Fluid requiredInkType(@NotNull ItemStack target) {
                return CeiFluids.HYPER_EXPERIENCE.get();
            }

            @Override
            public boolean isTooExpensive(@NotNull ItemStack target, int limit) {
                return limit<requiredInkAmount(target);
            }

            @Override
            public void addToGoggleTooltip(@NotNull List<Component> tooltip, boolean isPlayerSneaking, @NotNull ItemStack target) {
                var b = LANG.itemName(target).style(ChatFormatting.DARK_PURPLE);
                b.forGoggles(tooltip, 1);
                boolean tooExpensive = Printing.isTooExpensive(this, target, CeiConfigs.SERVER.copierTankCapacity.get());
                if (tooExpensive)
                    tooltip.add(Component.literal("     ").append(LANG.translate(
                            "gui.goggles.too_expensive").component()
                    ).withStyle(ChatFormatting.RED));
                else
                    tooltip.add(Component.literal("     ").append(LANG.translate(
                            "gui.goggles.xp_consumption",
                            String.valueOf(requiredInkAmount(target))).component()
                    ).withStyle(ChatFormatting.AQUA));
                var e = getTomeEnchantment(target);
                if(e!=null){
                    tooltip.add(Component.literal("     ").append(getFullTooltipText(e)).withStyle(ChatFormatting.GRAY));
                }
            }

            @Override
            public @NotNull MutableComponent getDisplaySourceContent(@NotNull ItemStack target) {
                var ret = LANG.itemName(target);
                var e = getTomeEnchantment(target);
                if(e!=null){
                    ret.text( " / ");
                    ret.add(getFullTooltipText(e).copy());
                }
                return ret.component();
            }

            private static Holder<Enchantment> getTomeEnchantment(ItemStack stack) {
                // In 1.21, enchantments are accessed via DataComponents
                ItemEnchantments storedEnchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                for (var entry : storedEnchantments.entrySet()) {
                    return entry.getKey();
                }
                return null;
            }

            public static Component getFullTooltipText(Holder<Enchantment> enchHolder) {
                Enchantment ench = enchHolder.value();
                return Component.translatable("quark.misc.ancient_tome_tooltip", Component.translatable(ench.description().getString()), Component.translatable("enchantment.level." + (ench.getMaxLevel() + 1)));
            }
        });
    }

}
