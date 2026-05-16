package plus.dragons.createenchantmentindustry.foundation.mixin;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.ink.InkRenderingCamera;
import plus.dragons.createenchantmentindustry.entry.CeiTags;

@Mixin(Camera.class)
@Implements(@Interface(iface = InkRenderingCamera.class, prefix = "enchantmentIndustry$"))
public class CameraMixin {

    @Shadow(remap = false)
    private BlockGetter level;

    @Shadow(remap = false)
    @Final
    private BlockPos.MutableBlockPos blockPosition;

    @Shadow(remap = false)
    private Vec3 position;
    @Unique
    private boolean enchantmentIndustry$inInk;

    @Inject(method = "getFluidInCamera", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;", remap = false, ordinal = 0), cancellable = true)
    private void updateInk(CallbackInfoReturnable<FogType> cir) {
        FluidState fluidstate = this.level.getFluidState(this.blockPosition);
        if (fluidstate.is(CeiTags.FluidTag.INK.tag) && this.position.y < (double) ((float) this.blockPosition.getY() + fluidstate.getHeight(this.level, this.blockPosition))) {
            enchantmentIndustry$inInk = true;
            cir.setReturnValue(FogType.POWDER_SNOW);
        } else enchantmentIndustry$inInk = false;
    }

    public boolean enchantmentIndustry$isInInk() {
        return enchantmentIndustry$inInk;
    }

}
