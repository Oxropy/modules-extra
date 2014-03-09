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
package de.cubeisland.engine.core.util.formatter;

import java.util.Set;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.formatter.context.MacroContext;
import de.cubeisland.engine.formatter.formatter.ConstantMacro;

import static de.cubeisland.engine.formatter.formatter.AbstractFormatter.toSet;

public class TextMacro implements ConstantMacro
{
    private final Set<String> names;

    public TextMacro()
    {
        this.names = toSet("text");
    }

    @Override
    public String process(MacroContext macroContext)
    {
        ChatFormat color = macroContext.readMapped("color", ChatFormat.class);
        if (color == null)
        {
            color = ChatFormat.GOLD;
        }
        return color + macroContext.getArg(0);
    }

    @Override
    public Set<String> names()
    {
        return this.names;
    }
}
