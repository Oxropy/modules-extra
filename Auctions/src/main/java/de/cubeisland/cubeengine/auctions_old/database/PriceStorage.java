package de.cubeisland.cubeengine.auctions_old.database;

import de.cubeisland.cubeengine.auctions_old.CubeAuctions;
import de.cubeisland.cubeengine.auctions_old.Util;
import de.cubeisland.cubeengine.auctions_old.auction.PricedItemStack;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PriceStorage implements Storage<Integer, PricedItemStack>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "priceditem";

    public PriceStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("price_getall", "SELECT id,item,price,timessold FROM {{" + TABLE + "}}");
            this.database.prepareStatement("price_get", "SELECT id,item,price,timessold FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("price_store", "INSERT INTO {{" + TABLE + "}} (item,price,timessold) VALUES (?,?,?)");
            this.database.prepareStatement("price_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("price_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("price_update", "UPDATE {{" + TABLE + "}} SET price=? timesold=? WHERE id=?");
            //this.database.prepareStatement("auction_merge",    "INSERT INTO {{"+TABLE+"}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<PricedItemStack> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("price_getall");

            Collection<PricedItemStack> pricedItems = new ArrayList<PricedItemStack>();
            while (result.next())
            {
                ItemStack item = Util.convertItem(result.getString("item"));
                Material mat = item.getType();
                short data = item.getDurability();
                double price = result.getDouble("price");
                int timessold = result.getInt("timessold");

                pricedItems.add(new PricedItemStack(mat, data, price, timessold));
            }

            return pricedItems;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the PricedItems from the database!", e);
        }
    }

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `priceditem` ("
                    + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + "`item` varchar(42) NOT NULL,"
                    + "`price` decimal(11,2) NOT NULL,"
                    + "`timessold` int(11) NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the PricedItems-Table!", ex);
        }
    }

    public void store(PricedItemStack model)
    {
        try
        {
            String sItem = Util.convertItem(model);
            double price = model.getAvgPrice();
            int timessold = model.getTimesSold();

            this.database.preparedExec("price_store", sItem, price, timessold);
        }
        catch (Exception ex)
        {
            throw new StorageException("Failed to store the Price !", ex);
        }
    }

    public boolean delete(PricedItemStack model)
    {
        return this.delete(model.getKey());
    }

    public boolean delete(Integer id)
    {
        try
        {
            return this.database.preparedExec("price_delete", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Price !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("price_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public void update(PricedItemStack model)
    {
        try
        {
            this.database.preparedExec("price_update", model.getAvgPrice(), model.getTimesSold(), model.getKey());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the Price!", e);
        }
    }

    public PricedItemStack get(Integer key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("price_get", key);

            if (!result.next())
            {
                return null;
            }
            ItemStack item = Util.convertItem(result.getString("item"));
            Material mat = item.getType();
            short data = item.getDurability();
            double price = result.getDouble("price");
            int timessold = result.getInt("timessold");

            return new PricedItemStack(mat, data, price, timessold);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the PricedItem '" + key + "'!", e);
        }
    }

    public void merge(PricedItemStack model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}