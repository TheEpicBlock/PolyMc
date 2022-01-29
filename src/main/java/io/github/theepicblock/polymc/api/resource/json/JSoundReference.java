package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.AssetWithDependencies;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;

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
    public void importRequirements(ModdedResources from, PolyMcResourcePack to, SimpleLogger logger) {
        var soundId = Util.parseId(this.name);
        if (soundId != null && !Util.isVanilla(soundId) && to.getSound(soundId.getNamespace(), soundId.getPath()) == null) {
            var sound = from.getSound(soundId.getNamespace(), soundId.getPath());
            if (sound != null) {
                to.setSound(soundId.getNamespace(), soundId.getPath(), sound);
                to.importRequirements(from, sound, logger);
            } else {
                logger.error("Couldn't find sound model %s".formatted(this.name));
            }
        }
    }
}
