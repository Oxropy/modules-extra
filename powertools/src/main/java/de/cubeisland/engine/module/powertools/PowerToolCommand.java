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
package de.cubeisland.engine.module.powertools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Flag;
import de.cubeisland.engine.command.methodic.Flags;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.core.command.CommandContainer;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.command.alias.Alias;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.command.parameter.Parameter.INFINITE;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

/**
 * The PowerTool commands allow binding commands and/or chat-macros to a specific item.
 * <p>The data is appended onto the items lore
 * <p>/powertool
 */
@Command(name = "powertool", desc = "Binding shortcuts to an item.", alias = "pt")
public class PowerToolCommand extends CommandContainer implements Listener
{
    private final Powertools module;

    public PowerToolCommand(Powertools module)
    {
        super(module);
        this.module = module;
        /* TODO delegation
        this.delegateChild(new DelegatingContextFilter()
        {
            @Override
            public String delegateTo(CommandContext context)
            {
                return context.hasPositional(0) ? "add" : "clear";
            }

            @Override
            public CommandContext filterContext(CommandContext context, String child)
            {
                Set<String> flagSet = context.getFlags();
                if (child.equals("add"))
                {
                    flagSet.add("r");
                }
                // TODO read context before passing
                return new CommandContext(context.getRawArgs(), context.getRawIndexed(), context.getRawNamed(), flagSet, context.getCommand(), context.getLabels(), context.getSource());
            }
        });
         */
    }

    @Alias(value = "ptc")
    @Command(desc = "Removes all commands from your powertool")
    @Flags(@Flag(longName = "all", name = "a"))
    public void clear(CommandContext context)
    {
        CommandSender sender = context.getSource();
        if (sender instanceof User)
        {
            User user = (User)sender;
            if (context.hasFlag("a"))
            {
                for (ItemStack item : user.getInventory().getContents())
                {
                    this.setPowerTool(item, null);
                }
                context.sendTranslated(POSITIVE, "Removed all commands bound to items in your inventory!");
            }
            else
            {
                if (user.getItemInHand().getTypeId() == 0)
                {
                    context.sendTranslated(NEUTRAL, "You are not holding any item in your hand.");
                    return;
                }
                this.setPowerTool(user.getItemInHand(), null);
                context.sendTranslated(POSITIVE, "Removed all commands bound to the item in your hand!");
            }
            return;
        }
        context.sendTranslated(NEUTRAL, "No more power for you!");
    }

    @Alias(value = "ptr")
    @Command(alias = {"del", "delete", "rm"}, desc = "Removes a command from your powertool")
    @Params(positional = @Param(req = false, label = "command", greed = INFINITE))
    @Flags(@Flag(longName = "chat", name = "c"))
    public void remove(CommandContext context)
    {
        if (context.getSource() instanceof User)
        {
            User sender = (User)context.getSource();
            if (sender.getItemInHand().getTypeId() == 0)
            {
                context.sendTranslated(NEUTRAL, "You are not holding any item in your hand.");
                return;
            }
            String cmd = context.getStrings(0);
            this.remove(context, sender.getItemInHand(), cmd, !context.hasFlag("c"));
            return;
        }
        context.sendTranslated(NEUTRAL, "No more power for you!");
    }

