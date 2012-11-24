package de.cubeisland.cubeengine.war.commands;

import de.cubeisland.cubeengine.war.CubeWar;
import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.Perm;
import de.cubeisland.cubeengine.war.area.AreaControl;
import de.cubeisland.cubeengine.war.groups.AreaType;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.user.WarUser;
import de.cubeisland.cubeengine.war.user.UserControl;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommands
{
    private AreaControl areas = AreaControl.get();
    private GroupControl groups = GroupControl.get();
    private UserControl users = UserControl.get();

    public ClaimCommands()
    {
    }

    @Command(usage = "[Radius] [Tag]")
    public boolean claim(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_claim.hasNotPerm(sender))
        {
            return true;
        }
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            WarUser user = users.getUser(player);
            if (args.isEmpty())
            {
                if ((Perm.command_claim_BP.hasPerm(sender))
                        || (Perm.command_claim_ownTeam.hasPerm(sender)))
                {
                    this.claim(player.getLocation(), 0, user.getTeam(), player, user);
                }
                return true;
            }

            if (args.size() > 1)
            {
                int rad;
                try
                {

                    rad = args.getInt(0);
                }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(t("claim_invalid_radius", args.getString(0)));
                    return true;
                }
                if (rad > CubeWar.getInstance().getConfiguration().max_claim)
                {
                    sender.sendMessage(t("claim_big_radius", rad));
                    return true;
                }
                Group team = groups.getGroup(args.getString(1));
                if (team == null)
                {
                    sender.sendMessage(t("claim_invalid_team", args.getString(1)));
                    return true;
                }
                if (Perm.command_claim_BP.hasNotPerm(sender))
                {
                    if (!team.equals(user.getTeam()))
                    {
                        if (Perm.command_claim_otherTeam.hasNotPerm(sender))
                        {
                            return true;
                        }
                    }
                    if (rad > 0)
                    {
                        if (Perm.command_claim_radius.hasNotPerm(sender))
                        {
                            return true;
                        }
                    }
                }
                this.claim(player.getLocation(), rad, team, player, user);
                return true;
            }
            if (args.size() > 0)
            {
                int rad;
                try
                {
                    rad = args.getInt(0);
                }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(t("claim_invalid_radius", args.getString(0)));
                    return true;
                }
                if (rad > CubeWar.getInstance().getConfiguration().max_claim)
                {
                    sender.sendMessage(t("claim_big_radius", rad));
                    return true;
                }
                if (rad > 0)
                {
                    if ((Perm.command_claim_BP.hasNotPerm(sender))
                            && (Perm.command_claim_radius.hasNotPerm(sender)))
                    {
                        return true;
                    }
                }
                this.claim(player.getLocation(), rad, user.getTeam(), player, user);
                return true;
            }

        }
        else
        {
            sender.sendMessage(t("claim_never"));
            return true;
        }
        return false;
    }

    private void claim(Location loc, int rad, Group team, Player player, WarUser user)
    {

        if (team.getKey() == 0)
        {
            player.sendMessage(t("claim_noteam"));
            return;
        }
        if (rad == 0)
        {
            if (areas.getGroup(loc).equals(team))
            {
                player.sendMessage(t("claim_deny_own"));
                return;
            }
        }
        if (areas.getGroup(loc) != null)
        {
            if (team.equals(areas.getGroup(loc)))
            {
                if (Perm.command_claim_BP.hasNotPerm(player))
                {
                    if (Perm.command_claim_fromother.hasNotPerm(player))
                    {
                        return;
                    }
                }
            }
        }
        List<Chunk> chunks = new ArrayList<Chunk>();
        if (rad == 0)
        {
            if (team.getInfluence_used() >= team.getInfluence_max())
            {
                player.sendMessage(t("claim_influence"));
                return;
            }
            Group group = areas.giveChunk(loc.getChunk(), team);
            if (group == null)
            {
                group = groups.getWildLand();
            }
            if (Perm.command_claim_BP.hasPerm(player))
            {
                player.sendMessage(t("claim_claimed_bypass", group.getTag(), team.getTag()));
            }
            else
            {
                if (group.equals(groups.getWildLand()))
                {
                    player.sendMessage(t("claim_claimed_wild", team.getTag()));
                }
                else
                {
                    player.sendMessage(t("claim_claimed_enemy", group.getTag(), team.getTag()));
                }
            }
            return;
        }
        else
        {
            World world = loc.getWorld();
            int x = (int) loc.getChunk().getX();
            int z = (int) loc.getChunk().getZ();
            for (int i = -rad; i <= rad; ++i)
            {
                for (int j = -rad; j <= rad; ++j)
                {
                    chunks.add(world.getChunkAt(x + i, z + j));
                }
            }
        }
        int sum = 0, wild = 0, enemy = 0, own = 0;
        boolean noEnemyClaim = false;
        if (Perm.command_claim_BP.hasNotPerm(player))
        {
            noEnemyClaim = Perm.command_claim_fromother.checkPerm(player);
        }
        boolean influence_low = false;
        for (Chunk chunk : chunks)
        {//TODO Check Money

            if (team.getInfluence_used() >= team.getInfluence_max())
            {
                influence_low = true;
                continue;
            }
            Group group = areas.getGroup(chunk);
            if (!group.getType().equals(AreaType.WILDLAND))
            {
                if ((noEnemyClaim && !team.equals(group)))
                {
                    continue;
                }
            }
            ++sum;
            areas.giveChunk(chunk, team);
            if (group.getType().equals(AreaType.WILDLAND))
            {

                ++wild;
            }
            else if (group.equals(team))
            {
                ++own;
            }
            else
            {
                ++enemy;
            }
        }
        if (influence_low)
        {
            player.sendMessage(t("claim_influence"));
        }
        player.sendMessage(t("claim_more", sum, team.getTag(), wild, enemy, sum - own, own));
    }

    @Command(usage = "[radius]|[all] [Tag]|[all]")
    public boolean unclaim(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_unclaim_BP.hasNotPerm(sender))
        {
            if (Perm.command_unclaim.hasNotPerm(sender))
            {
                return true;
            }
        }
        Player player;
        Location loc;
        WarUser user;
        if (sender instanceof Player)
        {
            player = (Player) sender;
            loc = player.getLocation();
            user = users.getUser(sender);
        }
        else
        {
            sender.sendMessage(t("unclaim_never"));
            return true;
        }
        if (args.isEmpty())
        {
            if (groups.getGroupAtLocation(player).equals(groups.getWildLand()))
            {
                sender.sendMessage(t("unclaim_wild"));
                return true;
            }
            if (Perm.command_unclaim_ownTeam.hasPerm(sender)
                    || (Perm.command_unclaim_BP.hasPerm(sender)));
            this.unclaim(loc, 0, user.getTeam(), sender);
            return true;
        }
        int rad;
        try
        {
            rad = args.getInt(0);
        }
        catch (NumberFormatException ex)
        {
            if (args.getString(0).equalsIgnoreCase("all"))
            {
                rad = -1;
            }
            else
            {
                sender.sendMessage(t("claim_invalid_radius"));
                return true;
            }
        }
        if (args.size() > 1)
        {
            Group group = GroupControl.get().getGroup(args.getString(1));
            if (group == null)
            {
                if (!args.getString(1).equalsIgnoreCase("all"))
                {
                    sender.sendMessage(t("g_noGroup"));
                    return true;
                }
            }
            if (Perm.command_unclaim_BP.hasNotPerm(sender))
            {
                if (group == null)
                {
                    if (Perm.command_unclaim_allTeam.hasNotPerm(sender))
                    {
                        return true;
                    }
                    if (rad == -1)
                    {
                        if (Perm.command_unclaim_allTeam_all.hasNotPerm(sender))
                        {
                            return true;
                        }
                    }
                }
                else
                {
                    if (group.equals(user.getTeam()))
                    {
                        if (Perm.command_unclaim_radius.hasNotPerm(sender))
                        {
                            return true;
                        }
                        if (rad == -1)
                        {
                            if (Perm.command_unclaim_ownTeam_all.hasNotPerm(sender))
                            {
                                return true;
                            }
                        }
                    }
                    else
                    {
                        if (Perm.command_unclaim_otherTeam.hasNotPerm(sender))
                        {
                            return true;
                        }
                        if (rad == -1)
                        {
                            if (Perm.command_unclaim_otherTeam_all.hasNotPerm(sender))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
            this.unclaim(loc, rad, group, sender);
            return true;
        }
        if (args.size() > 0)
        {
            if (Perm.command_unclaim_BP.hasNotPerm(sender))
            {
                if (Perm.command_unclaim_radius.hasNotPerm(sender))
                {
                    return true;
                }
                if (rad == -1)
                {
                    if (Perm.command_unclaim_ownTeam_all.hasNotPerm(sender))
                    {
                        return true;
                    }
                }
            }
            this.unclaim(loc, rad, user.getTeam(), sender);
            return true;
        }
        return false;
    }

    private void unclaim(Location loc, int radius, Group group, CommandSender sender)
    {
        if (radius == 0)
        {
            areas.remChunk(loc);
            sender.sendMessage(t("unclaim_single"));
        }
        else if (radius < 0)
        {
            if (radius == -1)
            {
                if (group == null)
                {
                    groups.wipeArea();
                    sender.sendMessage(t("unclaim_all"));
                }
                else
                {
                    areas.remAll(group);
                    sender.sendMessage(t("unclaim_group_all", group.getTag()));
                }
            }
            else
            {
                sender.sendMessage(t("claim_neg_radius"));
            }
        }
        else
        {
            List<Chunk> chunks = new ArrayList<Chunk>();
            World world = loc.getWorld();
            int x = (int) loc.getChunk().getX();
            int z = (int) loc.getChunk().getZ();
            for (int i = -radius; i <= radius; ++i)
            {
                for (int j = -radius; j <= radius; ++j)
                {
                    chunks.add(world.getChunkAt(x + i, z + j));
                }
            }
            int i = 0;
            for (Chunk chunk : chunks)
            {
                if (group != null)
                {
                    if (!group.equals(areas.getGroup(chunk)))
                    {
                        continue;
                    }
                }
                Group g = areas.remChunk(chunk);
                if (g != null)
                {
                    ++i;
                }
            }
            sender.sendMessage(t("unclaim_more", i));
        }
    }
}