/*
 * Copyright 2018 Alex Thomson
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

package io.github.lxgaming.sledgehammer.mixin.plugin;

import com.google.common.collect.Sets;
import io.github.lxgaming.sledgehammer.Sledgehammer;
import io.github.lxgaming.sledgehammer.interfaces.fml.common.IMixinLoader;
import io.github.lxgaming.sledgehammer.interfaces.fml.common.IMixinMetadataCollection;
import io.github.lxgaming.sledgehammer.launch.SledgehammerLaunch;
import io.github.lxgaming.sledgehammer.manager.MappingManager;
import io.github.lxgaming.sledgehammer.util.Toolbox;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ForgePlugin extends AbstractPlugin {
    
    private static boolean INITIALIZED;
    
    @Override
    public void onLoad(String mixinPackage) {
        super.onLoad(mixinPackage);
        if (MixinEnvironment.getCurrentEnvironment().getPhase() != MixinEnvironment.Phase.DEFAULT) {
            return;
        }
        
        if (INITIALIZED
                || SledgehammerLaunch.isDeobfuscatedEnvironment()
                || !SledgehammerLaunch.isForgeRegistered()
                || !SledgehammerLaunch.isSledgehammerRegistered()
                || MappingManager.getModMappings().isEmpty()) {
            return;
        }
        
        INITIALIZED = true;
        if (!MappingManager.getMixinMapping("forge.fml.common.MixinLoader").orElse(false)) {
            Sledgehammer.getInstance().getLogger().error("MixinLoader is disabled");
            return;
        }
        
        if (!MappingManager.getMixinMapping("forge.fml.common.MixinMetadataCollection").orElse(false)) {
            Sledgehammer.getInstance().getLogger().error("MixinMetadataCollection is disabled");
            return;
        }
        
        IMixinLoader mixinLoader = (IMixinLoader) Loader.instance();
        for (File file : LibraryManager.gatherLegacyCanidates(mixinLoader.getMinecraftDirectory())) {
            Map<String, ModMetadata> metadatas = getMetadataCollection(file).getMetadatas();
            if (metadatas.isEmpty()) {
                continue;
            }
            
            mixinLoader.getMappings().computeIfAbsent(file, key -> Sets.newHashSet());
            Sledgehammer.getInstance().getLogger().debug("{}:", file.getName());
            for (String id : metadatas.keySet()) {
                if (mixinLoader.getMappings().get(file).add(id)) {
                    Sledgehammer.getInstance().getLogger().debug("   {}", id);
                } else {
                    Sledgehammer.getInstance().getLogger().debug("   {} (Duplicate)", id);
                }
            }
        }
        
        int size = mixinLoader.getMappings().size();
        Sledgehammer.getInstance().getLogger().info("Identified {} {}", size, Toolbox.formatUnit(size, "mod", "mods"));
        
        for (Map.Entry<File, Set<String>> entry : mixinLoader.getMappings().entrySet()) {
            for (String id : entry.getValue()) {
                Boolean modMapping = MappingManager.getModMapping(id).orElse(null);
                if (modMapping == null) {
                    continue;
                }
                
                if (modMapping) {
                    // Add Mods
                    if (CoreModManager.getIgnoredMods().removeIf(entry.getKey().getName()::equals)) {
                        Sledgehammer.getInstance().getLogger().info("Acknowledged {}", entry.getKey().getName());
                    }
                    
                    mixinLoader.addFile(entry.getKey());
                } else {
                    // Remove Mods
                    if (CoreModManager.getIgnoredMods().contains(entry.getKey().getName())) {
                        continue;
                    }
                    
                    CoreModManager.getIgnoredMods().add(entry.getKey().getName());
                    Sledgehammer.getInstance().getLogger().info("Ignored {}", entry.getKey().getName());
                }
            }
        }
    }
    
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!SledgehammerLaunch.isForgeRegistered()) {
            return false;
        }
        
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
    
    private IMixinMetadataCollection getMetadataCollection(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            ZipEntry zipEntry = jarFile.getEntry("mcmod.info");
            if (zipEntry == null) {
                return (IMixinMetadataCollection) MetadataCollection.from(null, "");
            }
            
            try (InputStream inputStream = jarFile.getInputStream(zipEntry)) {
                return (IMixinMetadataCollection) MetadataCollection.from(inputStream, jarFile.getName());
            }
        } catch (Exception ex) {
            Sledgehammer.getInstance().getLogger().error("Encountered an error while getting Metadata from {}", file, ex);
            return (IMixinMetadataCollection) MetadataCollection.from(null, "");
        }
    }
}