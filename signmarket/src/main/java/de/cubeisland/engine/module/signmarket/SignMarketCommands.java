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
package de.cubeisland.engine.module.signmarket;

import java.util.HashSet;

import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.util.Arrays.asList;

public class SignMarketCommands extends ContainerCommand
{
    private final Signmarket module;

    public SignMarketCommands(Signmarket module)
    {
        super(module, "marketsign", "MarketSign-Commands");
        this.setAliases(new HashSet<>(asList("signmarket", "market")));
        this.module = module;
    }

    @Alias(names = "medit")
    @Command(alias = "edit", desc = "Enters the editmode allowing to change market signs easily")
    public void editMode(CubeContext context)
    {
        if (context.getSender() instanceof User)
        {
            if (this.module.getEditModeListener().hasUser((User)context.getSender()))
            {
                this.module.getEditModeListener().removeUser((User)context.getSender());
            }
            else
            {
                if (this.module.getConfig().disableInWorlds.contains(((User)context.getSender()).getWorld().getName()))
                {
                    context.sendTranslated(NEUTRAL, "MarketSigns are disabled in the configuration for this world!");
                    return;
                }
                this.module.getEditModeListener().addUser((User)context.getSender());
                context.sendTranslated(POSITIVE, "You are now in edit mode!");
                context.sendTranslated(POSITIVE, "Chat will now work as commands.");
                context.sendTranslated(NEUTRAL, "Type exit or use this command again to leave the edit mode.");
            }
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Only players can edit market signs!");
        }
    }
}