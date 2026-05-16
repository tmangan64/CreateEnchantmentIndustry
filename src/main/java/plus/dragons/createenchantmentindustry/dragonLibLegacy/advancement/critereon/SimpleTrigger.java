package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SimpleTrigger extends SimpleCriterionTrigger<SimpleTrigger.Instance> {

    private final ResourceLocation id;

    public SimpleTrigger(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, instance -> true);
    }

    public Instance instance() {
        return new Instance(Optional.empty());
    }

    public Criterion<Instance> criterion() {
        return this.createCriterion(instance());
    }

    public static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(instance, Instance::new));

        private final Optional<ContextAwarePredicate> player;

        public Instance(Optional<ContextAwarePredicate> player) {
            this.player = player;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}
