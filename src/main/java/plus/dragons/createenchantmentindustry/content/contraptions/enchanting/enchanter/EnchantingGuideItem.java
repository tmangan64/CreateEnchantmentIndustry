package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiContainerTypes;
import plus.dragons.createenchantmentindustry.entry.CeiItems;
import plus.dragons.createenchantmentindustry.foundation.advancement.CeiAdvancements;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantingGuideItem extends Item implements MenuProvider {
    public EnchantingGuideItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getDisplayName() {
        return getDescription();
    }

    public InteractionResult useOn(UseOnContext pContext) {
        var level = pContext.getLevel();
        var player = pContext.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            var itemStack = pContext.getItemInHand();
            if (itemStack.is(CeiItems.ENCHANTING_GUIDE.get())) {
                var blockPos = pContext.getClickedPos();
                var blockState = level.getBlockState(blockPos);
                var blockEntity = level.getBlockEntity(blockPos);
                if (blockState.getBlock() instanceof BlazeBurnerBlock &&
                        blockEntity instanceof BlazeBurnerBlockEntity) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(blockPos, CeiBlocks.BLAZE_ENCHANTER.getDefaultState()
                                .setValue(BlazeEnchanterBlock.FACING, level.getBlockState(blockPos).getValue(BlazeBurnerBlock.FACING))
                        );
                        if (level.getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity tileEntity) {
                            var i = itemStack.copy();
                            i.setCount(1);
                            tileEntity.setTargetItem(i);
                        }
                        AdvancementBehaviour.setPlacedBy(pContext.getLevel(), blockPos, player);
                        CeiAdvancements.BLAZES_NEW_JOB.getTrigger().trigger((ServerPlayer) player);
                        if (!player.getAbilities().instabuild)
                            itemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
                serverPlayer.openMenu(this, (RegistryFriendlyByteBuf buf) -> {
                    ItemStack.STREAM_CODEC.encode(buf, heldItem);
                    buf.writeBoolean(true);
                });
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.create_enchantment_industry.enchanting_guide.tooltip.current_enchantment"));
        EnchantmentEntry enchantment = getEnchantment(pStack);
        if (enchantment == null) {
            pTooltipComponents.add(Component.translatable("item.create_enchantment_industry.enchanting_guide.tooltip.not_configured"));
        } else
            pTooltipComponents.add(Enchantment.getFullname(enchantment.getFirst(), enchantment.getSecond()));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return getEnchantment(pStack) != null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        ItemStack heldItem = pPlayer.getMainHandItem();
        return new EnchantingGuideMenu(CeiContainerTypes.ENCHANTING_GUIDE_FOR_BLAZE.get(), pContainerId, pPlayerInventory, heldItem, null);
    }

    @Nullable
    public static EnchantmentEntry getEnchantment(ItemStack itemStack) {
        ItemStack target = itemStack.get(CeiDataComponents.ENCHANTING_GUIDE_TARGET.get());
        if (target == null || target.isEmpty())
            return null;

        ItemEnchantments enchantments = target.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            enchantments = target.get(DataComponents.ENCHANTMENTS);
        }
        if (enchantments == null || enchantments.isEmpty())
            return null;

        List<Map.Entry<Holder<Enchantment>, Integer>> enchantmentList = new ArrayList<>();
        for (var entry : enchantments.entrySet()) {
            enchantmentList.add(Map.entry(entry.getKey(), entry.getIntValue()));
        }

        if (enchantmentList.isEmpty())
            return null;

        Integer index = itemStack.get(CeiDataComponents.ENCHANTING_GUIDE_INDEX.get());
        if (index == null) index = 0;
        if (index >= enchantmentList.size())
            index = 0;

        var result = enchantmentList.get(index);
        return EnchantmentEntry.of(result.getKey(), result.getValue());
    }
}
