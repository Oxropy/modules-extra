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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.completer.Completer;
import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Flag;
import de.cubeisland.engine.command.methodic.Flags;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.conversation.ConversationCommand;
import de.cubeisland.engine.core.command.completer.ItemCompleter;
import de.cubeisland.engine.core.user.User;
import gnu.trove.map.hash.TLongObjectHashMap;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static org.bukkit.event.Event.Result.DENY;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

@Command(name = "MarketSign", desc = "Edit Signmarket signs")
public class EditModeListener extends ConversationCommand
{
    private final MarketSignFactory signFactory;
    private final Signmarket module;
    private final TLongObjectHashMap<Location> currentSignLocation = new TLongObjectHashMap<>();
    private final TLongObjectHashMap<MarketSign> previousMarketSign = new TLongObjectHashMap<>();

    public EditModeListener(final Signmarket module)
    {
        super(module);
        this.module = module;
        this.signFactory = module.getMarketSignFactory();
    }

    private boolean setEditingSign(User user, MarketSign marketSign)
    {
        if (marketSign == null) return true;
        Location previous = this.currentSignLocation.put(user.getId(), marketSign.getLocation());
        if (!marketSign.getLocation().equals(previous))
        {
            MarketSign previousSign = this.signFactory.getSignAt(previous);
            if (previousSign != null)
            {
                this.previousMarketSign.put(user.getId(), previousSign);
                previousSign.exitEditMode(user);
            }
            if (!checkAllowedEditing(marketSign, user)) return true;
            marketSign.enterEditMode();
            user.sendTranslated(POSITIVE, "Changed active sign!");
            return true;
        }
        if (!checkAllowedEditing(marketSign, user)) return true;
        marketSign.enterEditMode();
        return false;
    }

    private boolean checkAllowedEditing(MarketSign marketSign, User user)
    {
        if (marketSign.isAdminSign() && !module.perms().SIGN_CREATE_ADMIN_CREATE.isAuthorized(user))
        {
            user.sendTranslated(NEGATIVE, "You are not allowed to edit admin signs!");
            this.currentSignLocation.remove(user.getId());
            return false;
        }
        else if (!marketSign.isAdminSign() && !module.perms().SIGN_CREATE_USER_CREATE.isAuthorized(user))
        {
            user.sendTranslated(NEGATIVE, "You are not allowed to edit player signs!");
            this.currentSignLocation.remove(user.getId());
            return false;
        }
        if (!marketSign.isAdminSign() && !marketSign.isOwner(user) && !module.perms().SIGN_CREATE_USER_OTHER.isAuthorized(user))
        {
            user.sendTranslated(NEGATIVE, "You are not allowed to edit Signs of other players!");
            this.currentSignLocation.remove(user.getId());
            return false;
        }
        return true;
    }

    @Override
    public void removeUser(User user)
    {
        super.removeUser(user);
        user.sendTranslated(POSITIVE, "Exiting edit mode.");
    }

    @EventHandler
    public void changeWorld(PlayerChangedWorldEvent event)
    {
        if (this.module.getConfig().disableInWorlds.contains(event.getPlayer().getWorld().getName()))
        {
            User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            if (this.hasUser(user))
            {
                user.sendTranslated(NEUTRAL, "MarketSigns are disabled in the configuration for this world!");
                this.removeUser(user);
                this.currentSignLocation.remove(user.getId());
            }
        }
    }

