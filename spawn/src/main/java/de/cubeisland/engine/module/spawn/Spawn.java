/**
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
package de.cubeisland.engine.module.spawn;

import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.module.Inject;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.module.roles.Roles;

public class Spawn extends Module
{
    private SpawnConfig config;
    @Inject
    private Roles roles;
    private SpawnPerms perms;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(SpawnConfig.class);
        this.getCore().getEventManager().registerListener(this, new SpawnListener(roles));
        CommandManager cm = this.getCore().getCommandManager();
        cm.removeCommand("setSpawn", true); // unregister basics commands
        cm.removeCommand("spawn", true); // unregister basics commands
        cm.addCommands(cm, this, new SpawnCommands(roles, this));
        perms = new SpawnPerms(this); // PermContainer registers itself
    }

    @Override
    public void onDisable()
    {
        // TODO if not getSuggestions shutdown reregister basics commands OR do not unregister simply override (let CommandManager handle it)
    }

    public SpawnConfig getConfiguration()
    {
        return config;
    }

    public SpawnPerms perms()
    {
        return this.perms;
    }
}
