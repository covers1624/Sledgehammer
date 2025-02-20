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

package io.github.lxgaming.sledgehammer.configuration.category;

import com.google.common.collect.Maps;
import io.github.lxgaming.sledgehammer.configuration.category.general.MessageCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

@ConfigSerializable
public class GeneralCategory {
    
    @Setting(value = "debug", comment = "For debugging purposes")
    private boolean debug = false;
    
    @Setting(value = "message")
    private MessageCategory messageCategory = new MessageCategory();
    
    @Setting(value = "mod", comment = ""
            + "False: Mod will not be loaded.\n"
            + "True: Mod will be added to the classloader.")
    private Map<String, Boolean> modMappings = Maps.newHashMap();
    
    public boolean isDebug() {
        return debug;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public MessageCategory getMessageCategory() {
        return messageCategory;
    }
    
    public Map<String, Boolean> getModMappings() {
        return modMappings;
    }
}