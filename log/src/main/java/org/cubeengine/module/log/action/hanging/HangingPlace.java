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
package org.cubeengine.module.log.action.hanging;

import org.cubeengine.module.log.LoggingConfiguration;
import org.cubeengine.module.log.action.BaseAction;
import org.cubeengine.libcube.service.user.User;
import org.spongepowered.api.text.Text;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;
import static org.cubeengine.module.log.action.ActionCategory.ENTITY_HANGING;

/**
 * Represents a player placing a hanging entity
 */
public class HangingPlace extends ActionHanging
{
    public HangingPlace()
    {
        super("place", ENTITY_HANGING);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof HangingPlace && this.player.equals(((HangingPlace)action).player)
            && ((HangingPlace)action).hanging.isSameType(this.hanging);
    }

    @Override
    public Text translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{name#hanging} got hung up by {user}",
                                    "{name#hanging} got hung up by {user} {amount} times", this.hanging.name(),
                                    this.player.name, count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.hanging.place;
    }
}
