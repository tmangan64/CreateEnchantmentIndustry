package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public class CeiDataComponents {

    public static final DeferredRegister<DataComponentType<?>> REGISTER =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EnchantmentIndustry.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENCHANTING_GUIDE_INDEX =
        REGISTER.register("enchanting_guide_index", () ->
            DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT)
                .build()
        );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> ENCHANTING_GUIDE_TARGET =
        REGISTER.register("enchanting_guide_target", () ->
            DataComponentType.<ItemStack>builder()
                .persistent(ItemStack.OPTIONAL_CODEC)
                .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC)
                .build()
        );

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
