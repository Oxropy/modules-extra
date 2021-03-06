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
package org.cubeengine.module.chat.listener;

import static org.spongepowered.api.event.Order.POST;

import org.cubeengine.module.chat.Chat;
import org.cubeengine.module.chat.command.AfkCommand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class AfkListener
{
    private final Chat module;
    private AfkCommand afkCommand;

    public AfkListener(Chat module, AfkCommand afkCommand)
    {
        this.module = module;
        this.afkCommand = afkCommand;
    }

    @Listener(order = POST)
    public void onMove(MoveEntityEvent event)
    {
        if (event.getTargetEntity() instanceof Player)
        {
            if (event.getFromTransform().getLocation().getBlockX() == event.getToTransform().getLocation().getBlockX()
                    && event.getFromTransform().getLocation().getBlockZ() == event.getToTransform().getLocation().getBlockZ())
            {
                return;
            }
            this.updateLastAction(((Player) event.getTargetEntity()));
        }

    }

    @Listener(order = POST)
    public void onInventoryInteract(InteractInventoryEvent event, @Root Player player)
    {
        this.updateLastAction(player);
    }

    @Listener(order = POST)
    public void playerInteract(InteractBlockEvent event, @Root Player player)
    {
        this.updateLastAction(player);
    }


    @Listener(order = POST)
    public void playerInteract(InteractEntityEvent event, @Root Player player)
    {
        this.updateLastAction(player);
    }

    @Listener(order = POST)
    public void onChat(MessageChannelEvent event, @Root Player player)
    {
        this.updateLastAction(player);
        afkCommand.run();
    }

    @Listener(order = POST)
    public void onCommand(SendCommandEvent event, @Root Player player)
    {
        this.updateLastAction(player);
    }

    @Listener(order = POST)
    public void onTabComplete(TabCompleteEvent event, @Root Player player)
    {
        this.updateLastAction(player);
    }

    @Listener(order = POST)
    public void onLeave(ClientConnectionEvent.Disconnect event, @Root Player player)
    {
        afkCommand.setAfk(player, false);
        afkCommand.resetLastAction(player);
    }

    @Listener(order = POST)
    public void onBowShot(LaunchProjectileEvent event, @Root Player source)
    {
        this.updateLastAction(source);
    }

    private void updateLastAction(Player player)
    {
        if (afkCommand.isAfk(player) && player.hasPermission(module.perms().PREVENT_AUTOUNAFK.getId()))
        {
            return;
        }
        afkCommand.updateLastAction(player);
    }

}
