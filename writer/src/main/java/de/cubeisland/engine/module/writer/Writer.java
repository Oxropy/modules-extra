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
package de.cubeisland.engine.module.writer;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * A module to edit signs and/or unsign written books
 */
public class Writer extends Module
{
    @Override
    public void onEnable()
    {
        this.getCore().getCommandManager().addCommands(this.getCore().getCommandManager(), this, this);
    }

    @Command(alias = "rewrite", desc = "Edit a sign or unsign a book")
    @Params(nonpositional = {@Param(names ={"1", "Line1"}, label = "1st line"),
                             @Param(names ={"2", "Line2"}, label = "2nd line"),
                             @Param(names ={"3", "Line3"}, label = "3rd line"),
                             @Param(names ={"4", "Line4"}, label = "4th line")})
    public void edit(CommandContext context)
    {
        if (!(context.getSource() instanceof User))
        {
            context.sendTranslated(NEGATIVE, "Edit what?");
            return;
        }
        User user = (User)context.getSource();
        if (!this.unsignBook(user))
        {
            Map<String, String> params = context.getRawNamed();
            if (params.size() < 1)
            {
                context.sendTranslated(NEGATIVE, "You need to specify at least one parameter to edit a sign!");
                context.sendTranslated(NEGATIVE, "Or hold a signed book in your hand to edit it.");
                return;
            }
            if (!this.editSignInSight(user, params))
            {
                user.sendTranslated(NEGATIVE, "You need to have a signed book in hand or be looking at a sign less than 10 blocks away!");
            }
        }
    }

    /**
     * Edits the sign the user is looking at
     *
     * @param user the user
     * @param params the parameters (only 1-4 are allowed as key)
     * @return false of there is no sign
     *
     * @throws java.lang.NumberFormatException when the parameter keys are not numbers
     * @throws java.lang.ArrayIndexOutOfBoundsException when the parameter keys are other numbers than 1-4
     */
    public boolean editSignInSight(User user, Map<String, String> params)
    {
        Block target = user.getTargetBlock(null, 10);
        if (target.getType() == Material.WALL_SIGN || target.getType() == Material.SIGN_POST)
        {
            Sign sign = (Sign)target.getState();
            String[] lines = sign.getLines();
            for (Entry<String, String> entry : params.entrySet())
            {
                lines[Integer.parseInt(entry.getKey()) - 1] = entry.getValue();
            }
            SignChangeEvent event = new SignChangeEvent(sign.getBlock(), user, sign.getLines());
            user.getCore().getEventManager().fireEvent(event);
            if (event.isCancelled())
            {
                user.sendTranslated(NEGATIVE, "Could not change the sign!");
                return true;
            }
            for (int i = 0; i < 4; ++i)
            {
                sign.setLine(i, lines[i]);
            }
            sign.update();
            user.sendTranslated(POSITIVE, "The sign has been changed!");
            return true;
        }
        // No Sign in sight
        return false;
    }

    /**
     * Unsigns a written book in the hand of given user
     *
     * @param user the user
     * @return false if there is no written book in the hand of given user
     */
    public boolean unsignBook(User user)
    {
        if (user.getItemInHand().getType() == Material.WRITTEN_BOOK)
        {
            ItemStack item = user.getItemInHand();
            BookMeta meta = ((BookMeta)item.getItemMeta());
            meta.setAuthor("");
            meta.setTitle("");
            item.setItemMeta(meta);
            item.setType(Material.BOOK_AND_QUILL);
            user.sendTranslated(POSITIVE, "Your book is now unsigned and ready to be edited.");
            return true;
        }
        return false;
    }
}
