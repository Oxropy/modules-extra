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
package org.cubeengine.module.spawn;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.DefaultValue;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.util.concurrent.ExecutionException;

public class SubjectParser implements ArgumentParser<Subject>, DefaultValue<Subject>
{
    private PermissionService pm;

    public SubjectParser(PermissionService pm)
    {
        this.pm = pm;
    }

    @Override
    public Subject parse(Class aClass, CommandInvocation commandInvocation) throws ParserException
    {
        try
        {
            String token = commandInvocation.currentToken();
            if (pm.getGroupSubjects().hasSubject(token).get())
            {
                return pm.getGroupSubjects().loadSubject(token).get();
            }
        }
        catch (ExecutionException | InterruptedException e)
        {
            throw new IllegalStateException(e); // TODO better handling
        }

        // TODO msg i18n.sendTranslated(ctx, NEGATIVE, "Could not find the role {input}!", role, context);
        return null;
    }

    @Override
    public Subject provide(CommandInvocation invocation)
    {
        try
        {
            if (invocation.getCommandSource() instanceof Player)
            {
                return pm.getUserSubjects().loadSubject(((Player)invocation.getCommandSource()).getIdentifier()).get();
            }
        }
        catch (ExecutionException | InterruptedException e)
        {
            throw new IllegalStateException(e); // TODO better handling
        }
        // TODO exception
        return null;
    }
}
