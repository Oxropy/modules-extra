package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.World;

public class UserCommandHelper extends ContainerCommand
{
    protected RoleManager manager;
    protected WorldManager worldManager;
    protected Roles module;

    public UserCommandHelper(Roles module)
    {
        super(module, "user", "Manage users.");//TODO alias manuser
        this.manager = module.getManager();
        this.worldManager = module.getCore().getWorldManager();
        this.module = module;
    }

    protected User getUser(CommandContext context, int pos)
    {
        User user;
        if (context.hasIndexed(pos))
        {
            user = context.getUser(pos);
        }
        else
        {
            user = context.getSenderAsUser("roles", "&cYou have to specify a player.");
        }
        if (user == null)
        {
            paramNotFound(context, "roles", "&cUser %s not found!", context.getString(pos));
        }
        return user;
    }

    protected UserSpecificRole getUserRole(User user, World world)
    {
        long worldId = this.getWorldId(world);
        if (user == null)
        {
            return null;
        }
        TLongObjectHashMap<UserSpecificRole> roleContainer = user.getAttribute(this.getModule(), "roleContainer");
        if (roleContainer == null)
        {
            if (user.isOnline())
            {
                throw new IllegalStateException("User has no rolecontainer!");
            }
            else
            {
                this.manager.preCalculateRoles(user.getName(), true);
                roleContainer = user.getAttribute(this.getModule(), "roleContainer");
            }
        }
        return roleContainer.get(worldId);
    }

    protected long getWorldId(World world)
    {
        return this.getModule().getCore().getWorldManager().getWorldId(world);
    }

    /**
     * Returns the world defined with named param "in" or the users world
     *
     * @param context
     * @param user
     * @return
     */
    protected World getWorld(CommandContext context, User user)
    {
        User sender = context.getSenderAsUser();
        World world;
        if (context.hasNamed("in"))
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                paramNotFound(context, "roles", "&cWorld %s not found!", context.getString("in"));
            }
        }
        else
        {
            if (sender == null)
            {
                world = this.worldManager.getWorld(ModuleManagementCommands.curWorldIdOfConsole);
                if (world == null)
                {
                    invalidUsage(context, "roles", "&ePlease provide a world.\n&aYou can define a world with &6/roles admin defaultworld <world>");
                }
                else
                {
                    context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
                }
            }
            else
            {
                world = this.worldManager.getWorld((Long) sender.getAttribute(this.module, "curWorldId"));
                if (world == null)
                {
                    world = sender.getWorld();
                }
                else
                {
                    context.sendMessage("roles", "&eYou are using &6%s &eas current world.", world.getName());
                }
            }
        }
        return world;
    }
}