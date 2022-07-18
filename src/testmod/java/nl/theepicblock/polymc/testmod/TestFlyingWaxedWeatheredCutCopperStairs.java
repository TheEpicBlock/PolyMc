package nl.theepicblock.polymc.testmod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class TestFlyingWaxedWeatheredCutCopperStairs extends ThrownItemEntity implements FlyingItemEntity {
    public TestFlyingWaxedWeatheredCutCopperStairs(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.WAXED_WEATHERED_CUT_COPPER_STAIRS;
    }
}
