package nl.theepicblock.polymc.testmod;

import net.minecraft.block.DoorBlock;
import net.minecraft.sound.SoundEvents;

public class TestDoorBlock extends DoorBlock {
    protected TestDoorBlock(Settings settings) {
        super(settings, SoundEvents.ENTITY_CAMEL_DASH, SoundEvents.ENTITY_CAMEL_EAT);
    }
}
