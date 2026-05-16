package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TriggerFactory {
    private final List<CriterionTrigger<?>> triggers = new ArrayList<>();

    public SimpleTrigger simple(ResourceLocation resourceLocation) {
        return add(new SimpleTrigger(resourceLocation));
    }

    public AccumulativeTrigger accumulative(ResourceLocation resourceLocation) {
        return add(new AccumulativeTrigger(resourceLocation));
    }

    private <T extends CriterionTrigger<?>> T add(T instance) {
        triggers.add(instance);
        return instance;
    }

    public void register() {
        triggers.forEach(trigger -> {
            ResourceLocation id = getId(trigger);
            Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
        });
    }

    private ResourceLocation getId(CriterionTrigger<?> trigger) {
        if (trigger instanceof SimpleTrigger simpleTrigger) {
            return simpleTrigger.getId();
        } else if (trigger instanceof AccumulativeTrigger accumulativeTrigger) {
            return accumulativeTrigger.getId();
        }
        throw new IllegalArgumentException("Unknown trigger type: " + trigger.getClass());
    }

}
