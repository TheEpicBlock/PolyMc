package io.github.theepicblock.polymc.api.resource;

import net.minecraft.util.Identifier;

import java.util.Set;

public interface ClientJarResources extends AutoCloseable, ResourceContainer {
    Set<Identifier> locateLanguageFiles();
}
