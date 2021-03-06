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

import org.cubeengine.module.log.LoggingConfiguration;
import org.cubeengine.module.log.action.BaseAction;
import org.cubeengine.module.log.action.block.player.ActionPlayerBlock.PlayerSection;
import org.cubeengine.libcube.service.user.User;
import org.bukkit.entity.AnimalTamer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.player.Player;
import org.bukkit.entity.Tameable;
import org.spongepowered.api.text.Text;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;

/**
 * Represents a pet dying
 */
public class DeathPet extends EntityDeathAction
{
    public PlayerSection owner;

    public DeathPet()
    {
        super("pet");
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return super.canAttach(action) && action instanceof DeathPet && this.owner.equals(((DeathPet)action).owner);
    }

    @Override
    public Text translateAction(User user)
    {
        DeathKill fetch = this.killer.fetch(DeathKill.class);
        if (fetch.isPlayerKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} {name#entity} owned by {user} got killed by {user}",
                                           this.countAttached(), this.killed.name(), this.owner.name,
                                           fetch.playerKiller.name);
            }
            return user.getTranslation(POSITIVE, "{name#entity} owned by {user} got killed by {user}",
                                       this.killed.name(), this.owner.name, fetch.playerKiller.name);
        }
        if (fetch.isEntityKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE,
                                           "{amount} {name#entity} owned by {user} could not escape {name#entity}",
                                           this.countAttached(), this.killed.name(), this.owner.name,
                                           fetch.entityKiller.name());
            }
            return user.getTranslation(POSITIVE, "{name#entity} owned by {user} could not escape {name#entity}",
                                       this.killed.name(), this.owner.name, fetch.entityKiller.name());
        }
        if (fetch.isOtherKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} {name#entity} owned by {user} died of {name#cause}",
                                           this.countAttached(), this.killed.name(), this.owner.name,
                                           fetch.otherKiller.name());
            }
            return user.getTranslation(POSITIVE, "{name#entity} owned by {user} died of {name#cause}",
                                       this.killed.name(), this.owner.name, fetch.otherKiller.name());
        }
        return user.getTranslation(POSITIVE, "{name#entity} owned by {user} died", this.killed.name(), this.owner.name);
    }

    @Override
    public void setKilled(Entity entity)
    {
        super.setKilled(entity);
        Tamer owner = ((Tameable)entity).getOwner();
        if (owner instanceof Player)
        {
            this.owner = new PlayerSection((Player)owner);
        }
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.death.pet;
    }
}
