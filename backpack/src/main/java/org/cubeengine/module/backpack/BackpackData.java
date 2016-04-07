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
package org.cubeengine.module.backpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.cubeisland.engine.reflect.codec.nbt.ReflectedNBT;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.context.Context;

public class BackpackData extends ReflectedNBT
{
    public boolean allowItemsIn = true;
    public int pages = 1;
    public int size = 6;
    public Map<Integer, ItemStack> contents = new HashMap<>();
    public List<Context> activeIn = new ArrayList<>();

    @Override
    public void onSave()
    {
        contents.keySet().stream()
                .filter(next -> contents.get(next) == null)
                .forEach(next -> contents.remove(next));
    }
}