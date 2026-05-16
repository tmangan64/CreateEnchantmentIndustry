package plus.dragons.createenchantmentindustry.content.contraptions.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class EnchantmentLevelUtil {

    private static final MethodHandle getMaxLevel;
    static {
        Method method = null;
        try {
            Class<?> EnchHooks = Class.forName("dev.shadowsoffire.apotheosis.ench.asm.EnchHooks");
            method = EnchHooks.getMethod("getMaxLevel", Holder.class);
        } catch (Throwable exception) {
            EnchantmentIndustry.LOGGER.debug("Failed to load EnchHooks from Apotheosis, fall back to vanilla method...");
        }
        if (method != null) {
            try {
                method.setAccessible(true);
                getMaxLevel = MethodHandles.lookup().unreflect(method);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException("Failed to access EnchHooks#getMaxLevel!");
            }
        } else {
            getMaxLevel = null;
        }
    }

    public static int getMaxLevel(Holder<Enchantment> enchantment) {
        if (getMaxLevel != null) {
            try {
                return (Integer) getMaxLevel.invoke(enchantment);
            } catch (Throwable throwable) {
                EnchantmentIndustry.LOGGER.warn("Failed to invoke getMaxLevel", throwable);
            }
        }
        return enchantment.value().getMaxLevel();
    }
}
