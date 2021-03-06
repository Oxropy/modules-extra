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
package org.cubeengine.module.log.converter;

import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.converter.SimpleConverter;
import org.cubeengine.converter.node.IntNode;
import org.cubeengine.converter.node.MapNode;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.NullNode;
import org.cubeengine.converter.node.StringNode;
import org.spongepowered.api.item.inventory.ItemStack;

public class ItemStackConverter extends SimpleConverter<ItemStack>
{
    @Override
    public Node toNode(ItemStack itemStack) throws ConversionException
    {
        if (itemStack == null || itemStack.getItem() == Material.AIR)
        {
            return NullNode.emptyNode();
        }

        MapNode item = MapNode.emptyMap();
        item.set("Count", new IntNode(itemStack.getAmount()));
        item.set("Damage", new IntNode(itemStack.getDurability()));
        item.set("Item", StringNode.of(itemStack.getType().name()));
        net.minecraft.server.v1_8_R2.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);
        if (nmsCopy == null)
        {
            CubeEngine.getLog().error("NMSCopy is unexpectedly null! " + itemStack);
            return null;
        }
        NBTTagCompound tag = nmsCopy.getTag();
        item.set("tag", tag == null ? MapNode.emptyMap() : NBTUtils.convertNBTToNode(tag));
        return item;
    }


    @Override
    public ItemStack fromNode(Node node) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Node count = ((MapNode)node).get("Count");
            Node damage = ((MapNode)node).get("Damage");
            Node item = ((MapNode)node).get("Item");
            Node tag = ((MapNode)node).get("tag");
            if (count instanceof IntNode && damage instanceof IntNode &&
                item instanceof StringNode && (tag instanceof MapNode))
            {
                try
                {
                    ItemStack itemStack = new ItemStack(Material.valueOf(item.asText()));
                    itemStack.setDurability(((IntNode)damage).getValue().shortValue());
                    itemStack.setAmount(((IntNode)count).getValue());
                    net.minecraft.server.v1_8_R2.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
                    nms.setTag(((MapNode)tag).isEmpty() ? null : (NBTTagCompound)NBTUtils.convertNodeToNBT(tag));
                    return CraftItemStack.asBukkitCopy(nms);
                }
                catch (IllegalArgumentException e)
                {
                    throw ConversionException.of(this, item, "Unknown Material!");
                }
            }
            else
            {
                throw ConversionException.of(this, node, "Invalid SubNodes!");
            }
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
