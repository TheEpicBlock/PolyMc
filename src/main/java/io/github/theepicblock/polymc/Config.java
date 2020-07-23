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
package io.github.theepicblock.polymc;

import java.util.List;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class Config {
    public static final int LATEST_VERSION = 2;

    private int configVersion;
    private List<String> disabledMixins;
    public resourcepackConfig resourcepack;
    public miscConfig misc;

    public int getConfigVersion() {
        return configVersion;
    }

    public boolean isMixinDisabled(String mixin) {
        if (disabledMixins == null) return false;
        return disabledMixins.contains(mixin);
    }

    public static class resourcepackConfig {
        public boolean advancedDiscovery;
    }

    public static class miscConfig {
        private List<String> processSyncedBlockEventServerSide;

        public List<String> getProcessSyncedBlockEventServerSide() {
            return processSyncedBlockEventServerSide;
        }
    }
}
