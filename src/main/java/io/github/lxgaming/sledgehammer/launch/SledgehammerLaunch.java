/*
 * Copyright 2019 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.sledgehammer.launch;

import io.github.lxgaming.sledgehammer.util.Reference;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.mixin.Mixins;

import java.util.List;

public class SledgehammerLaunch {
    
    private static final Logger LOGGER = LogManager.getLogger(Reference.NAME + " Launch");
    private static final String FORGE_CLASS = "net.minecraftforge.fml.relauncher.CoreModManager";
    private static final String FORGE_DEOBF_TWEAKER_CLASS = "net.minecraftforge.fml.common.launcher.FMLDeobfTweaker";
    private static final String FORGE_INITIALIZED = "forge.initialized";
    private static final String SLEDGEHAMMER_INITIALIZED = Reference.ID + ".initialized";
    private static final String SPONGE_CLASS = "org.spongepowered.common.launch.SpongeLaunch";
    private static final String SPONGE_INITIALIZED = "sponge.initialized";
    
    private SledgehammerLaunch() {
    }
    
    public static void configureClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("io.github.lxgaming.sledgehammer.launch.");
        classLoader.addTransformerExclusion("io.github.lxgaming.sledgehammer.launch.");
        classLoader.addTransformerExclusion("io.github.lxgaming.sledgehammer.lib.");
    }
    
    public static void configureEnvironment() {
        if (!isForgeRegistered() && isClassPresent(FORGE_CLASS)) {
            registerForge();
        }
        
        if (!isSledgehammerRegistered()) {
            registerSledgehammer();
            
            if (isTweakerQueued(SledgehammerTweaker.class)) {
                Mixins.addConfiguration("mixins.sledgehammer.preinit.json");
            }
        }
        
        if (!isSpongeRegistered() && isClassPresent(SPONGE_CLASS)) {
            registerSponge();
        }
    }
    
    public static boolean isEarly() {
        return !isClassPresent(FORGE_CLASS) || isTweakerQueued(FORGE_DEOBF_TWEAKER_CLASS);
    }
    
    public static boolean isClassPresent(String name) {
        try {
            return Class.forName(name, false, Launch.classLoader) != null;
        } catch (Throwable ex) {
            return false;
        }
    }
    
    public static boolean isTweakerQueued(Class<? extends ITweaker> tweakerClass) {
        return isTweakerQueued(tweakerClass.getName());
    }
    
    public static boolean isTweakerQueued(String tweakerClass) {
        return getTweakerClasses().contains(tweakerClass)
                || getTweakers().stream().map(ITweaker::getClass).map(Class::getName).anyMatch(tweakerClass::equals);
    }
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public static List<String> getTweakerClasses() {
        return GlobalProperties.get("TweakClasses");
    }
    
    public static List<ITweaker> getTweakers() {
        return GlobalProperties.get("Tweaks");
    }
    
    public static boolean isDeobfuscatedEnvironment() {
        return GlobalProperties.get("fml.deobfuscatedEnvironment", false);
    }
    
    public static boolean isForgeRegistered() {
        return GlobalProperties.get(FORGE_INITIALIZED) == Boolean.TRUE;
    }
    
    private static void registerForge() {
        GlobalProperties.put(FORGE_INITIALIZED, Boolean.TRUE);
    }
    
    public static boolean isMixinRegistered() {
        return getMixinVersion() != null;
    }
    
    public static String getMixinVersion() {
        return GlobalProperties.get(GlobalProperties.Keys.INIT);
    }
    
    public static boolean isSledgehammerRegistered() {
        return GlobalProperties.get(SLEDGEHAMMER_INITIALIZED) != null;
    }
    
    private static void registerSledgehammer() {
        GlobalProperties.put(SLEDGEHAMMER_INITIALIZED, Reference.VERSION);
    }
    
    public static boolean isSpongeRegistered() {
        return GlobalProperties.get(SPONGE_INITIALIZED) == Boolean.TRUE;
    }
    
    private static void registerSponge() {
        GlobalProperties.put(SPONGE_INITIALIZED, Boolean.TRUE);
    }
}