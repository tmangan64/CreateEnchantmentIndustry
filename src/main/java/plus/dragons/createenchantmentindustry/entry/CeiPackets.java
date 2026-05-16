package plus.dragons.createenchantmentindustry.entry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterEditPacket;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideEditPacket;

public class CeiPackets {

    public static void register(IEventBus modEventBus) {
        modEventBus.register(CeiPackets.class);
    }

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(EnchantmentIndustry.ID).versioned("1.0");
        registrar.playToServer(
            EnchantingGuideEditPacket.TYPE,
            EnchantingGuideEditPacket.STREAM_CODEC,
            EnchantingGuideEditPacket::handle
        );
        registrar.playToServer(
            BlazeEnchanterEditPacket.TYPE,
            BlazeEnchanterEditPacket.STREAM_CODEC,
            BlazeEnchanterEditPacket::handle
        );
    }
}
