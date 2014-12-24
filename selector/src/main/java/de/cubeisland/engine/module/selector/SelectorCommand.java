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
package de.cubeisland.engine.module.selector;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.module.selector.CuboidSelector.SELECTOR_TOOL_NAME;
import static java.util.Arrays.asList;
import static org.bukkit.Material.WOOD_AXE;

public class SelectorCommand
{
    // TODO add //wand alias when WE is not found

    @SuppressWarnings("deprecation")
    public static void giveSelectionTool(User user)
    {
        ItemStack found = null;
        for (ItemStack item : user.getInventory().getContents())
        {
            if (item != null && item.getType() == WOOD_AXE && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(SELECTOR_TOOL_NAME))
            {
                found = item;
                break;
            }
        }
        if (found == null)
        {
            found = new ItemStack(WOOD_AXE, 1);
            ItemMeta meta = found.getItemMeta();
            meta.setDisplayName(SELECTOR_TOOL_NAME);
            meta.setLore(asList("created by " + user.getDisplayName()));
            found.setItemMeta(meta);
            ItemStack oldItemInHand = user.getItemInHand();
            user.setItemInHand(found);
            HashMap<Integer, ItemStack> tooMuch = user.getInventory().addItem(oldItemInHand);
            for (ItemStack item : tooMuch.values())
            {
                user.getWorld().dropItemNaturally(user.getLocation(), item);
            }
            user.updateInventory();
            user.sendTranslated(POSITIVE, "Received a new region selector tool");
            return;
        }
        user.getInventory().removeItem(found);
        ItemStack oldItemInHand = user.getItemInHand();
        user.setItemInHand(found);
        user.getInventory().addItem(oldItemInHand);
        user.updateInventory();
        user.sendTranslated(POSITIVE, "Found a region selector tool in your inventory!");
    }

    @Command(desc = "Provides you with a wand to select a cuboid")
    public CommandResult selectiontool(CommandContext context)
    {
        if (context.getSource() instanceof User)
        {
            giveSelectionTool((User)context.getSource());
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You cannot hold a selection tool!");
        }
        return null;
    }
}
