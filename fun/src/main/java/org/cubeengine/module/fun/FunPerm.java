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
package org.cubeengine.module.fun;

import java.util.Locale;
import de.cubeisland.engine.service.permission.PermDefault;
import de.cubeisland.engine.service.permission.Permission;
import org.cubeengine.service.permission.PermissionContainer;
import org.cubeengine.service.permission.PermissionManager;
import org.bukkit.entity.EntityType;
import org.spongepowered.api.service.permission.PermissionDescription;

@SuppressWarnings("all")
public class FunPerm extends PermissionContainer<Fun>
{
    public boolean ARE_THROW_ITEMS_REGISTERED = false;

    public FunPerm(Fun module)
    {
        super(module);
        this.registerAllPermissions();

        if (!ARE_THROW_ITEMS_REGISTERED)
        {
            PermissionManager perm = module.getCore().getPermissionManager();
            for (EntityType type : EntityType.values())
            {
                if (type.isSpawnable())
                {
                    perm.registerPermission(module, COMMAND_THROW.child(type.name().toLowerCase(Locale.ENGLISH).replace("_", "-")));
                }
            }
            ARE_THROW_ITEMS_REGISTERED = true;
        }
    }

    private final PermissionDescription COMMAND = getBasePerm().childWildcard("command");

    private final PermissionDescription COMMAND_EXPLOSION = COMMAND.childWildcard("explosion");
    public final PermissionDescription COMMAND_EXPLOSION_OTHER = COMMAND_EXPLOSION.child("other");
    public final PermissionDescription COMMAND_EXPLOSION_PLAYER_DAMAGE = COMMAND_EXPLOSION.child("player.damage");
    public final PermissionDescription COMMAND_EXPLOSION_BLOCK_DAMAGE = COMMAND_EXPLOSION.child("block.damage");
    public final PermissionDescription COMMAND_EXPLOSION_FIRE = COMMAND_EXPLOSION.child("fire");

    private final PermissionDescription COMMAND_HAT = COMMAND.childWildcard("hat");
    public final PermissionDescription COMMAND_HAT_OTHER = COMMAND_HAT.child("other");
    public final PermissionDescription COMMAND_HAT_ITEM = COMMAND_HAT.child("item");
    public final PermissionDescription COMMAND_HAT_MORE_ARMOR = COMMAND_HAT.child("more-armor");
    public final PermissionDescription COMMAND_HAT_QUIET = COMMAND_HAT.child("quit");
    public final PermissionDescription COMMAND_HAT_NOTIFY = COMMAND_HAT.child("notify", PermDefault.TRUE);

    private final PermissionDescription COMMAND_LIGHTNING = COMMAND.childWildcard("lightning");
    public final PermissionDescription COMMAND_LIGHTNING_PLAYER_DAMAGE = COMMAND_LIGHTNING.child("player.damage");
    public final PermissionDescription COMMAND_LIGHTNING_UNSAFE = COMMAND_LIGHTNING.child("unsafe");

    public final PermissionDescription COMMAND_THROW = COMMAND.childWildcard("throw");
    public final PermissionDescription COMMAND_THROW_UNSAFE = COMMAND_THROW.child("unsafe");

    private final PermissionDescription COMMAND_NUKE = COMMAND.childWildcard("nuke");
    public final PermissionDescription COMMAND_NUKE_CHANGE_RANGE = COMMAND_NUKE.child("change_range");
    public final PermissionDescription COMMAND_NUKE_OTHER = COMMAND_NUKE.child("other");
}
