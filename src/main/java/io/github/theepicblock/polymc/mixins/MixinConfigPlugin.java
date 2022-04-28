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
package io.github.theepicblock.polymc.mixins;

import io.github.theepicblock.polymc.impl.Config;
import io.github.theepicblock.polymc.impl.ConfigManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "io.github.theepicblock.polymc.mixins.";
    public Config config;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            config = ConfigManager.getConfig();
        } catch (Exception e) {
            ConfigManager.LOGGER.warn("PolyMc: couldn't read config due to exception.");
            e.printStackTrace();
            throw new NullPointerException("Couldn't read config");
        }
        if (config == null) {
            ConfigManager.LOGGER.warn("PolyMc: couldn't read config.");
            throw new NullPointerException("Couldn't read config");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        if (config.isMixinDisabled(mixin)) {
            ConfigManager.LOGGER.info(String.format("%s is disabled by config", mixin));
            return false;
        }
        if (config.isMixinAutoDisabled(mixin)) {
            ConfigManager.LOGGER.info(String.format("%s is disabled automatically", mixin));
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
