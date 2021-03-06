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
package org.cubeengine.module.log.action.death;

import org.cubeengine.libcube.service.user.User;
import org.cubeengine.module.log.LoggingConfiguration;
import org.cubeengine.module.log.action.BaseAction;
import org.cubeengine.module.log.action.block.entity.ActionEntityBlock.EntitySection;
import org.cubeengine.module.log.action.block.player.ActionPlayerBlock.PlayerSection;
import org.spongepowered.api.text.Text;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;
import static org.cubeengine.module.log.action.ActionCategory.DEATH;

/**
 * Represents something killing a LivingEntity
 */
public class DeathKill extends BaseAction
{
    public PlayerSection playerKiller = null;
    public EntitySection entityKiller = null;
    public DamageCause otherKiller = null;
    public boolean projectile = false;

    public DeathKill()
    {
        super("kill", DEATH);
    }

    // TODO item in hand

    public boolean isPlayerKiller()
    {
        return playerKiller != null && playerKiller.uuid != null;
    }

    public boolean isEntityKiller()
    {
        return entityKiller != null && entityKiller.uuid != null;
    }

    public boolean isOtherKiller()
    {
        return otherKiller != null;
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        if (action instanceof DeathKill)
        {
            if (this.isPlayerKiller() && ((DeathKill)action).isPlayerKiller())
            {
                return this.playerKiller.equals(((DeathKill)action).playerKiller);
            }
            if (this.isEntityKiller() && ((DeathKill)action).isEntityKiller())
            {
                return this.entityKiller.isSameType(((DeathKill)action).entityKiller);
            }
            if (this.isOtherKiller() && ((DeathKill)action).isOtherKiller())
            {
                return this.otherKiller == ((DeathKill)action).otherKiller;
            }
        }
        return false;
    }

    @Override
    public Text translateAction(User user)
    {
        int count = this.countAttached();
        if (this.isPlayerKiller())
        {
            return user.getTranslationN(POSITIVE, count, "{user} killed an entity", "{user} killed {amount} entities",
                                        this.playerKiller.name, count);
        }
        if (this.isEntityKiller())
        {
            return user.getTranslationN(POSITIVE, count, "{name#entity} killed an entity",
                                        "{name#entity} killed {amount} entities", this.entityKiller.name(), count);
        }
        if (this.isOtherKiller())
        {
            return user.getTranslationN(POSITIVE, count, "{name#cause} killed an entity",
                                        "{name#cause} killed {amount} entities", this.otherKiller.name(), count);
        }
        return "INVALID KILLTYPE!";
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.death.killer.enable;
    }
}
