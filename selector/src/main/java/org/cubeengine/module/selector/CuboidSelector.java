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
package org.cubeengine.module.selector;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;
import static org.spongepowered.api.block.BlockTypes.AIR;
import static org.spongepowered.api.text.format.TextColors.GRAY;

import org.cubeengine.libcube.service.Selector;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.util.math.shape.Cuboid;
import org.cubeengine.libcube.util.math.shape.Shape;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class CuboidSelector implements Selector
{
    @Inject private org.cubeengine.module.selector.Selector module;
    @Inject private EventManager em;
    @Inject private I18n i18n;

    private Map<UUID, SelectorData> selectorData = new HashMap<>();

    @Inject
    public void onEnable()
    {
        em.registerListener(Selector.class, this);
    }

    @Override
    public Shape getSelection(Player user)
    {
        SelectorData data = this.selectorData.get(user.getUniqueId());
        return data == null ? null : data.getSelection();
    }

    @Override
    public Shape get2DProjection(Player user)
    {
        throw new UnsupportedOperationException("Not supported yet!"); // TODO Shape.projectOnto(Plane)
    }

    @Override
    public <T extends Shape> T getSelection(Player user, Class<T> shape)
    {
        throw new UnsupportedOperationException("Not supported yet!");
    }

    @Override
    public Location<World> getPoint(Player user, int index)
    {
        SelectorData data = this.selectorData.get(user.getUniqueId());
        return data == null ? null : data.getPoint(index);
    }

    @Listener
    public void onInteract(InteractBlockEvent event, @First Player player)
    {
        if (!(event instanceof InteractBlockEvent.Primary.MainHand) && !(event instanceof InteractBlockEvent.Secondary.MainHand))
        {
            return;
        }

        if (event.getTargetBlock() == BlockSnapshot.NONE)
        {
            return;
        }
        Location block = event.getTargetBlock().getLocation().get();
        if ((int)block.getPosition().length() == 0)
        {
            return;
        }
        Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemInHand.isPresent() || !"Selector-Tool".equals(itemInHand.get().get(Keys.DISPLAY_NAME).map(Text::toPlain).orElse("")))
        {
            return;
        }

        if (block.getBlockType() == AIR || !player.hasPermission(module.getSelectPerm().getId()))
        {
            return;
        }

        SelectorData data = selectorData.computeIfAbsent(player.getUniqueId(), k -> new SelectorData());
        if (event instanceof InteractBlockEvent.Primary)
        {
            data.setPoint(0, block);
            Text selected = getText(player, data);
            i18n.send(player, POSITIVE, "Position 1 ({integer}, {integer}, {integer}). {txt}", block.getBlockX(), block.getBlockY(), block.getBlockZ(), selected);
        }
        else if (event instanceof InteractBlockEvent.Secondary)
        {
            data.setPoint(1, block);
            Text selected = getText(player, data);
            i18n.send(player, POSITIVE, "Position 2 ({integer}, {integer}, {integer}). {txt}", block.getBlockX(), block.getBlockY(), block.getBlockZ(), selected);
        }
        event.setCancelled(true);
    }

    private Text getText(Player player, SelectorData data)
    {
        if (data.getSelection() ==null)
        {
            return i18n.translate(player, TextFormat.of(GRAY), "incomplete selection");
        }
        Cuboid cube = data.getSelection().getBoundingCuboid();
        int amount = (int) Math.abs((cube.getDepth() + 1) * (cube.getHeight() + 1) * (cube.getWidth() + 1));
        return i18n.translate(player, TextFormat.of(GRAY), "{amount} blocks selected", amount);
    }

    @Override
    public void setPoint(Player user, int index, Location<World> loc)
    {
        SelectorData data = selectorData.computeIfAbsent(user.getUniqueId(), k -> new SelectorData());
        data.setPoint(index, loc);
    }
}
