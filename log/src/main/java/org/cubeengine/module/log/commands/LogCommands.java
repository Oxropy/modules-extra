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
package org.cubeengine.module.log.commands;

import java.util.Arrays;
import java.util.HashMap;
import org.cubeengine.butler.alias.Alias;
import org.cubeengine.butler.filter.Restricted;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.Label;
import org.cubeengine.butler.parametric.Optional;
import org.cubeengine.libcube.service.command.ContainerCommand;
import org.cubeengine.libcube.service.command.CommandContext;
import org.cubeengine.libcube.service.command.CommandSender;
import org.cubeengine.libcube.service.user.User;
import de.cubeisland.engine.module.core.util.matcher.Match;
import org.cubeengine.module.log.Log;
import org.cubeengine.module.log.LogAttachment;
import org.bukkit.Material;
import org.spongepowered.api.item.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.WOOD_AXE;

@Command(name = "log", desc = "log-module commands.")
public class LogCommands extends ContainerCommand
{
    public static final Text toolName = Texts.of(TextColors.DARK_AQUA, "Logging-ToolBlock");
    public static final Text selectorToolName = Texts.of(TextColors.DARK_AQUA, "Selector-Tool");

    private final Log module;

    public LogCommands(Log module)
    {
        super(module);
        this.module = module;
    }

    @SuppressWarnings("deprecation")
    public static void giveSelectionTool(User user)
    {
        ItemStack found = null;
        for (ItemStack item : user.getInventory().getContents())
        {
            if (item != null && item.getType() == WOOD_AXE
                && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(selectorToolName))
            {
                found = item;
                break;
            }
        }
        if (found == null)
        {
            found = new ItemStack(WOOD_AXE, 1);
            ItemMeta meta = found.getItemMeta();
            meta.setDisplayName(selectorToolName);
            meta.setLore(Arrays.asList("created by " + user.getDisplayName()));
            found.setItemMeta(meta);
            ItemStack oldItemInHand = user.getItemInHand();
            user.setItemInHand(found);
            HashMap<Integer, ItemStack> tooMuch = user.getInventory().addItem(oldItemInHand);
            for (ItemStack item : tooMuch.values())
            {
                user.getWorld().dropItemNaturally(user.getLocation(), item);
            }
            user.updateInventory();
            user.sendTranslated(POSITIVE, "Received a new Region-Selector Tool");
            return;
        }
        user.getInventory().removeItem(found);
        ItemStack oldItemInHand = user.getItemInHand();
        user.setItemInHand(found);
        user.getInventory().addItem(oldItemInHand);
        user.updateInventory();
        user.sendTranslated(POSITIVE, "Found a Region-Selector Tool in your inventory!");
    }


    // TODO command to show current params on a lookup-tool
    // TODO command to change params on a lookup-tool (only further limiting)

    //TODO add rollback tool
    //TODO loghand (cmd hand) -> toggles general lookup with bare hands

    @Command(desc = "Shows the current queue-size.")
    public void queuesize(CommandSender context)
    {
        int size = module.getLogManager().getQueueSize();
        if (size == 0)
        {
            context.sendTranslated(POSITIVE, "Logging-queue is currently empty!");
        }
        else
        {
            context.sendTranslated(POSITIVE, "{integer} logs are currently queued!", size);
        }
        this.module.getLogManager().getQueryManager().logStatus();
    }

