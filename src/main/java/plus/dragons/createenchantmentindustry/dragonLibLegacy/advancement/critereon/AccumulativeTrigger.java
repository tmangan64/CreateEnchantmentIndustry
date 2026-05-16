package plus.dragons.createenchantmentindustry.dragonLibLegacy.advancement.critereon;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Optional;
import java.util.UUID;

public class AccumulativeTrigger extends SimpleCriterionTrigger<AccumulativeTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public AccumulativeTrigger(ResourceLocation pId) {
        this.id = pId;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.codec(id);
    }

    public void trigger(Player pPlayer, int change) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            this.trigger(serverPlayer, (triggerInstance) -> triggerInstance.matches(id, pPlayer, change));
        }
    }

    public TriggerInstance instance(int requirement) {
        return new TriggerInstance(id, Optional.empty(), MinMaxBounds.Ints.atLeast(requirement));
    }

    public Criterion<TriggerInstance> criterion(int requirement) {
        return this.createCriterion(instance(requirement));
    }

    private static class AccumulativeData extends SavedData {
        public Table<ResourceLocation, UUID, Integer> data;

        public void change(ResourceLocation resourceLocation, UUID playerId, int i) {
            var temp = data.get(resourceLocation, playerId);
            temp = temp == null ? 0 : temp;
            temp += i;
            data.put(resourceLocation, playerId, temp);
            setDirty();
        }

        public int get(ResourceLocation resourceLocation, UUID playerId) {
            var ret = data.get(resourceLocation, playerId);
            return ret == null ? 0 : ret;
        }

        public AccumulativeData() {
            data = HashBasedTable.create();
        }

        public static AccumulativeData load(CompoundTag compoundNBT, HolderLookup.Provider provider) {
            AccumulativeData ret = new AccumulativeData();

            if (!compoundNBT.contains("AccumulativeData"))
                return ret;

            var list = NBTHelper.readCompoundList((ListTag) compoundNBT.get("AccumulativeData"), c -> new TriCell(
                    NBTHelper.readResourceLocation(c, "TriggerId"),
                    c.getUUID("PlayerId"),
                    c.getInt("Count")
            ));

            list.forEach(triCell -> ret.data.put(triCell.rl, triCell.id, triCell.i));
            return ret;
        }

        @Override
        public CompoundTag save(CompoundTag pCompoundTag, HolderLookup.Provider provider) {
            var dataListTag = NBTHelper.writeCompoundList(data.cellSet().stream().toList(), cell -> {
                var ret = new CompoundTag();
                NBTHelper.writeResourceLocation(ret, "TriggerId", cell.getRowKey());
                ret.putUUID("PlayerId", cell.getColumnKey());
                ret.putInt("Count", cell.getValue());
                return ret;
            });
            pCompoundTag.put("AccumulativeData", dataListTag);
            return pCompoundTag;
        }

        private record TriCell(ResourceLocation rl, UUID id, int i) {
        }
    }

    private static AccumulativeData get(Level level) {
        if (!(level instanceof ServerLevel)) {
            throw new RuntimeException("Attempted to get the data from a client world.");
        }

        ServerLevel serverWorld = level.getServer().overworld();
        DimensionDataStorage dimensionSavedDataManager = serverWorld.getDataStorage();
        return dimensionSavedDataManager.computeIfAbsent(
                new SavedData.Factory<>(AccumulativeData::new, AccumulativeData::load),
                "accumulative_data"
        );
    }

    public static class TriggerInstance implements SimpleCriterionTrigger.SimpleInstance {
        private final ResourceLocation triggerId;
        private final Optional<ContextAwarePredicate> player;
        private final MinMaxBounds.Ints requirement;

        public static Codec<TriggerInstance> codec(ResourceLocation triggerId) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                    MinMaxBounds.Ints.CODEC.fieldOf("requirement").forGetter(i -> i.requirement)
            ).apply(instance, (player, requirement) -> new TriggerInstance(triggerId, player, requirement)));
        }

        public TriggerInstance(ResourceLocation triggerId, Optional<ContextAwarePredicate> player, MinMaxBounds.Ints requirement) {
            this.triggerId = triggerId;
            this.player = player;
            this.requirement = requirement;
        }

        public boolean matches(ResourceLocation resourceLocation, Player player, int change) {
            AccumulativeData data = get(player.level());
            data.change(resourceLocation, player.getUUID(), change);
            return requirement.matches(data.get(resourceLocation, player.getUUID()));
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}
