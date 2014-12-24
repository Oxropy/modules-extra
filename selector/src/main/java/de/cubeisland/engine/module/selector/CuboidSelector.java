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

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.core.module.service.Selector;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.math.shape.Shape;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.event.Event.Result.DENY;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.PHYSICAL;

public class CuboidSelector implements Selector, Listener
{
    private final de.cubeisland.engine.module.selector.Selector module;
    private final Permission selectPerm;

    public CuboidSelector(de.cubeisland.engine.module.selector.Selector module)
    {
        this.module = module;
        this.module.getCore().getEventManager().registerListener(module, this);
        this.module.getCore().getCommandManager().addCommands(this.module.getCore().getCommandManager(), this.module, new SelectorCommand());
        this.selectPerm = module.getBasePermission().child("use-wand");
        this.module.getCore().getPermissionManager().registerPermission(module, selectPerm);
    }

    @Override
    public Shape getSelection(User user)
    {
        SelectorAttachment attachment = user.attachOrGet(SelectorAttachment.class, this.module);
        return attachment.getSelection();
    }

    @Override
    public Shape get2DProjection(User user)
    {
        throw new UnsupportedOperationException("Not supported yet!"); // TODO Shape.projectOnto(Plane)
    }

    @Override
    public <T extends Shape> T getSelection(User user, Class<T> shape)
    {
        throw new UnsupportedOperationException("Not supported yet!");
    }

    @Override
    public Location getFirstPoint(User user)
    {
        return this.getPoint(user, 0);
    }

    @Override
    public Location getSecondPoint(User user)
    {
        return this.getPoint(user, 1);
    }

    @Override
    public Location getPoint(User user, int index)
    {
        SelectorAttachment attachment = user.attachOrGet(SelectorAttachment.class, this.module);
        return attachment.getPoint(index);
    }

    public static final String SELECTOR_TOOL_NAME = ChatFormat.INDIGO + "Selector-Tool";

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (module.hasWorldEdit()) return;
        if (event.getAction() == PHYSICAL) return;
        if (!selectPerm.isAuthorized(event.getPlayer())) return;
        if (event.getClickedBlock() != null)
        {
            if (event.getPlayer().getItemInHand().hasItemMeta()
                && event.getPlayer().getInventory().getItemInHand().getItemMeta().hasDisplayName()
                && event.getPlayer().getInventory().getItemInHand().getItemMeta().getDisplayName().equals(SELECTOR_TOOL_NAME))
            {
                User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
                SelectorAttachment logAttachment = user.attachOrGet(SelectorAttachment.class, this.module);
                Location clicked = event.getClickedBlock().getLocation();
                if (event.getAction() == LEFT_CLICK_BLOCK)
                {
                    logAttachment.setPoint(0, clicked);
                    user.sendTranslated(POSITIVE, "First position set to ({integer}, {integer}, {integer}).", clicked.getBlockX(), clicked.getBlockY(), clicked.getBlockZ());
                }
                else
                {
                    logAttachment.setPoint(1, clicked);
                    user.sendTranslated(POSITIVE, "Second position set to ({integer}, {integer}, {integer}).", clicked.getBlockX(), clicked.getBlockY(), clicked.getBlockZ());
                }
                event.setCancelled(true);
                event.setUseItemInHand(DENY);
            }
        }
    }
}
