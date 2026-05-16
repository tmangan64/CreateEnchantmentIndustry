package plus.dragons.createenchantmentindustry.entry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer.PrinterBlockEntity;

@EventBusSubscriber(modid = EnchantmentIndustry.ID, bus = EventBusSubscriber.Bus.MOD)
public class CeiCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Disenchanter
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            CeiBlockEntities.DISENCHANTER.get(),
            (be, side) -> {
                if (side != null && side.getAxis().isHorizontal()) {
                    return be.getItemHandler(side);
                }
                return null;
            }
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            CeiBlockEntities.DISENCHANTER.get(),
            (be, side) -> be.getFluidHandler()
        );

        // Blaze Enchanter
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            CeiBlockEntities.BLAZE_ENCHANTER.get(),
            (be, side) -> {
                if (side != null && side.getAxis().isHorizontal()) {
                    return be.getItemHandler(side);
                }
                return null;
            }
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            CeiBlockEntities.BLAZE_ENCHANTER.get(),
            (be, side) -> be.getFluidHandler()
        );

        // Printer
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            CeiBlockEntities.PRINTER.get(),
            (be, side) -> be.getItemHandler()
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            CeiBlockEntities.PRINTER.get(),
            (be, side) -> be.getFluidHandler(side)
        );
    }
}
