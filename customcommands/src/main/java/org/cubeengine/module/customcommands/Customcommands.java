/*
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cubeengine.module.customcommands;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.cubeengine.libcube.CubeEngineModule;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.filesystem.ModuleConfig;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.Broadcaster;
import org.cubeengine.processor.Dependency;
import org.cubeengine.processor.Module;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

@Singleton
@Module
public class Customcommands extends CubeEngineModule
{
    @ModuleConfig private CustomCommandsConfig config;
    @Inject private EventManager em;
    @Inject private CommandManager cm;
    @Inject private Broadcaster bc;
    @Inject private I18n i18n;

    @Listener
    public void onEnable(GamePreInitializationEvent event)
    {
        if (this.config.commands.size() > 0)
        {
            em.registerListener(Customcommands.class, new CustomCommandsListener(this, bc));
        }
        cm.addCommand(new ManagementCommands(this, i18n, cm));
        cm.getProviders().register(this, new CustomCommandCompleter(this));
    }

    public CustomCommandsConfig getConfig()
    {
        return config;
    }
}
