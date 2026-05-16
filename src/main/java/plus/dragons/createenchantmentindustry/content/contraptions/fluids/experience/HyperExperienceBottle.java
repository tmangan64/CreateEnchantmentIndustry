package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import plus.dragons.createenchantmentindustry.entry.CeiEntityTypes;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiItems;

public class HyperExperienceBottle extends ThrowableItemProjectile {
    public HyperExperienceBottle(EntityType<? extends HyperExperienceBottle> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public HyperExperienceBottle(double pX, double pY, double pZ, Level pLevel) {
        super(CeiEntityTypes.HYPER_EXPERIENCE_BOTTLE.get(), pX, pY, pZ, pLevel);
    }

    public HyperExperienceBottle(LivingEntity pShooter, Level pLevel) {
        super(CeiEntityTypes.HYPER_EXPERIENCE_BOTTLE.get(), pShooter, pLevel);
    }


    @Override
    protected Item getDefaultItem() {
        return CeiItems.HYPER_EXP_BOTTLE.get();
    }

    @SuppressWarnings("unchecked")
    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<HyperExperienceBottle> entityBuilder = (EntityType.Builder<HyperExperienceBottle>) builder;
        return entityBuilder.sized(.25f, .25f);
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    @Override
    protected double getDefaultGravity() {
        return 0.07;
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (this.level() instanceof ServerLevel) {
            // 3694022 is the color for water potion (previously PotionUtils.getColor(Potions.WATER))
            this.level().levelEvent(2002, this.blockPosition(), 3694022);
            int amount = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);
            CeiFluids.HYPER_EXPERIENCE.get().drop((ServerLevel)this.level(), this.position(), amount);
            this.discard();
        }
    }
    
}
