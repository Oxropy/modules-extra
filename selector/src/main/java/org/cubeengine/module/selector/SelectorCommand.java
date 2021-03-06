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
import static org.spongepowered.api.data.key.Keys.COAL_TYPE;
import static org.spongepowered.api.data.key.Keys.DISPLAY_NAME;
import static org.spongepowered.api.data.key.Keys.ITEM_ENCHANTMENTS;
import static org.spongepowered.api.data.key.Keys.ITEM_LORE;
import static org.spongepowered.api.item.ItemTypes.COAL;

import org.cubeengine.butler.filter.Restricted;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.util.SpawnUtil;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.CoalTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.Optional;

public class SelectorCommand
{
    private Game game;
    private I18n i18n;

    public SelectorCommand(Game game, I18n i18n)
    {
        this.game = game;
        this.i18n = i18n;
    }

    public void giveSelectionTool(Player user)
    {
        ItemStack found = null;
        Inventory axes = user.getInventory().query(QueryOperationTypes.ITEM_TYPE.of(ItemTypes.WOODEN_AXE));
        for (Inventory slot : axes.slots())
        {
            Optional<ItemStack> itemStack = slot.peek();
            if (itemStack.isPresent())
            {
                Optional<Text> display = itemStack.get().get(Keys.DISPLAY_NAME);
                if (display.isPresent())
                {
                    if ("Selector-Tool".equals(display.get().toPlain()))
                    {
                        found = itemStack.get();
                        slot.clear();
                        break;
                    }
                }
            }
        }

        Optional<ItemStack> itemInHand = user.getItemInHand(HandTypes.MAIN_HAND);
        if (found == null)
        {
            found = game.getRegistry().createBuilder(ItemStack.Builder.class).itemType(COAL).quantity(1).build();
            found.offer(COAL_TYPE, CoalTypes.CHARCOAL);
            found.offer(ITEM_ENCHANTMENTS, Arrays.asList(Enchantment.builder().type(EnchantmentTypes.BINDING_CURSE).level(1).build()));
            found.offer(DISPLAY_NAME, Text.of(TextColors.BLUE, "Selector-Tool"));
            found.offer(ITEM_LORE, Arrays.asList(Text.of("created by ", user.getName())));

            user.setItemInHand(HandTypes.MAIN_HAND, found);
            if (itemInHand.isPresent())
            {
                if (user.getInventory().offer(itemInHand.get()).getType() != InventoryTransactionResult.Type.SUCCESS)
                {
                    SpawnUtil.spawnItem(itemInHand.get(), user.getLocation());
                }
            }
            i18n.send(user, POSITIVE, "Received a new region selector tool");
            return;
        }

        user.setItemInHand(HandTypes.MAIN_HAND, found);
        itemInHand.ifPresent(stack -> user.getInventory().offer(stack));
        i18n.send(user, POSITIVE, "Found a region selector tool in your inventory!");
    }

    @Command(desc = "Provides you with a wand to select a cuboid")
    @Restricted(value = Player.class, msg =  "You cannot hold a selection tool!")
    public void selectiontool(Player context)
    {
        giveSelectionTool(context);
    }
}
