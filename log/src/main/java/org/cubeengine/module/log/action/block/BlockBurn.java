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
package org.cubeengine.module.log.action.block;

import org.cubeengine.module.log.LoggingConfiguration;
import org.cubeengine.module.log.action.ActionCategory;
import org.cubeengine.module.log.action.BaseAction;
import org.cubeengine.service.user.User;
import org.spongepowered.api.text.Text;

import static org.cubeengine.service.i18n.formatter.MessageType.POSITIVE;

/**
 * Represents a block burning away
 */
public class BlockBurn extends ActionBlock
{
    public BlockBurn()
    {
        super("burn", ActionCategory.BLOCK);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof BlockBurn && ((BlockBurn)action).oldBlock == this.oldBlock;
    }

    @Override
    public Text translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "A {name#block} went up into flames",
                                    "{1:amount}x {name#block} went up into flames", this.oldBlock.name(), count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.destroyByFire;
    }
}