    private Material matchType(String type, boolean block)// or item
    {
        if (type == null)
        {
            if (block)
            {
                return Material.BEDROCK;
            }
            return Material.BOOK;
        }
        String match = Match.string().matchString(type, "chest", "player", "kills", "block");
        if (match == null)
        {
            return null;
        }
        switch (match)
        {
            case "chest":
            case "container":
                if (block)
                {
                    return Material.CHEST;
                }
                return Material.CLAY_BRICK;
            case "player":
                if (block)
                {
                    return Material.PUMPKIN;
                }
                return Material.CLAY_BALL;
            case "kills":
                if (block)
                {
                    return Material.SOUL_SAND;
                }
                return Material.BONE;
            case "block":
                if (block)
                {
                    return Material.LOG;
                }
                return Material.NETHER_BRICK_ITEM;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private void findLogTool(User user, Material material)
    {
        ItemStack found = null;
        for (ItemStack item : user.getInventory().getContents())
        {
            if (item != null && item.getType().equals(material) && item.hasItemMeta() && item.getItemMeta()
                                                                                             .hasDisplayName() && item
                .getItemMeta().getDisplayName().equals(toolName))
            {
                found = item;
                break;
            }
        }
        if (found == null)
        {
            found = new ItemStack(material, 1);
            ItemMeta meta = found.getItemMeta();
            meta.setDisplayName(toolName);
            meta.setLore(Arrays.asList("created by " + user.getDisplayName()));
            found.setItemMeta(meta);
            ItemStack oldItemInHand = user.getItemInHand();
            user.setItemInHand(found);
            HashMap<Integer, ItemStack> tooMuch = user.getInventory().addItem(oldItemInHand);
            for (ItemStack item : tooMuch.values())
            {
                user.getWorld().dropItemNaturally(user.getLocation(), item);
            }
            user.updateInventory();
            user.sendTranslated(POSITIVE, "Received a new Log-Tool!");
            LogAttachment logAttachment = user.attachOrGet(LogAttachment.class, this.module);
            logAttachment.createNewLookup(material);

            return;
        }
        user.getInventory().removeItem(found);
        ItemStack oldItemInHand = user.getItemInHand();
        user.setItemInHand(found);
        user.getInventory().addItem(oldItemInHand);
        user.updateInventory();
        user.sendTranslated(POSITIVE, "Found a Log-Tool in your inventory!");
    }

    @Alias(value = "lb")
    @Command(desc = "Gives you a block to check logs with." +
        "no log-type: Shows everything\n" +
        "chest: Shows chest-interactions only\n" +
        "player: Shows player-interacions only\n" +
        "kills: Shows kill-interactions only\n" +
        "block: Shows block-changes only")
    @Restricted(value = User.class, msg = "Why don't you check in your log-file? You won't need a block there!")
    public void block(User context, @Optional @Label("log-type") String logType)
    {
        //TODO tabcompleter for logBlockTypes (waiting for CE-389)
        Material blockMaterial = this.matchType(logType, true);
        if (blockMaterial == null)
        {
            context.sendTranslated(NEGATIVE, "{input} is not a valid log-type. Use chest, container, player, block or kills instead!", logType);
            return;
        }
        this.findLogTool(context, blockMaterial);
    }

    @Alias(value = "lt")
    @Command(desc = "Gives you an item to check logs with.\n" +
        "no log-type: Shows everything\n" +
        "chest: Shows chest-interactions only\n" +
        "player: Shows player-interacions only\n" +
        "kills: Shows kill-interactions only\n" +
        "block: Shows block-changes only")
    @Restricted(value = User.class, msg = "Why don't you check in your log-file? You won't need a block there!")
    public void tool(User context, @Optional @Label("log-type") String logType)
    {
        //TODO tabcompleter for logToolTypes (waiting for CE-389)
        Material blockMaterial = this.matchType(logType, false);
        if (blockMaterial == null)
        {
            context.sendTranslated(NEGATIVE, "{input} is not a valid log-type. Use chest, container, player, block or kills instead!", logType);
            return;
        }
        this.findLogTool(context, blockMaterial);
    }

    @Command(desc = "Gives you a item to select a region with.")
    public void selectionTool(CommandContext context)
    {
        if (context.getSource() instanceof User)
        {
            giveSelectionTool((User)context.getSource());
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You cannot hold a selection tool!");
        }
        // if worldEdit give WE wand else give OUR wand
    }
}
