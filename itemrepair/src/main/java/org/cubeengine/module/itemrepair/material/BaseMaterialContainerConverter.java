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
package org.cubeengine.module.itemrepair.material;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.ConverterManager;
import org.cubeengine.converter.converter.SingleClassConverter;
import org.cubeengine.converter.node.MapNode;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.NullNode;
import org.spongepowered.api.item.ItemType;

public class BaseMaterialContainerConverter extends SingleClassConverter<BaseMaterialContainer>
{
    private final Type fieldType;
    private Map<ItemType, Double> map; // Needed for GenericType

    public BaseMaterialContainerConverter()
    {
        try
        {
            fieldType = this.getClass().getDeclaredField("map").getGenericType();
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Node toNode(BaseMaterialContainer object, ConverterManager manager) throws ConversionException
    {
        Map<ItemType,Double> result = new TreeMap<>(new Comparator<ItemType>()
        {
            @Override
            public int compare(ItemType o1, ItemType o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (BaseMaterial baseMaterial : object.getBaseMaterials().values())
        {
            result.put(baseMaterial.getMaterial(),baseMaterial.getPrice());
        }
        return manager.convertToNode(result);
    }

    @Override
    public BaseMaterialContainer fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            map = manager.convertFromNode(node, fieldType);
            BaseMaterialContainer container = new BaseMaterialContainer(map);
            map = null;
            return container;
        }
        else if (node instanceof NullNode)
        {
            return null;
        }
        else
        {
            throw ConversionException.of(this, node, "Node is not a MapNode!");
        }
    }
}