    /* new ConversationContextFactory(
            new CtxBuilder()
                .addFlag(new PermissibleFlag("exit", "exit"))
                .addFlag(new PermissibleFlag("copy", "copy"))
                .addFlag(new PermissibleFlag("buy", "buy"))
                .addFlag(new PermissibleFlag("sell", "sell"))
                .addFlag(new PermissibleFlag("admin", "admin"))
                .addFlag(new PermissibleFlag("user", "user"))
                .addFlag(new PermissibleFlag("stock", "stock"))
                .addFlag(new PermissibleFlag("nodemand", "nodemand"))
                .addNamed(new PermissibleNamedParameter("demand", "demand", Integer.class))
                .addNamed(new PermissibleNamedParameter("owner", "owner", User.class).withCompleter(new PlayerCompleter()))
                .addNamed(new PermissibleNamedParameter("price", "price", String.class))
                .addNamed(new PermissibleNamedParameter("amount", "amount", Integer.class))
                .addNamed(new PermissibleNamedParameter("item", "item", ItemStack.class).withCompleter(new ItemCompleter()))
                .addNamed(new PermissibleNamedParameter("setstock", "setstock", Integer.class))
                .addNamed(new PermissibleNamedParameter("size", "size", Integer.class).withCompleter(new Completer<CubeContext>()
                {
                })).get()
        ));*/
    @Flags({@Flag(name = "exit"),
            @Flag(name = "copy"),
            @Flag(name = "buy"),
            @Flag(name = "sell"),
            @Flag(name = "admin"),
            @Flag(name = "user"),
            @Flag(name = "stock"),
            @Flag(name = "nodemand")})
    @Params(nonpositional = {@Param(names = "demand", label = "demand", type = Integer.class),
                             @Param(names = "owner", label = "owner", type = User.class),
                             @Param(names = "price", label = "price", type = String.class),
                             @Param(names = "amount", label = "amount", type = Integer.class),
                             @Param(names = "item", label = "item", type = ItemStack.class, completer = ItemCompleter.class),
                             @Param(names = "setstock", label = "setstock", type = Integer.class),
                             @Param(names = "size", label = "size", type = Integer.class, completer = SignSizeCompleter.class)})
    public CommandResult run(CommandContext context)
    {
        User user = (User)context.getSource();
        Location loc = this.currentSignLocation.get(user.getId());
        if (loc == null)
        {
            if (context.hasFlag("exit"))
            {
                this.removeUser(user);
                return null;
            }
            user.sendTranslated(NEGATIVE, "Please select a sign to edit.");
            return null;
        }
        MarketSign marketSign = this.signFactory.getSignAt(loc);
        if (marketSign == null)
        {
            user.sendTranslated(CRITICAL, "No market sign at position! This should not happen!");
            return null;
        }
        this.setEditingSign(user, marketSign);
        if (context.hasFlag("copy"))
        {
            MarketSign prevMarketSign = this.previousMarketSign.get(user.getId());
            if (prevMarketSign == null)
            {
                user.sendTranslated(NEGATIVE, "No market sign at previous position.");
                return null;
            }
            else
            {
                if (prevMarketSign.isAdminSign() && !module.perms().SIGN_CREATE_ADMIN_CREATE.isAuthorized(user))
                {
                    user.sendTranslated(NEGATIVE, "You are not allowed to copy admin signs!");
                    return null;
                }
                else if (!prevMarketSign.isAdminSign() && !module.perms().SIGN_CREATE_USER_CREATE.isAuthorized(user))
                {
                    user.sendTranslated(NEGATIVE, "You are not allowed to copy player signs!");
                    return null;
                }
                marketSign.copyValuesFrom(prevMarketSign);
            }
        }
        if (context.hasFlag("buy"))
        {
            if (marketSign.isAdminSign())
            {
                if (module.perms().SIGN_CREATE_ADMIN_BUY.isAuthorized(user))
                {
                    marketSign.setTypeBuy();
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "You are not allowed to create admin buy signs!");
                    return null;
                }
            }
            else
            {
                if (module.perms().SIGN_CREATE_USER_BUY.isAuthorized(user))
                {
                    marketSign.setTypeBuy();
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "You are not allowed to create player buy signs!");
                    return null;
                }
            }
        }
        if (context.hasFlag("sell"))
        {
            if (marketSign.isAdminSign())
            {
                if (module.perms().SIGN_CREATE_ADMIN_SELL.isAuthorized(user))
                {
                    marketSign.setTypeSell();
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "You are not allowed to create admin sell signs!");
                    return null;
                }
            }
            else
            {
                if (module.perms().SIGN_CREATE_USER_SELL.isAuthorized(user))
                {
                    marketSign.setTypeSell();
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "You are not allowed to create player sell signs!");
                    return null;
                }
            }
        }
        if (context.hasNamed("demand"))
        {
            if (!marketSign.hasType())
            {
                marketSign.setTypeSell();
            }
            if (marketSign.isTypeBuy())
            {
                user.sendTranslated(NEGATIVE, "Buy signs cannot have a demand!");
                return null;
            }
            else if (marketSign.isAdminSign())
            {
                user.sendTranslated(NEGATIVE, "Admin signs cannot have a demand!");
                return null;
            }
            else
            {
                Integer demand = context.get("demand", null);
                if (demand == -1)
                {
                    marketSign.setNoDemand();
                }
                else if (demand != null && demand > 0)
                {
                    if (module.perms().SIGN_CREATE_USER_DEMAND.isAuthorized(user))
                    {
                        marketSign.setDemand(demand);
                    }
                    else
                    {
                        context.sendTranslated(NEGATIVE, "You are not allowed to set a demand!");
                        return null;
                    }
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "Invalid demand amount!");
                    return null;
                }
            }
        }
        if (context.hasFlag("noDemand"))
        {
            marketSign.setNoDemand();
        }
        if (context.hasFlag("admin"))
        {
            if (module.perms().SIGN_CREATE_ADMIN_CREATE.isAuthorized(user))
            {
                marketSign.setAdminSign();
                if (this.module.getConfig().maxAdminStock != -1 && (marketSign.hasInfiniteSize() || marketSign.getChestSize() > this.module.getConfig().maxAdminStock))
                {
                    marketSign.setSize(this.module.getConfig().maxAdminStock);
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to create admin signs");
                return null;
            }
        }
        if (context.hasFlag("user"))
        {
            if (module.perms().SIGN_CREATE_USER_CREATE.isAuthorized(user))
            {
                marketSign.setOwner(user);
                if (this.module.getConfig().maxUserStock != -1 && (marketSign.hasInfiniteSize() || marketSign.getChestSize() > this.module.getConfig().maxUserStock))
                {
                    marketSign.setSize(this.module.getConfig().maxUserStock);
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to create player signs");
                return null;
            }
        }
        if (context.hasNamed("owner"))
        {
            if (module.perms().SIGN_CREATE_USER_OTHER.isAuthorized(user))
            {
                User owner = context.get("owner", null);
                if (owner == null)
                {
                    user.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString("owner"));
                    return null;
                }
                else
                {
                    marketSign.setOwner(owner);
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to create player signs for other players");
                return null;
            }
        }
        if (context.hasFlag("stock"))
        {
            if (marketSign.isAdminSign())
            {
                if (marketSign.hasStock())
                {
                    if (this.module.getConfig().allowAdminNoStock)
                    {
                        if (module.perms().SIGN_CREATE_ADMIN_NOSTOCK.isAuthorized(user))
                        {
                            marketSign.setNoStock();
                        }
                        else
                        {
                            context.sendTranslated(NEGATIVE, "You are not allowed to create admin-signs with no stock");
                            return null;
                        }
                    }
                    else
                    {
                        context.sendTranslated(NEGATIVE, "Admin-signs without stock are not allowed!");
                        return null;
                    }
                }
                else
                {
                    if (this.module.getConfig().allowAdminStock)
                    {
                        if (module.perms().SIGN_CREATE_ADMIN_STOCK.isAuthorized(user))
                        {
                            marketSign.setStock(0);
                        }
                        else
                        {
                            context.sendTranslated(NEGATIVE, "You are not allowed to create admin-signs with stock");
                            return null;
                        }
                    }
                    else
                    {
                        context.sendTranslated(NEGATIVE, "Admin-signs with stock are not allowed!");
                        return null;
                    }
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "User signs cannot have no stock!");
                return null;
            }
        }
        if (context.hasNamed("setstock"))
        {
            if (module.perms().SIGN_SETSTOCK.isAuthorized(user))
            {
                if (marketSign.hasStock())
                {
                    marketSign.setStock(context.get("setstock", 0));
                    marketSign.syncOnMe = true;
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "This sign has no stock! Use \"stock\" first to enable it!");
                    return null;
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to set the stock!");
                return null;
            }
        }
        if (context.hasNamed("price"))
        {
            Double dPrice = marketSign.economy.parseFor(context.getString("price"), context.getSource().getLocale());
            if (dPrice == null)
            {
                user.sendTranslated(NEGATIVE, "Invalid price!");
                marketSign.setPrice(0);
                return null;
            }
            else if (dPrice < 0)
            {
                user.sendTranslated(NEGATIVE, "A negative price!? Are you serious?");
                return null;
            }
            else
            {
                marketSign.setPrice((long)(dPrice * marketSign.economy.fractionalDigitsFactor()));
            }
        }
        if (context.hasNamed("amount"))
        {
            Integer amount = context.get("amount", null);
            if (amount == null)
            {
                user.sendTranslated(NEGATIVE, "Invalid amount {input#amount}!", context.getString("amount"));
                return null;
            }
            else if (amount < 0)
            {
                user.sendTranslated(NEGATIVE, "Negative amounts could be unfair! Just sayin'");
                return null;
            }
            else
            {
                marketSign.setAmount(amount);
            }
        }
        if (context.hasNamed("item"))
        {
            ItemStack item = context.get("item", null);
            if (item == null)
            {
                user.sendTranslated(NEGATIVE, "Item not found!");
            }
            else if (marketSign.isAdminSign())
            {
                marketSign.setItemStack(item, false);
            }
            else if (marketSign.hasStock() && marketSign.getStock() != 0)
            {
                user.sendTranslated(NEGATIVE, "You have to take all items out of the market-sign to be able to change the item in it!");
                return null;
            }
            else
            {
                marketSign.setItemStack(item, false);
            }
        }
        if (context.hasNamed("size"))
        {
            if (module.perms().SIGN_SIZE_CHANGE.isAuthorized(user))
            {
                Integer size = context.get("size", null);
                if (size == null || size == 0 || size > 6 || size < -1)
                {
                    context.sendTranslated(NEGATIVE, "Invalid size! Use -1 for infinite OR 1-6 inventory-lines!");
                    return null;
                }
                else
                {
                    if (size == -1 && !module.perms().SIGN_SIZE_INFINITE.isAuthorized(user))
                    {
                        context.sendTranslated(NEGATIVE, "You are not allowed to set infinite inventories!");
                        return null;
                    }
                    else
                    {
                        if (marketSign.isAdminSign())
                        {
                            int maxAdmin = this.module.getConfig().maxAdminStock;
                            if (maxAdmin != -1 && (size > maxAdmin || size == -1))
                            {
                                context.sendTranslated(NEGATIVE, "The maximum size of admin-signs is set to {amount}!", maxAdmin);
                                return null;
                            }
                            else
                            {
                                marketSign.setSize(size);
                                marketSign.syncOnMe = true;
                            }
                        }
                        else // user-sign
                        {
                            int maxUser = this.module.getConfig().maxUserStock;
                            if (maxUser != -1 && (size > maxUser || size == -1))
                            {
                                context.sendTranslated(NEGATIVE, "The maximum size of player signs is set to {amount}!", maxUser);
                                return null;
                            }
                            else
                            {
                                marketSign.setSize(size);
                                marketSign.syncOnMe = true;
                            }
                        }
                    }
                }
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to change the sign inventory-size.");
                return null;
            }
        }
        if (context.hasFlag("exit"))
        {
            this.removeUser(user);
            this.previousMarketSign.put(user.getId(), marketSign);
            this.currentSignLocation.remove(user.getId());
            marketSign.exitEditMode(user);
            return null;
        }
        marketSign.showInfo(user);
        marketSign.updateSignText();
        return null;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(DENY)) return;

        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            if (this.module.getConfig().disableInWorlds.contains(event.getPlayer().getWorld().getName()))
            {
                user.sendTranslated(NEUTRAL, "MarketSigns are disabled in the configuration for this world!");
                return;
            }
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
            {
                if (event.getClickedBlock().getState() instanceof Sign)
                {
                    event.setCancelled(true);
                    event.setUseItemInHand(DENY);
                    Location newLoc = event.getClickedBlock().getLocation();
                    if (!newLoc.equals(this.currentSignLocation.get(user.getId())))
                    {
                        if (this.currentSignLocation.valueCollection().contains(newLoc))
                        {
                            user.sendTranslated(NEGATIVE, "Someone else is editing this sign!");
                            return;
                        }
                    }
                    MarketSign curSign = this.signFactory.getSignAt(newLoc);
                    if (curSign == null)
                    {
                        if (!user.isSneaking())
                        {
                            user.sendTranslated(NEGATIVE, "That is not a market sign!");
                            user.sendTranslated(NEUTRAL, "Use shift leftclick to convert the sign.");
                            return;
                        }
                        curSign = this.signFactory.createSignAt(user, newLoc);
                        this.setEditingSign(user, curSign);
                        return;
                    }
                    if (curSign.isInEditMode())
                    {
                        if (curSign.tryBreak(user))
                        {
                            this.previousMarketSign.put(user.getId(), curSign);
                            this.currentSignLocation.remove(user.getId());
                        }
                        return;
                    }
                    this.setEditingSign(user, curSign);
                }
            }
            else
            {
                if (event.getPlayer().isSneaking()) return;
                BlockState signFound = null;
                if (event.getAction() == RIGHT_CLICK_AIR)
                {
                    if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() != 0)
                    {
                        signFound = MarketSignListener.getTargettedSign(event.getPlayer());
                    }
                }
                else if (event.getAction() == RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign)
                {
                    signFound = event.getClickedBlock().getState();
                }
                if (signFound == null) return;
                event.setCancelled(true);
                event.setUseItemInHand(DENY);
                Location curLoc = signFound.getLocation();
                MarketSign curSign = this.signFactory.getSignAt(curLoc);
                if (curSign == null)
                {
                    user.sendTranslated(NEUTRAL, "This sign is not a market-sign!");
                    return; // not a market-sign
                }
                if (!this.setEditingSign(user, curSign))
                {
                    if (user.getItemInHand() == null || user.getItemInHand().getTypeId() == 0) return;
                    if (!curSign.isAdminSign() && curSign.hasStock() && curSign.getStock() != 0)
                    {
                        user.sendTranslated(NEGATIVE, "You have to take all items out of the market sign to be able to change the item in it!");
                        return;
                    }
                    curSign.setItemStack(user.getItemInHand(), true);
                    curSign.updateSignText();
                    user.sendTranslated(POSITIVE, "Item in sign updated!");
                }
            }
        }
    }

    @EventHandler
    public void onSignPlace(BlockPlaceEvent event)
    {
        if (event.getBlockPlaced().getState() instanceof Sign)
        {
            User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            if (this.hasUser(user))
            {
                if (this.module.getConfig().disableInWorlds.contains(event.getPlayer().getWorld().getName()))
                {
                    user.sendTranslated(NEUTRAL, "MarketSigns are disabled in the configuration for this world!");
                    return;
                }
                if (!module.perms().SIGN_CREATE_ADMIN_CREATE.isAuthorized(user))
                {
                    if (!module.perms().SIGN_CREATE_USER_CREATE.isAuthorized(user))
                    {
                        user.sendTranslated(NEGATIVE, "You are not allowed to create market signs!");
                        event.setCancelled(true);
                        return;
                    }
                }
                this.setEditingSign(user, this.signFactory.createSignAt(user, event.getBlockPlaced().getLocation()));
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.hasUser(user))
        {
            if (this.module.getConfig().disableInWorlds.contains(event.getPlayer().getWorld().getName()))
            {
                user.sendTranslated(NEUTRAL, "MarketSigns are disabled in the configuration for this world!");
                return;
            }
            Location loc = event.getBlock().getLocation();
            if (loc.equals(this.currentSignLocation.get(user.getId())))
            {
                event.setCancelled(true);
            }
        }
    }

    private class SignSizeCompleter implements Completer
    {
        @Override
        public List<String> getSuggestions(CommandInvocation invocation)
        {
            CommandSource commandSource = invocation.getCommandSource();
            if (commandSource instanceof CommandSender)
            {
                if (module.perms().SIGN_SIZE_INFINITE.isAuthorized((CommandSender)commandSource))
                {
                    return Arrays.asList("6", "5", "4", "3", "2", "1", "-1");
                }
                return Arrays.asList("6", "5", "4", "3", "2", "1");
            }
            return new ArrayList<>();
        }
    }
}
