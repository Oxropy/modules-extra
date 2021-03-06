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
package org.cubeengine.module.log.action.block.player.interact;

import org.cubeengine.module.log.action.BaseAction;
import org.cubeengine.libcube.service.user.User;
import org.cubeengine.module.log.LoggingConfiguration;
import org.cubeengine.module.log.action.block.player.ActionPlayerBlock;
import org.bukkit.material.Door;
import org.spongepowered.api.text.Text;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;
import static org.cubeengine.module.log.action.ActionCategory.USE;

/**
 * Represents a player using a door
 */
public class UseDoor extends ActionPlayerBlock
{
    public UseDoor()
    {
        super("door", USE);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof UseDoor && this.player.equals(((ActionPlayerBlock)action).player)
            && this.oldBlock == ((UseDoor)action).oldBlock;
    }

    @Override
    public Text translateAction(User user)
    {
        // TODO plurals
        @SuppressWarnings("deprecation") boolean open = this.newBlock.as(Door.class).isOpen();
        if (open)
        {
            return user.getTranslation(POSITIVE, "{user} opened the {name#block}", this.player.name,
                                       this.oldBlock.name());
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} closed the {name#block}", this.player.name,
                                       this.oldBlock.name());
        }
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.use.door;
    }
}
