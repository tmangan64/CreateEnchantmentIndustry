package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

class AdvancementGen implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String name;
    private final String modid;
    DataGenerator generator;

    AdvancementGen(String name, String modid) {
        this.name = name;
        this.modid = modid;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = this.generator.getPackOutput().getOutputFolder();

        return CompletableFuture.runAsync(() -> {
            Set<ResourceLocation> set = Sets.newHashSet();
            Consumer<net.minecraft.advancements.AdvancementHolder> consumer = advancementHolder -> {
                if (!set.add(advancementHolder.id()))
                    throw new IllegalStateException("Duplicate advancement " + advancementHolder.id());
                Path advancementPath = path.resolve("data/"
                        + advancementHolder.id().getNamespace() + "/advancements/"
                        + advancementHolder.id().getPath() + ".json"
                );
                JsonElement json = Advancement.CODEC.encodeStart(JsonOps.INSTANCE, advancementHolder.value())
                        .getOrThrow(error -> new IllegalStateException("Failed to encode advancement: " + error));
                DataProvider.saveStable(cache, json, advancementPath);
            };
            var advancements = AdvancementHolder.ENTRIES_MAP.get(modid);
            if (advancements != null)
                for (var advancement : advancements) {
                    advancement.save(consumer);
                }
        });
    }

    @Override
    public String getName() {
        return name + " Advancements";
    }

}
