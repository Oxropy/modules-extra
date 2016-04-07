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
package org.cubeengine.module.customcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import de.cubeisland.engine.butler.CommandInvocation;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.Flag;
import org.cubeengine.butler.parametric.Complete;
import org.cubeengine.butler.parametric.Greed;
import org.cubeengine.butler.result.CommandResult;
import org.cubeengine.service.command.ContainerCommand;
import org.cubeengine.service.command.CommandContext;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.butler.parameter.Parameter.INFINITE;
import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.service.i18n.formatter.MessageType.POSITIVE;
import static java.util.Locale.ENGLISH;

@Command(name = "customcommands", desc = "Commands to modify custom commands.")
public class ManagementCommands extends ContainerCommand
{
    private final Customcommands module;
    private I18n i18n;
    private final CustomCommandsConfig config;

    public ManagementCommands(Customcommands module, I18n i18n)
    {
        super(module);
        this.module = module;
        this.i18n = i18n;
        this.config = module.getConfig();
    }

    @Command(desc = "Adds a custom chat command.")
    public void add(CommandSource context, String name, @Greed(INFINITE) String message, @Flag boolean force, @Flag boolean global)
    {
        if (config.commands.containsKey(name))
        {
            if (!force)
            {
                i18n.sendTranslated(context, NEGATIVE, "Custom command {input} already exists. Set the flag {text:-force} if you want to replace the message.", "!" + name);
                return;
            }
            config.commands.put(name, message);
            i18n.sendTranslated(context, POSITIVE, "Custom command {input} has successfully been replaced.", "!" + name);
        }
        else
        {
            config.commands.put(name.toLowerCase(ENGLISH), message);
            i18n.sendTranslated(context, POSITIVE, "Custom command {input} has successfully been added.", "!" + name);
        }
        config.save();
    }

    @Command(desc = "Deletes a custom chat command.")
    public void delete(CommandSource context, @Complete(CustomCommandCompleter.class)String name, @Flag boolean global)
    {
        if (config.commands.containsKey(name))
        {
            config.commands.remove(name.toLowerCase(ENGLISH));
            config.save();

            i18n.sendTranslated(context, POSITIVE, "Custom command {input} has successfully been deleted.", "!" + name);
        }
        else
        {
            i18n.sendTranslated(context, NEGATIVE, "Custom command {input} has not been found.", "!" + name);
        }
    }


    @Command(name = "help", desc = "Prints out all the custom chat commands.")
    public CommandResult showHelp(CommandContext context)
    {
        return new PaginatedResult(context, new CustomCommandIterator());
    }

    private class CustomCommandIterator implements PaginationIterator
    {

        @Override
        public List<String> getPage(int page, int numberOfLines)
        {
            int counter = 0;
            int commandsSize = config.commands.size();
            int offset = page * numberOfLines;

            ArrayList<String> lines = new ArrayList<>();

            if (offset < commandsSize)
            {
                int lastItem = Math.min(offset + numberOfLines, commandsSize);

                for (Entry<String, String> entry : config.commands.entrySet())
                {
                    if (counter < offset)
                    {
                        counter++;
                        continue;
                    }
                    else if (counter > lastItem)
                    {
                        return lines;
                    }

                    lines.add("!" + entry.getKey() + " -> " + entry.getValue());
                }
            }
            return lines;
        }

        @Override
        public int pageCount(int numberOfLinesPerPage)
        {
            return (int) Math.ceil((float) config.commands.size() / (float) numberOfLinesPerPage);
        }
    }

    public static class CustomCommandCompleter implements Completer
    {
        private Customcommands module;

        public CustomCommandCompleter(Customcommands module)
        {
            this.module = module;
        }

        @Override
        public List<String> getSuggestions(CommandInvocation invocation)
        {
            ArrayList<String> list = new ArrayList<>();
            for (String item : module.getConfig().commands.keySet())
            {
                if (item.startsWith(invocation.currentToken().toLowerCase(ENGLISH)))
                {
                    list.add(item);
                }
            }
            Collections.sort(list);
            return list;
        }
    }
}