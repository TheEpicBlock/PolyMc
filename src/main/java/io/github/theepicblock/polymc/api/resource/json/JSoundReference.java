package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.AssetWithDependencies;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import net.minecraft.util.Identifier;

public class JSoundReference implements AssetWithDependencies {
    public String name;
    public float volume = 1;
    public float pitch = 1;
    public int weight = 1;
    public boolean stream = false;
    public int attenuation_distance = 16;
    public boolean preload = false;
    public SoundType type = SoundType.sound;

    enum SoundType {
        sound,
        event
    }

    public boolean isNameOnly() {
        //noinspection PointlessBooleanExpression
        return volume == 1 &&
                pitch == 1 &&
                weight == 1 &&
                stream == false &&
                attenuation_distance == 16 &&
                preload == false &&
                type == SoundType.sound;
    }

    @Override
    public void importRequirements(ModdedResources from, PolyMcResourcePack to) {
        var soundId = Identifier.tryParse(this.name);
        if (soundId != null) {
            var sound = from.getSound(soundId.getNamespace(), soundId.getPath());
            to.setSound(soundId.getNamespace(), soundId.getPath(), sound);
        }
    }
}
