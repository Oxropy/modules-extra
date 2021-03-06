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
package org.cubeengine.module.unbreakableboat;

import static java.util.Collections.singletonList;

import org.cubeengine.libcube.CubeEngineModule;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.task.TaskManager;
import org.cubeengine.module.unbreakableboat.data.ImmutableUnbreakableData;
import org.cubeengine.module.unbreakableboat.data.UnbreakableData;
import org.cubeengine.module.unbreakableboat.data.UnbreakableDataBuilder;
import org.cubeengine.processor.Module;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A module providing a recipe for an (almost) unbreakable boat
 *
 * Boats do not break easily anymore in 1.11
 */
@Singleton
@Module
public class Unbreakableboat extends CubeEngineModule
{
    private ItemStack boat;

    @Inject private EventManager em;
    @Inject private TaskManager tm;
    @Inject private PluginContainer plugin;

    @Listener
    public void onEnable(GamePreInitializationEvent event)
    {
        tm.runTaskDelayed(Unbreakableboat.class, this::registerRecipe, 1);

        DataRegistration.<UnbreakableData, ImmutableUnbreakableData>builder()
                .dataClass(UnbreakableData.class).immutableClass(ImmutableUnbreakableData.class)
                .builder(new UnbreakableDataBuilder()).manipulatorId("unbreakableboat")
                .dataName("CubeEngine Elevator Data")
                .buildAndRegister(plugin);
    }

    private void registerRecipe() {
        boat = ItemStack.builder().itemType(ItemTypes.BOAT).quantity(1).build();
        boat.offer(Keys.ITEM_ENCHANTMENTS, singletonList(Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(1).build()));
        boat.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Sturdy Boat"));
        boat.offer(Keys.ITEM_LORE, Arrays.asList(Text.of(TextColors.YELLOW, "Can take a lot!")));

        ItemStack log = ItemStack.of(ItemTypes.LOG, 1);

        /*
        ShapedRecipe recipe = Sponge.getRegistry().createBuilder(ShapedRecipe.Builder.class)
            .width(3).height(2)
            .row(0, log, null, log)
            .row(1, log, log, log)
        // TODO SpongePR#1098 .aisle("l l", "lll")
        // TODO SpongePR#1098 .where('l', log)
            .addResult(boat)
            .build();



        Sponge.getRegistry().getRecipeRegistry().register(recipe);
        */

        HashMap<Character, ItemStack> map = new HashMap<>();
        map.put('l', log);
        // TODO wait for SpongeAPI Object recipe = RecipeHack.addRecipe(boat.copy(), new String[]{"l l", "lll"}, map);
    }

    @Listener
    public void onVehicleBreak(AttackEntityEvent event)
    {
        System.out.println("break");
        if (event.getTargetEntity() instanceof Boat)
        {
            if (event.getTargetEntity().get(UnbreakableData.UNBREAKING).isPresent())
            {
                // TODO do no cancel if direct attacker is player
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onVehiclePlace(ConstructEntityEvent.Post event)
    {
        if (event.getTargetEntity() instanceof Boat)
        {
            System.out.println("place");
            event.getTargetEntity().offer(new UnbreakableData(true));
        }
    }
}
