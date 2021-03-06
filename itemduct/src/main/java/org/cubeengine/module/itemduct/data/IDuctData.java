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
package org.cubeengine.module.itemduct.data;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.TypeTokens;

import java.util.List;
import java.util.Map;

public interface IDuctData
{
    TypeToken<MapValue<Direction, List<ItemStack>>> TTV_ItemDirection = new TypeToken<MapValue<Direction, List<ItemStack>>>() {};

    Key<MapValue<Direction, List<ItemStack>>> FILTERS = Key.builder().type(TTV_ItemDirection)
            .query(of("ductfilters")).id("filters").name("ItemDuct Filters").build();

    Key<Value<Integer>> USES = Key.builder().type(TypeTokens.INTEGER_VALUE_TOKEN)
            .query(of("activatoruses")).id("uses").name("ItemDuct Activator Uses").build();

    Map<Direction, List<ItemStack>> getFilters();
    int  getUses();
}
