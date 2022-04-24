/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.impl;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionComparisonOperator;

import java.util.List;

/**
 * Hello, if you've come here, you might be wondering how to add something to this config.
 * Please increment the LATEST_VERSION in this class.
 * If you're adding a value, add it into this class so it's parsed correctly.
 * (the entire config is parsed using GSON)
 * Please add an entry in the "config_update.json" under the resources folder.
 * This file defines how to update the config for each version.
 * It is based on removing and adding json.
 * Look at some of the previous ones to get an idea of how to do it.
 * The add section takes in a path and some json value/object. It adds the json value/object on that path
 * Check {@link ConfigManager#update(int, JsonObject, JsonObject)} for the exact implementation. Warning: ugly code.
 * Optional: update the "defaultconfig.json" to reflect the changes.
 * <p>
 * Sorry for this absolute mess...
 */
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection", "JavadocReference"})
public class Config {
    public static final int LATEST_VERSION = 7;
    public MiscConfig misc;
    private int configVersion;
    private List<String> disabledMixins;
    public boolean remapVanillaBlockIds;

    public int getConfigVersion() {
        return configVersion;
    }

    public boolean isMixinDisabled(String mixin) {
        if (disabledMixins == null) return false;
        return disabledMixins.contains(mixin);
    }

    public boolean isMixinAutoDisabled(String mixin) {
        // Automatically disable mixins related to processSyncedBlockEventServerSide if it's empty
        if (misc.processSyncedBlockEventServerSide.size() == 0 &&
                   (mixin.equals("block.ProcessSyncedBlockEventServerSideImplementation") ||
                   mixin.equals("ServerParticlePatch"))) {
            return true;
        }

        try {
            // Use `FabricRegistrySyncDisabler` for >=0.9.0 and `FabricRegistrySyncDisablerOld` for <0.9.0
            // If registry sync is not present both will be disabled
            if (mixin.equals("compat.FabricRegistrySyncDisabler")) {
                var regSync = FabricLoader.getInstance().getModContainer("fabric-registry-sync-v0");
                if (regSync.isEmpty()) return true;
                return VersionComparisonOperator.LESS.test(regSync.get().getMetadata().getVersion(), Version.parse("0.9.0"));
            }

            if (mixin.equals("compat.FabricRegistrySyncDisablerOld")) {
                var regSync = FabricLoader.getInstance().getModContainer("fabric-registry-sync-v0");
                if (regSync.isEmpty()) return true;
                return VersionComparisonOperator.GREATER_EQUAL.test(regSync.get().getMetadata().getVersion(), Version.parse("0.9.0"));
            }

            if (mixin.startsWith("compat.immersive_portals")) {
                return !FabricLoader.getInstance().isModLoaded("imm_ptl_core");
            }
            if (mixin.equals("block.implementations.ChunkDataPlayerProvider") || mixin.equals("wizards.block.WatchProviderMixin")) {
                return FabricLoader.getInstance().isModLoaded("imm_ptl_core");
            }
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static class MiscConfig {
        private List<String> processSyncedBlockEventServerSide;

        public List<String> getProcessSyncedBlockEventServerSide() {
            return processSyncedBlockEventServerSide;
        }
    }
}
