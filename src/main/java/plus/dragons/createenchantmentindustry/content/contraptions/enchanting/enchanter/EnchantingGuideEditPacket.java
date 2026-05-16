package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public record EnchantingGuideEditPacket(int index, ItemStack itemStack) implements CustomPacketPayload {

    public static final Type<EnchantingGuideEditPacket> TYPE =
        new Type<>(EnchantmentIndustry.genRL("enchanting_guide_edit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingGuideEditPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, EnchantingGuideEditPacket::index,
            ItemStack.OPTIONAL_STREAM_CODEC, EnchantingGuideEditPacket::itemStack,
            EnchantingGuideEditPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EnchantingGuideEditPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                ItemStack mainHandItem = sender.getMainHandItem();
                if (!CeiItems.ENCHANTING_GUIDE.isIn(mainHandItem))
                    return;

                mainHandItem.set(CeiDataComponents.ENCHANTING_GUIDE_INDEX.get(), packet.index());
                mainHandItem.set(CeiDataComponents.ENCHANTING_GUIDE_TARGET.get(), packet.itemStack());

                sender.getCooldowns()
                        .addCooldown(mainHandItem.getItem(), 5);
            }
        });
    }
}
