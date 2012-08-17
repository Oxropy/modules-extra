package de.cubeisland.cubeengine.test;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.test.database.TestStorage;
import java.sql.SQLException;

public class CubeTest extends Module
{
    @Override
    public void onEnable()
    {
        this.getLogger().info("Test1 onEnable...");
        Configuration.load(TestConfig.class, this);
        this.initializeDatabase();
        this.testDatabase();

    }

    public void initializeDatabase()
    {
        try
        {
            TestStorage storage = new TestStorage(this.getDatabase());
            storage.initialize();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable()
    {
    }

    public void testDatabase()
    {
        this.getDatabase().getQueryBuilder()
                .createTable("Orders", true)
                    .beginFields()
                        .field("id", AttrType.INT, 0, true, true, true)
                        .field("OrderDate", AttrType.DATE)
                        .field("OrderPrice", AttrType.INT)
                        .field("Customer", AttrType.VARCHAR, 16)
                        .primaryKey("id")
                    .endFields()
                    .engine("InnoDB").defaultcharset("utf8").autoIncrement(1)
                .end()
            .end();
        this.getDatabase().getQueryBuilder()
                .select()
                    .beginFunction()
                        .avg("OrderPrice").as("OrderAverage")
                    .end()
                    .from("Orders")
                .end()
            .end();
    }

    public void testl18n()
    {
        //TODO
    }
}
