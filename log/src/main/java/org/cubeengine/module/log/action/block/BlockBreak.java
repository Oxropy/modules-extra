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

import org.cubeengine.service.user.User;
import org.cubeengine.module.log.LoggingConfiguration;
import org.cubeengine.module.log.action.ActionCategory;
import org.cubeengine.module.log.action.BaseAction;
import org.spongepowered.api.text.Text;

import static org.cubeengine.service.i18n.formatter.MessageType.POSITIVE;
import static org.cubeengine.module.log.action.ActionCategory.BLOCK;

/**
 * Represents a block breaking
 */
public class BlockBreak extends ActionBlock
{
    public BlockBreak()
    {
        super("break", BLOCK);
    }

    public BlockBreak(String name, ActionCategory... categories)
    {
        super(name, categories);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof BlockBreak
            && ((BlockBreak)action).oldBlock.material == this.oldBlock.material;
    }

    @Override
    public Text translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,"{name#block} got destroyed or moved",
                                    "{name#block} got destroyed or moved x{amount}", this.oldBlock.name(), count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.destroyByOther;
    }
}