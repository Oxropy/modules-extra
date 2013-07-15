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
package de.cubeisland.cubeengine.signmarket.storage;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleKeyEntity(autoIncrement = true, primaryKey = "key", tableName = "signmarketblocks", indices = {
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "world", f_field = "key", f_table = "worlds", onDelete = "CASCADE"),
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "itemKey", f_field = "key", f_table = "signmarketitem", onDelete = "CASCADE"),
        @Index(value = Index.IndexType.FOREIGN_KEY, fields = "owner", f_field = "key", f_table = "user"),
        @Index(value = Index.IndexType.INDEX, fields = {"x", "y", "z"})
})
public class SignMarketBlockModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key = -1;

    @Attribute(type = AttrType.INT, unsigned = true)
    public long world;
    @Attribute(type = AttrType.INT)
    public int x;
    @Attribute(type = AttrType.INT)
    public int y;
    @Attribute(type = AttrType.INT)
    public int z;

    @Attribute(type = AttrType.BOOLEAN)
    public Boolean signType; // null - invalid | true - buy | false - sell
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long owner; // null - admin-shop | else user-key

    @Attribute(type = AttrType.INT, unsigned = true)
    public long itemKey = -1;

    @Attribute(type = AttrType.SMALLINT, unsigned = true)
    public int amount = 0;
    @Attribute(type = AttrType.MEDIUMINT, unsigned = true, notnull = false)
    public Integer demand;

    @Attribute(type = AttrType.INT, unsigned = true)
    public long price;

    // Helper-methods:
    private Location location;

    public SignMarketBlockModel(Location location)
    {
        this.world = CubeEngine.getCore().getWorldManager().getWorldId(location.getWorld());
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public void copyValuesFrom(SignMarketBlockModel blockInfo)
    {
        this.signType = blockInfo.signType;
        this.owner = blockInfo.owner;
        this.itemKey = blockInfo.itemKey;
        this.amount = blockInfo.amount;
        this.demand = blockInfo.demand;
        this.price = blockInfo.price;
    }


    /**
     * Returns the location of this sign
     * <p>Do NEVER change this location!
     *
     * @return the location of the sign represented by this model
     */
    public final Location getLocation()
    {
        if (this.location == null)
        {
            this.location = new Location(CubeEngine.getCore().getWorldManager().getWorld(world), x, y, z);
        }
        return this.location;
    }

    /**
     * Sets the owner
     *
     * @param owner null for admin-signs
     */
    public void setOwner(User owner)
    {
        this.owner = owner == null ? null : owner.key;
    }

    public boolean isOwner(User user)
    {
        return this.owner.equals(user.key);
    }

    //for database:
    @Override
    public Long getId()
    {
        return key;
    }
    @Override
    public void setId(Long id)
    {
        this.key = id;
    }
    public SignMarketBlockModel()
    {}
}
