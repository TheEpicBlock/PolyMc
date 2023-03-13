package io.github.theepicblock.polymc.api.resource;

import net.minecraft.resource.InputSupplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.io.InputStream;
import java.util.Set;

public interface ClientJarResources extends AutoCloseable, ResourceContainer {
    Set<Pair<Identifier,InputSupplier<InputStream>>> locateLanguageFiles();
}
