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
package de.cubeisland.engine.core.bukkit;

import de.cubeisland.engine.core.CoreConfiguration;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;

public class BukkitCoreConfiguration extends CoreConfiguration
{
    @Option("prevent-spam-kick")
    @Comment("Whether to prevent Bukkit from kicking players for spamming")
    public boolean preventSpamKick = false;

    @Option("commands.improve-vanilla")
    @Comment("Whether to replace the vanilla standard commands with improved ones")
    public boolean improveVanillaCommands = true;

    @Option("catch-system-signals")
    @Comment("This allows the CubeEngine to act when signals are send to the Minecraft server")
    public boolean catchSystemSignals = true;

    @Option("metrics-enable")
    @Comment("Whether to send anonymous plugin metrics to http://mcstats.org")
    public boolean sendMetrics = true;
}
