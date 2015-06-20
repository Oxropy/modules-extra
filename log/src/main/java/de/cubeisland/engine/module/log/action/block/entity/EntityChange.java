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
package de.cubeisland.engine.module.log.action.block.entity;

import de.cubeisland.engine.service.user.User;
import de.cubeisland.engine.module.log.LoggingConfiguration;
import de.cubeisland.engine.module.log.action.BaseAction;
import org.spongepowered.api.text.Text;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.module.log.action.ActionCategory.ENTITY;

/**
 * Represents an entity changing a block
 */
public class EntityChange extends ActionEntityBlock
{
    public EntityChange()
    {
        super("change", ENTITY);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return false;
    }

    @Override
    public Text translateAction(User user)
    {
        return user.getTranslation(POSITIVE, "{name#entity} changed {name#block} to {name#block}", this.entity.name(),
                                   this.oldBlock.name(), this.newBlock.name());
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return false;
    }
}
