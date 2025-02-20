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

package io.github.lxgaming.sledgehammer.integration;

import io.github.lxgaming.sledgehammer.SledgehammerPlatform;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class SpongeIntegration_Death extends AbstractIntegration {
    
    public SpongeIntegration_Death() {
        addDependency("sponge");
        setState(SledgehammerPlatform.State.INITIALIZATION);
    }
    
    @Override
    public void execute() {
        Sponge.getEventManager().registerListeners(SledgehammerPlatform.getInstance().getContainer(), this);
    }
    
    @Listener(order = Order.LAST)
    public void onDestructEntityDeath(DestructEntityEvent.Death event, @Root DamageSource damageSource) {
        // event.getMessage().isBlank() returns false in this instance possibly due to the existence of blank children
        event.setMessageCancelled(StringUtils.isBlank(event.getMessage().toPlain()));
    }
}