    private void remove(CommandContext context, ItemStack item, String cmd, boolean isCommand)
    {
        List<String> powertools = this.getPowerTools(item);
        if (cmd == null || cmd.isEmpty())
        {
            powertools.remove(powertools.size() - 1);
            this.setPowerTool(item, powertools);
            context.sendTranslated(POSITIVE, "Removed the last command bound to this item!");
        }
        else
        {
            if (isCommand)
            {
                cmd = "/" + cmd;
            }
            boolean removed = false;
            while (powertools.remove(cmd)) // removes also multiple same cmds
            {
                removed = true;
            }
            if (removed)
            {
                context.sendTranslated(POSITIVE, "Removed the command: {input#command} bound to this item!", cmd);
            }
            else
            {
                context.sendTranslated(NEGATIVE, "The command {input#command} was not found on this item!", cmd);
            }
        }
        this.setPowerTool(item, powertools);
        if (powertools.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "No more commands saved on this item!");
            return;
        }
        this.showPowerToolList(context, powertools, false, false);
    }

    @Alias(value = "pta")
    @Command(desc = "Adds a command to your powertool")
    @Params(positional = @Param(label = "commandstring", greed = INFINITE))
    @Flags({@Flag(longName = "chat", name = "c"),
           @Flag(longName = "replace", name = "r")})
    public void add(CommandContext context)
    {
        CommandSender sender = context.getSource();
        if (sender instanceof User)
        {
            User user = (User)sender;
            String cmd = context.getStrings(0);
            if (user.getItemInHand().getType().equals(Material.AIR))
            {
                user.sendTranslated(NEUTRAL, "You do not have an item in your hand to bind the command to!");
                return;
            }
            if (!context.hasFlag("c"))
            {
                cmd = "/" + cmd;
            }
            List<String> powerTools;
            if (context.hasFlag("r"))
            {
                powerTools = new ArrayList<>(1);
            }
            else
            {
                powerTools = this.getPowerTools(user.getItemInHand());
            }
            powerTools.add(cmd);
            this.setPowerTool(user.getItemInHand(), powerTools);
            return;
        }
        context.sendTranslated(NEUTRAL, "You already have enough power!");
    }

    @Alias(value = "ptl")
    @Command(desc = "Lists your powertool-bindings.")
    @Flags(@Flag(longName = "all", name = "a"))
    public void list(CommandContext context)
    {
        if (context.getSource() instanceof User)
        {
            User sender = (User)context.getSource();
            if (context.hasFlag("a"))
            {
                for (ItemStack item : sender.getInventory().getContents())
                {
                    String itemName = item.getItemMeta().getDisplayName();
                    if (itemName == null)
                    {
                        sender.sendMessage(ChatFormat.GOLD + Match.material().getNameFor(item) + ChatFormat.GOLD + ":");
                    }
                    else
                    {
                        sender.sendMessage(ChatFormat.GOLD + itemName + ChatFormat.GOLD + ":");
                    }
                    this.showPowerToolList(context, this.getPowerTools(item), false, false);
                }
                return;
            }
            if (sender.getItemInHand().getType().equals(Material.AIR))
            {
                context.sendTranslated(NEUTRAL, "You do not have an item in your hand.");
            }
            else
            {
                this.showPowerToolList(context, this.getPowerTools(sender.getItemInHand()), false, true);
            }
            return;
        }
        context.sendTranslated(NEUTRAL, "You already have enough power!");
    }

    private void showPowerToolList(CommandContext context, List<String> powertools, boolean lastAsNew, boolean showIfEmpty)
    {
        if ((powertools == null || powertools.isEmpty()))
        {
            if (showIfEmpty)
            {
                context.sendTranslated(NEGATIVE, "No commands saved on this item!");
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < powertools.size() - 1; i++)
        {
            sb.append("\n").append(ChatFormat.WHITE).append(powertools.get(i));
        }
        if (lastAsNew)
        {
            context.sendTranslated(NEUTRAL, "{amount} command(s) bound to this item:{}", i + 1, sb.toString());
            context.sendMessage(ChatFormat.YELLOW + powertools.get(i) + ChatFormat.GOLD + "NEW"); // TODO translate
        }
        else
        {
            context.sendTranslated(NEUTRAL, "{amount} command(s) bound to this item:{}", i + 1, sb.toString());
            context.sendMessage(powertools.get(i));
        }
    }

    private void setPowerTool(ItemStack item, List<String> newPowerTools)
    {
        ItemMeta meta = item.getItemMeta();
        List<String> newLore = new ArrayList<>();
        if (meta.hasLore())
        {
            for (String line : meta.getLore())
            {
                if (line.equals(ChatFormat.DARK_GREEN + "PowerTool"))
                {
                    break;
                }
                newLore.add(line);
            }
        }
        if (newPowerTools != null && !newPowerTools.isEmpty())
        {
            newLore.add(ChatFormat.DARK_GREEN + "PowerTool");
            newLore.addAll(newPowerTools);
        }
        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    /**
     * Gets the PowerTools saved on this item.
     *
     * @param item
     * @return a list of the saved commands and/or chat-macros
     */
    private List<String> getPowerTools(ItemStack item)
    {
        ItemMeta meta = item.getItemMeta();
        List<String> powerTool = new ArrayList<>();
        if (meta.hasLore())
        {
            boolean ptStart = false;
            for (String line : meta.getLore())
            {
                if (!ptStart && line.equals("§2PowerTool"))
                {
                    ptStart = true;
                }
                else if (ptStart)
                {
                    powerTool.add(line);
                }
            }
        }
        return powerTool;
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            Player player = event.getPlayer();
            if (!player.getItemInHand().getType().equals(Material.AIR)
                    && module.perms().POWERTOOL_USE.isAuthorized(event.getPlayer()))
            {
                List<String> powerTool = this.getPowerTools(player.getItemInHand());
                for (String command : powerTool)
                {
                    player.chat(command);
                }
                if (!powerTool.isEmpty())
                {
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
        }
    }
}
