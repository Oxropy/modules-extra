package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Prevents idling players.
 */
public class AfkPrevention extends Prevention
{
    private THashMap<Player, PlayerAfkTracker> trackerMap;
    private final TaskManager taskManager;
    private int timeout;

    public AfkPrevention(Guests guests)
    {
        super("afk", guests, false);
        this.trackerMap = null;
        this.taskManager = guests.getTaskManger();
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration config = super.getDefaultConfig();

        config.set("timeout", 60 * 10);

        return config;
    }

    @Override
    public void enable()
    {
        super.enable();
        this.timeout = getConfig().getInt("timeout") * 20;

        this.trackerMap = new THashMap<Player, PlayerAfkTracker>();
    }

    @Override
    public void disable()
    {
        super.disable();
        for (PlayerAfkTracker tracker : this.trackerMap.values())
        {
            tracker.cancel();
        }
        this.trackerMap.clear();
        this.trackerMap = null;
    }

    public void updateTracker(final Player player)
    {
        final PlayerAfkTracker tracker = this.trackerMap.get(player);
        if (tracker != null)
        {
            tracker.update();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        if (!can(player))
        {
            this.trackerMap.put(player, new PlayerAfkTracker(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event)
    {
        final PlayerAfkTracker tracker = this.trackerMap.remove(event.getPlayer());
        if (tracker != null)
        {
            tracker.cancel();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void moveUpdater(PlayerMoveEvent event)
    {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
        {
            return;
        }
        this.updateTracker(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void chatUpdater(PlayerCommandPreprocessEvent event)
    {
        this.updateTracker(event.getPlayer());
    }

    private class PlayerAfkTracker implements Runnable
    {
        private final static int UPDATE_DELAY = 250;
        private final Player player;
        private int taskId;
        private long nextUpdate;

        public PlayerAfkTracker(Player player)
        {
            this.player = player;
            this.taskId = -1;
            this.update();
        }

        public final void run()
        {
            if (!can(this.player))
            {
                this.player.kickPlayer(getMessage());
            }
        }

        public final void update()
        {
            final long currentTime = System.currentTimeMillis();
            if (nextUpdate <= currentTime)
            {
                this.nextUpdate = currentTime + UPDATE_DELAY;
                if (this.taskId >= 0)
                {
                    taskManager.cancelTask(getModule(), this.taskId);
                    this.taskId = -1;
                }
                this.taskId = taskManager.scheduleSyncDelayedTask(getModule(), this, timeout);
                if (this.taskId < 0)
                {
                    getModule().getLogger().log(LogLevel.ERROR, "Tracker for {0} failed to schedule!", this.player.getName());
                }
            }
        }

        public final void cancel()
        {
            if (this.taskId >= 0)
            {
                taskManager.cancelTask(getModule(), this.taskId);
            }
        }
    }
}
