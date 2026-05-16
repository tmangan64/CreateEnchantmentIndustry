package plus.dragons.createenchantmentindustry.foundation.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.FurnaceExpExtractor;
import plus.dragons.createenchantmentindustry.foundation.mixin.accessor.FurnaceExpExtractorAccess;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract public class AbstractFurnaceBlockEntityMixin extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible, FurnaceExpExtractorAccess {
    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Final
    @Shadow(remap = false)
    private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

    @Unique
    private IFluidHandler createEnchantmentIndustry$expExtractor;

    @Unique
    @Override
    public IFluidHandler createEnchantmentIndustry$getExpExtractor() {
        if (createEnchantmentIndustry$expExtractor == null) {
            createEnchantmentIndustry$expExtractor = new FurnaceExpExtractor(recipesUsed, (AbstractFurnaceBlockEntity) (Object) this);
        }
        return createEnchantmentIndustry$expExtractor;
    }
}
