package io.github.theepicblock.polymc.impl.poly.wizard;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import org.jetbrains.annotations.Nullable;

public class FallingBlockWizardInfo extends EntityWizardInfo {

    public FallingBlockWizardInfo(FallingBlockEntity entity) {
        super(entity);
    }

    @Override
    public @Nullable BlockState getBlockState() {
        return ((FallingBlockEntity)source).getBlockState();
    }
}
