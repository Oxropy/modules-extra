package de.cubeisland.cubeengine.log.listeners.worldedit;

import org.bukkit.World;
import org.bukkit.block.BlockState;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ActionType;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class LogEditSession extends EditSession
{

    private LocalPlayer player;
    private Log module;

    public LogEditSession(LocalWorld world, int maxBlocks, LocalPlayer player, Log module)
    {
        super(world, maxBlocks);
        this.player = player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player, Log module)
    {
        super(world, maxBlocks, blockBag);
        this.player = player;
        this.module = module;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, Log module)
    {
        super(world, maxBlocks);
        this.module = module;
        this.player = null;
    }

    public LogEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, Log module)
    {
        super(world, maxBlocks, blockBag);
        this.module = module;
        this.player = null;
    }

    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block)
    {
        if (this.player instanceof BukkitPlayer)
        {
            if (this.player.getWorld() instanceof BukkitWorld)
            {
                World world = ((BukkitWorld)this.player.getWorld()).getWorld();
                BlockState oldState = world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
                boolean success = super.rawSetBlock(pt, block);
                if (success)
                {
                    User user = this.module.getCore().getUserManager().getExactUser(((BukkitPlayer)this.player).getPlayer());
                    BlockState newState =  world.getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
                    this.module.getLogManager().queueBlockChangeLog(oldState
                                                                        .getLocation(), ActionType.WORLDEDIT, user.key, oldState
                                                                        .getType().name(), oldState
                                                                        .getRawData(), newState.getType()
                                                                                               .name(), newState
                                                                        .getRawData(), null);
                }
                return success;
            }
        }
        return super.rawSetBlock(pt, block);
    }
}