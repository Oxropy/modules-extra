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
package org.cubeengine.module.itemrepair;

import java.util.HashMap;
import java.util.Map;
import org.cubeengine.module.itemrepair.material.BaseMaterialContainer;
import org.cubeengine.module.itemrepair.repair.blocks.RepairBlockConfig;
import org.cubeengine.reflect.Section;
import org.cubeengine.reflect.annotations.Comment;
import org.cubeengine.reflect.annotations.Name;
import org.cubeengine.reflect.codec.yaml.ReflectedYaml;

@SuppressWarnings("all")
public class ItemrepairConfig extends ReflectedYaml
{
    // TODO implement with Economy service
    @Name("server.bank")
    public String serverBank = "server";
    @Name("server.player")
    public String serverPlayer = "";
    public Price price = new Price();
    @Name("repair-blocks")
    public Map<String,RepairBlockConfig> repairBlockConfigs = new HashMap<String, RepairBlockConfig>();
    {
        repairBlockConfigs.put("normal", RepairBlockConfig.defaultNormal());
        repairBlockConfigs.put("cheap", RepairBlockConfig.defaultCheap());
    }

    public class Price implements Section
    {
        @Comment("factor x base^EnchantmentLevel")
        @Name("enchant-multiplier")
        public EnchantMultiplier enchantMultiplier = new EnchantMultiplier();
        @Name("materials")
        public BaseMaterialContainer baseMaterials = new BaseMaterialContainer();

        public class EnchantMultiplier implements Section
        {
            public float base = 1.75f;
            public float factor = 2.2f;
        }
    }
}
