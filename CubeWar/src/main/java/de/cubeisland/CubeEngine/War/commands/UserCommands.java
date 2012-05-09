package de.cubeisland.cubeengine.war.commands;

import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.Perm;
import de.cubeisland.cubeengine.war.user.User;
import de.cubeisland.cubeengine.war.user.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class UserCommands 
{

    public UserCommands() 
    {
    
    }
    
    @Command(usage = "[Player]", aliases = {"show"})
    public boolean whois(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_whois.hasNotPerm(sender)) return true;
        if (args.size() > 0)    
        {
            if (Perm.command_whois_other.hasNotPerm(sender)) return true;
            User user = Users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("e")+t("g_noplayer"));
                return true;
            }
            user.showInfo(sender);
            return true;
        }
        if (args.isEmpty())
        {
            User user = Users.getUser(sender);
            user.showInfo(sender);
            return true;
        }
        return false;
    }
    
    @Command(usage = "set <Player> <bounty>" )//aliases = {""}
    public void bounty(CommandSender sender, CommandArgs args)
    {
        if (Perm.command_bounty.hasNotPerm(sender)) return;
        //TODO bounty adding etc
    }
}
