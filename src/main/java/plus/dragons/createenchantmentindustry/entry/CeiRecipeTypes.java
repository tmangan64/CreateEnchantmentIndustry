package plus.dragons.createenchantmentindustry.entry;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.lang.Lang;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;

import javax.annotation.Nullable;
import java.util.Optional;

public enum CeiRecipeTypes implements IRecipeTypeInfo {
    DISENCHANTING(DisenchantRecipe::new);

    private final ResourceLocation id;
    private final Supplier<RecipeSerializer<?>> serializerObject;
    @Nullable
    private final Supplier<RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;

    CeiRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(name());
        id = EnchantmentIndustry.genRL(name);
        serializerObject = CeiRecipeTypes.Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        typeObject = CeiRecipeTypes.Registers.TYPE_REGISTER.register(name, () -> simpleType(id));
        type = typeObject;
    }

    CeiRecipeTypes(StandardProcessingRecipe.Factory<?> processingFactory) {
        this(() -> new StandardProcessingRecipe.Serializer<>(processingFactory));
    }

    public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
        String stringId = id.toString();
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return stringId;
            }
        };
    }

    public static void register(IEventBus modEventBus) {
        CeiRecipeTypes.Registers.SERIALIZER_REGISTER.register(modEventBus);
        CeiRecipeTypes.Registers.TYPE_REGISTER.register(modEventBus);
    }

    @SuppressWarnings("unchecked")
    public <T extends RecipeInput, R extends Recipe<T>> Optional<R> find(T inv, Level world) {
        RecipeType<R> type = this.getType();
        return world.getRecipeManager().getRecipeFor(type, inv, world).map(holder -> (R) holder.value());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    }

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, EnchantmentIndustry.ID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, EnchantmentIndustry.ID);
    }
}
