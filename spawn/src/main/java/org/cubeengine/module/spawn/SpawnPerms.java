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
package org.cubeengine.module.spawn;

import org.cubeengine.service.permission.PermissionContainer;
import org.spongepowered.api.service.permission.PermissionDescription;

@SuppressWarnings("all")
public class SpawnPerms extends PermissionContainer<Spawn>
{
    public SpawnPerms(Spawn module)
    {
        super(module);
    }
    private final PermissionDescription COMMAND_SPAWN = getBasePerm().childWildcard("command").childWildcard("spawn");
    /**
     * Prevents from being teleported to spawn by someone else
     */
    public final PermissionDescription COMMAND_SPAWN_PREVENT = COMMAND_SPAWN.child("prevent");
    /**
     * Allows teleporting a player to spawn even if the player has the prevent permission
     */
    public final PermissionDescription COMMAND_SPAWN_FORCE = COMMAND_SPAWN.child("force");

}