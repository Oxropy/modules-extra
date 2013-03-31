package de.cubeisland.cubeengine.test;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.v1_5_R2.DedicatedPlayerList;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.Packet0KeepAlive;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.test.commands.TestCommands;
import de.cubeisland.cubeengine.test.database.TestManager;
import de.cubeisland.cubeengine.test.database.TestModel;
import de.cubeisland.cubeengine.test.l18n.TestRecource;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;
import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;

public class Test extends Module
{
    public TestManager manager;
    public UserManager uM;
    protected TestConfig config;
    public static List<String> aListOfPlayers;
    public Basics basicsModule;
    private Timer timer;

    @Override
    public void onEnable()
    {
        config.loadChild(new File(this.getFolder(), "childConfig.yml"));
        Configuration.load(TestConfig2.class, new File(this.getFolder(), "updateConfig.yml"));
        this.getCore().getFileManager().dropResources(TestRecource.values());
        this.uM = this.getCore().getUserManager();
        try
        {
            this.initializeDatabase();
            this.testDatabase();
        }
        catch (Exception ex)
        {
            this.getLog().log(ERROR, "Error while Enabling the TestModule", ex);
        }
        try
        {
            this.getLog().addHandler(new CubeFileHandler(LogLevel.ALL, new File(this.getCore().getFileManager().getLogDir(), "test").toString()));
        }
        catch (Exception ex)
        {
            this.getLog().log(ERROR, "Error while adding the FileHandler", ex);
        }
        this.getCore().getEventManager().registerListener(this, new TestListener(this));

        this.testl18n();
        this.testMatchers();
        this.testsomeUtils();

        this.getCore().getCommandManager().registerCommands(this, new TestCommands(), ReflectedCommand.class);

        this.getCore().getEventManager().registerListener(this, new Listener()
        {
            @EventHandler
            public void onLanguageReceived(PlayerLanguageReceivedEvent event)
            {
                System.out.print("Player: " + event.getPlayer().getName() + " Lang: " + event.getLanguage());
            }
        });

        this.getLog().log(LogLevel.DEBUG, "Basics-Module: {0}", String.valueOf(basicsModule));
        this.getLog().log(LogLevel.DEBUG, "BukkitCore-Plugin: {0}", String.valueOf(this.getCore()));

        timer = new Timer("keepAliveTimer");
        timer.schedule(new KeepAliveTimer(), 2 * 1000, 2 * 1000);
    }

    public void initializeDatabase() throws SQLException
    {
        Database db = this.getCore().getDB();
        try
        {
            db.execute(db.getQueryBuilder().dropTable("Orders").end());
        }
        catch (Exception ignore)
        {}
        manager = new TestManager(db);

    }

    @Override
    public void onDisable()
    {
        this.timer.cancel();
        this.timer = null;
    }

    private Date getDate(int year, int month, int day)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return new Date(calendar.getTimeInMillis());
    }

    public void testDatabase() throws SQLException
    {
        Database database = this.getCore().getDB();

        try
        {//Clears the TestLogs in Database (This does always fail with new db)
            database.execute(database.getQueryBuilder().truncateTable("test_log").end());
        }
        catch (Exception ignored)
        {}

        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 10, "Heinz"), false);
        this.manager.store(new TestModel(this.getDate(2012, 6, 8), 30, "Hans"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 6), 20, "Manfred"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 20, "Heinz"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 120, "Hans"), false);
        this.manager.store(new TestModel(this.getDate(2011, 2, 8), 50, "Manfred"), false);
        this.manager.get(2L);
        this.manager.getAll();
        TestModel model = this.manager.get(3L);
        model.orderDate = this.getDate(111, 2, 2);
        model.orderPrice = 100;
        model.customer = "Paul";
        this.manager.update(model);
    }

    public void testl18n()
    {
        this.getLog().log(DEBUG, CubeEngine.getCore().getI18n().
            translate("de_DE", "test", "english TEST"));
        this.getLog().log(DEBUG, CubeEngine.getCore().getI18n().
            translate("fr_FR", "test", "english TEST"));
    }

    private void testMatchers()
    {
        this.getLog().log(DEBUG, String.valueOf(Match.enchant().enchantment("infinity")));
        this.getLog().log(DEBUG, String.valueOf(Match.enchant().enchantment("infini")));
        this.getLog().log(DEBUG, String.valueOf(Match.enchant().enchantment("hablablubb")) + " is null");
        this.getLog().log(DEBUG, String.valueOf(Match.enchant().enchantment("protect")));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("stone").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("stoned").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("hablablubb")) + " is null");
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("wool:red").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("35").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("35:15").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("35:red").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("wood:birch").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("leves:pine").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.material().itemStack("spawnegg:pig").serialize()));
        this.getLog().log(DEBUG, String.valueOf(Match.entity().any("pig")));
        this.getLog().log(DEBUG, String.valueOf(Match.entity().monster("zombi")));
        this.getLog().log(DEBUG, String.valueOf(Match.entity().friendlyMob("shep")));
        this.getLog().log(DEBUG, String.valueOf(Match.entity().friendlyMob("ghast")) + " is null");
    }

    private void testsomeUtils()
    {
        try
        {
            aListOfPlayers = FileUtil.readStringList(new File(this.getFolder(), "testdata" + File.separatorChar + "player.txt"));
        }
        catch (Exception ex)
        {
            this.getLog().log(ERROR, "Error in testsomeutils", ex);
        }
    }

    private class KeepAliveTimer extends TimerTask
    {
        private final DedicatedPlayerList mojangServer;
        private final Random random;

        public KeepAliveTimer()
        {
            this.mojangServer = ((CraftServer)((BukkitCore)getCore()).getServer()).getHandle();
            this.random = new Random();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run()
        {
            for (EntityPlayer player : (List<EntityPlayer>)this.mojangServer.players)
            {
                player.playerConnection.sendPacket(new Packet0KeepAlive(random.nextInt()));
            }
        }
    }
}