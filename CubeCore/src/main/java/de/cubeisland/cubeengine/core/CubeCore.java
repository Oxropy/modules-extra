package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.Database;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeCore extends JavaPlugin
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    private static CubeCore instance;
    private static Database database;
    private static CubeCoreConfiguration config;

    public static Database getDB()
    {
        return database;
    }
    
    public static CubeCore getInstance()
    {
        return instance;
    }
    
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;

    @Override
    public void onEnable()
    {
        instance = this;
        Configuration configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        debugMode = configuration.getBoolean("debug");
        config = new CubeCoreConfiguration(configuration);
        database = new Database(config.core_database_host,
                                config.core_database_port,
                                config.core_database_user,
                                config.core_database_pass,
                                config.core_database_name);
    }

    @Override
    public void onDisable()
    {
    }
}
