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
package org.cubeengine.module.backpack;

import org.cubeengine.butler.alias.Alias;
import org.cubeengine.butler.filter.Restricted;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.Complete;
import org.cubeengine.butler.parametric.Default;
import org.cubeengine.butler.parametric.Flag;
import org.cubeengine.butler.parametric.Named;
import org.cubeengine.butler.parametric.Optional;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.command.ContainerCommand;
import org.cubeengine.libcube.service.command.annotation.ParameterPermission;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.Tristate;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

@Command(name = "backpack", desc = "The Backpack commands", alias = "bp")
public class BackpackCommands extends ContainerCommand
{
    private final Backpack module;
    private final BackpackManager manager;
    private I18n i18n;

    public BackpackCommands(Backpack module, BackpackManager manager, I18n i18n, CommandManager base)
    {
        super(base, Backpack.class);
        this.module = module;
        this.manager = manager;
        this.i18n = i18n;
    }

    @Alias(value = "openbp")
    @Command(desc = "opens a backpack")
    @Restricted(value = Player.class, msg = "You cannot open a inventory in console!")
    public void open(Player ctx, @Complete(BackpackCompleter.class) @Optional String name, @Default User player,
                     @ParameterPermission(value = "other-context", desc = "Allows using the open command in another context")
                     @Flag boolean outOfContext)
    {
        if (name == null)
        {
            name = player.getName();
        }
        if (ctx != player && !ctx.hasPermission(module.perms().COMMAND_OPEN_OTHER_PLAYER.getId()))
        {
            i18n.send(ctx, NEGATIVE, "You are not allowed to open the backpacks of other users!");
            return;
        }
        manager.openBackpack(ctx, player, outOfContext, name) ;
    }

    @Alias(value = "createbp")
    @Command(desc = "creates a new backpack")
    public void create(CommandSource ctx,
                       @Default @Optional User player,
                       @Optional String name,
                       @Flag boolean blockinput)
    {
        if (name == null)
        {
            name = player.getName();
        }
        else if (!name.equals(player.getName()))
        {
            if (!ctx.hasPermission(module.perms().COMMAND_CREATE_NAMED.getId()))
            {
                i18n.send(ctx, NEGATIVE, "You are not allowed to create named backpacks");
                return;
            }
        }
        if (ctx != player)
        {
            if (!ctx.hasPermission(module.perms().COMMAND_CREATE_OTHER.getId()))
            {
                i18n.send(ctx, NEGATIVE, "You are not allowed to create backpacks for others");
                return;
            }
        }
        manager.createBackpack(ctx, player, name, blockinput);
    }

    @Alias(value = "blockbp")
    @Command(desc = "modifies a backpack")
    public void blockinput(CommandSource ctx, @Complete(BackpackCompleter.class) String name, @Default User player, boolean blockinput)
    {
        manager.modifyBackpack(ctx, player, name, blockinput);
    }

    @Command(desc = "modifies a backpacks context")
    public void allowIn(CommandSource ctx, @Complete(BackpackCompleter.class) String name, @Named("for") @Default User player, Context context)
    {
        manager.setBackpackContext(ctx, player, name, context, Tristate.TRUE);
    }

    @Command(desc = "modifies a backpacks context")
    public void denyIn(CommandSource ctx, @Complete(BackpackCompleter.class) String name, @Named("for") @Default User player, Context context, @Flag boolean reset)
    {
        manager.setBackpackContext(ctx, player, name, context, reset ? Tristate.UNDEFINED : Tristate.FALSE);
    }

    /* TODO give cmd
    @Alias(value = "givebp")
    @Command(desc = "Puts items into a backpack")
    // /givebp premium Faithcaio item diamondpick:1500 name "broken pick" lore "A broken\npick" "ench unbreaking:1,effi:3"
    public void give(CommandContext context, String name, @Default User player,
                     @Named({"item","i"}) @Label("item[:data]") String itemString, // TODO Required flag // TODO group parameter for ItemMeta
                     @Named({"name","n"}) @Label("name") String displayName,
                     @Named({"lore","l"}) @Label("lorelines...") String lore,
                     @Named({"amount","a"}) Integer amount,
                     @Named({"ench", "enchantments","e"}) @Label("enchs...") String enchantments,
                     @Named({"w", "world", "for", "in"}) World world)
    {
        if (world == null && (context.getSource() instanceof User))
        {
            world = ((User)context.getSource()).getWorld();
        }
        ItemStack matchedItem = Match.material().itemStack(itemString);
        if (matchedItem == null)
        {
            context.sendTranslated(NEGATIVE, "Item {input#name} not found!", itemString);
            return;
        }
        ItemMeta itemMeta = matchedItem.getItemMeta();
        if (displayName != null)
        {
            itemMeta.setDisplayName(ChatFormat.parseFormats(displayName));
        }
        if (lore != null)
        {
            itemMeta.setLore(asList(StringUtils.explode("\\n", ChatFormat.parseFormats(lore))));
        }
        if (enchantments != null)
        {
            String[] enchs = StringUtils.explode(",", enchantments);
            for (String ench : enchs)
            {
                Enchantment enchantment;
                int power;
                if (ench.contains(":"))
                {
                    enchantment = Match.enchant().enchantment(ench.substring(0, ench.indexOf(":")));
                    if (enchantment == null)
                    {
                        context.sendTranslated(NEGATIVE, "Unknown Enchantment {input#enchant}", ench);
                        return;
                    }
                    power = Integer.parseInt(ench.substring(ench.indexOf(":")+1));
                }
                else
                {
                    enchantment = Match.enchant().enchantment(ench);
                    if (enchantment == null)
                    {
                        context.sendTranslated(NEGATIVE, "Unknown Enchantment {input#enchant}", ench);
                        return;
                    }
                    power = enchantment.getMaxLevel();
                }
                itemMeta.addEnchant(enchantment, power, true);
            }
        }
        matchedItem.setItemMeta(itemMeta);
        if (amount == null)
        {
            amount = matchedItem.getMaxStackSize();
        }
        matchedItem.setAmount(amount);
        this.manager.giveItem(context.getSource(), player, name, matchedItem);
    }
    */
}
