package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

public record BlazeEnchanterEditPacket(int index, ItemStack itemStack, BlockPos blockPos) implements CustomPacketPayload {

    public static final Type<BlazeEnchanterEditPacket> TYPE =
        new Type<>(EnchantmentIndustry.genRL("blaze_enchanter_edit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlazeEnchanterEditPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, BlazeEnchanterEditPacket::index,
            ItemStack.OPTIONAL_STREAM_CODEC, BlazeEnchanterEditPacket::itemStack,
            BlockPos.STREAM_CODEC, BlazeEnchanterEditPacket::blockPos,
            BlazeEnchanterEditPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BlazeEnchanterEditPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                if (!(sender.level().getBlockEntity(packet.blockPos()) instanceof BlazeEnchanterBlockEntity blazeEnchanter))
                    return;

                blazeEnchanter.targetItem.set(CeiDataComponents.ENCHANTING_GUIDE_INDEX.get(), packet.index());
                blazeEnchanter.targetItem.set(CeiDataComponents.ENCHANTING_GUIDE_TARGET.get(), packet.itemStack());

                if (blazeEnchanter.processingTicks > 5) {
                    blazeEnchanter.processingTicks = BlazeEnchanterBlockEntity.ENCHANTING_TIME;
                }

                blazeEnchanter.notifyUpdate();
            }
        });
    }
}
