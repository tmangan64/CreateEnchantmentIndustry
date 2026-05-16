package plus.dragons.createenchantmentindustry.compat.jei.category;

import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

/*
MIT License

Copyright (c) 2019 simibubi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

public class RecipeCategoryBuilder<T extends Recipe<?>> {
    private final String modid;
    private final Class<? extends T> recipeClass;
    private Predicate<CRecipes> predicate = cRecipes -> true;

    private IDrawable background;
    private IDrawable icon;

    private final List<Consumer<List<RecipeHolder<T>>>> recipeListConsumers = new ArrayList<>();
    private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

    public RecipeCategoryBuilder(String modid, Class<? extends T> recipeClass) {
        this.modid = modid;
        this.recipeClass = recipeClass;
    }

    public RecipeCategoryBuilder<T> enableIf(Predicate<CRecipes> predicate) {
        this.predicate = predicate;
        return this;
    }

    public RecipeCategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBase.ConfigBool> configValue) {
        predicate = c -> configValue.apply(c).get();
        return this;
    }

    public RecipeCategoryBuilder<T> addRecipeListConsumer(Consumer<List<RecipeHolder<T>>> consumer) {
        recipeListConsumers.add(consumer);
        return this;
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addRecipes(Supplier<Collection<? extends RecipeHolder<T>>> collection) {
        return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addAllRecipesIf(Predicate<RecipeHolder<?>> pred) {
        return addRecipeListConsumer(recipes -> CreateJEI.consumeAllRecipes(holder -> {
            if (pred.test(holder)) {
                recipes.add((RecipeHolder<T>) holder);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addAllRecipesIf(Predicate<RecipeHolder<?>> pred, Function<RecipeHolder<?>, RecipeHolder<T>> converter) {
        return addRecipeListConsumer(recipes -> CreateJEI.consumeAllRecipes(holder -> {
            if (pred.test(holder)) {
                recipes.add(converter.apply(holder));
            }
        }));
    }

    @SuppressWarnings("unchecked")
    public <O extends Recipe<?>> RecipeCategoryBuilder<T> addTransformedRecipes(Supplier<RecipeType<O>> recipeType, Function<RecipeHolder<O>, RecipeHolder<T>> converter) {
        return addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(holder -> recipes.add(converter.apply((RecipeHolder<O>) holder)), recipeType.get()));
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
        return addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(holder -> recipes.add((RecipeHolder<T>) holder), recipeTypeEntry.getType()));
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
        return addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(holder -> recipes.add((RecipeHolder<T>) holder), recipeType.get()));
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<RecipeHolder<?>> pred) {
        return addRecipeListConsumer(recipes -> CreateJEI.consumeTypedRecipes(holder -> {
            if (pred.test(holder)) {
                recipes.add((RecipeHolder<T>) holder);
            }
        }, recipeType.get()));
    }

    @SuppressWarnings("unchecked")
    public RecipeCategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
                                                             Supplier<RecipeType<? extends T>> excluded) {
        return addRecipeListConsumer(recipes -> {
            List<RecipeHolder<?>> excludedRecipes = CreateJEI.getTypedRecipes(excluded.get());
            CreateJEI.consumeTypedRecipes(holder -> {
                for (RecipeHolder<?> excludedHolder : excludedRecipes) {
                    if (CreateJEI.doInputsMatch(holder.value(), excludedHolder.value())) {
                        return;
                    }
                }
                recipes.add((RecipeHolder<T>) holder);
            }, recipeType.get());
        });
    }

    public RecipeCategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
        return addRecipeListConsumer(recipes -> {
            List<RecipeHolder<?>> excludedRecipes = CreateJEI.getTypedRecipes(recipeType.get());
            recipes.removeIf(holder -> {
                for (RecipeHolder<?> excludedHolder : excludedRecipes) {
                    if (CreateJEI.doInputsMatch(holder.value(), excludedHolder.value())) {
                        return true;
                    }
                }
                return false;
            });
        });
    }

    public RecipeCategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
        catalysts.add(supplier);
        return this;
    }

    public RecipeCategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
        return catalystStack(() -> new ItemStack(supplier.get()
            .asItem()));
    }

    public RecipeCategoryBuilder<T> icon(IDrawable icon) {
        this.icon = icon;
        return this;
    }

    public RecipeCategoryBuilder<T> itemIcon(ItemLike item) {
        icon(new ItemIcon(() -> new ItemStack(item)));
        return this;
    }

    public RecipeCategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
        icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
        return this;
    }

    public RecipeCategoryBuilder<T> background(IDrawable background) {
        this.background = background;
        return this;
    }

    public RecipeCategoryBuilder<T> emptyBackground(int width, int height) {
        background(new EmptyBackground(width, height));
        return this;
    }

    public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {
        Supplier<List<RecipeHolder<T>>> recipesSupplier;
        if (predicate.test(AllConfigs.server().recipes)) {
            recipesSupplier = () -> {
                List<RecipeHolder<T>> recipes = new ArrayList<>();
                for (Consumer<List<RecipeHolder<T>>> consumer : recipeListConsumers)
                    consumer.accept(recipes);
                return recipes;
            };
        } else {
            recipesSupplier = Collections::emptyList;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
        mezz.jei.api.recipe.RecipeType<RecipeHolder<T>> jeiRecipeType = new mezz.jei.api.recipe.RecipeType<>(id, (Class<RecipeHolder<T>>) (Class<?>) RecipeHolder.class);
        CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
            jeiRecipeType,
            LANG.fromRL("recipe", id).component(),
            background, icon, recipesSupplier, catalysts);
        return factory.create(info);
    }

}
