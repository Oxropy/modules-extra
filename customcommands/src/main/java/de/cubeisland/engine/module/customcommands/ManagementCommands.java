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
package de.cubeisland.engine.module.customcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.completer.Completer;
import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Flag;
import de.cubeisland.engine.command.methodic.Flags;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.CommandContainer;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ModuleProvider;
import de.cubeisland.engine.core.command.result.paginated.PaginatedResult;
import de.cubeisland.engine.core.command.result.paginated.PaginationIterator;

import static de.cubeisland.engine.command.parameter.Parameter.INFINITE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static java.util.Locale.ENGLISH;

@Command(name = "customcommands", desc = "Commands to modify custom commands.")
public class ManagementCommands extends CommandContainer
{
    private final Customcommands module;
    private final CustomCommandsConfig config;

    public ManagementCommands(Customcommands module)
    {
        super(module);
        this.module = module;
        this.config = module.getConfig();
    }

    @Command(desc = "Adds a custom chat command.")
    @Params(positional = {@Param(label = "name"),
                          @Param(label = "message", greed = INFINITE)})
    @Flags({@Flag(name = "force"),
            @Flag(name = "global")})
    public void add(CommandContext context)
    {
        String name = context.get(0);
        String message = context.getStrings(1);

        if (config.commands.containsKey(name))
        {
            if (context.hasFlag("force"))
            {
                config.commands.put(name, message);
                context.sendTranslated(POSITIVE, "Custom command {input} has successfully been replaced.", "!" + name);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "Custom command {input} already exists. Set the flag {text:-force} if you want to replace the message.", "!" + name);
                return;
            }
        }
        else
        {
            config.commands.put(name.toLowerCase(ENGLISH), message);
            context.sendTranslated(POSITIVE, "Custom command {input} has successfully been added.", "!" + name);
        }
        config.save();
    }

    @Command(desc = "Deletes a custom chat command.")
    @Params(positional = @Param(label = "name", completer = CustomCommandCompleter.class))
    @Flags(@Flag(name = "global"))
    public void delete(CommandContext context)
    {
        String name = context.get(0);

        if (config.commands.containsKey(name))
        {
            config.commands.remove(name.toLowerCase(ENGLISH));
            config.save();

            context.sendTranslated(POSITIVE, "Custom command {input} has successfully been deleted.", "!" + name);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "Custom command {input} has not been found.", "!" + name);
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
        @Override
        public List<String> getSuggestions(CommandInvocation invocation)
        {
            ArrayList<String> list = new ArrayList<>();
            for (String item : ((Customcommands)invocation.valueFor(ModuleProvider.class)).getConfig().commands.keySet()) // TODO instead pass module via constuctor and register as default for readertype
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
