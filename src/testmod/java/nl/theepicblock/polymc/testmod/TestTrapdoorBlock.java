package nl.theepicblock.polymc.testmod;

import net.minecraft.block.TrapdoorBlock;
import net.minecraft.sound.SoundEvents;

public class TestTrapdoorBlock extends TrapdoorBlock {
    protected TestTrapdoorBlock(Settings settings) {
        super(settings, SoundEvents.ENTITY_CAMEL_DASH, SoundEvents.ENTITY_CAMEL_EAT);
    }
}
