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
package org.cubeengine.module.border;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.module.portals.Portals;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.event.EventManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

@ModuleInfo(name = "Border", description = "Limiting the world size")
public class Border extends Module
{
    private BorderConfig globalConfig;
    private Map<UUID, BorderConfig> worldConfigs;
    @Inject private Reflector reflector;
    @Inject private EventManager em;
    @Inject private CommandManager cm;
    @Inject private Log logger;
    @Inject private Path modulePath;
    private Path folder;
    private BorderPerms perms;

    @Enable
    public void onEnable()
    {
        this.globalConfig = reflector.load(BorderConfig.class, modulePath.resolve("globalconfig.yml").toFile());
        folder = modulePath.resolve("worlds");
        try
        {
            Files.createDirectories(folder);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Could not create the worlds folder", e);
        }
        this.worldConfigs = new HashMap<>();
        Sponge.getServer().getWorlds().forEach(this::loadConfig);
        perms = new BorderPerms(this);
        em.registerListener(this, new BorderListener(this));
        cm.addCommand(new BorderCommands(this, i18n, tm));

    }

    private BorderConfig loadConfig(World world)
    {
        BorderConfig worldConfig = this.globalConfig.loadChild(folder.resolve(world.getName() + ".yml").toFile());
        this.worldConfigs.put(world.getUniqueId(), worldConfig);

        if (!worldConfig.checkCenter(world))
        {
            logger.warn("The world spawn of {} is not inside the border!", world.getName());
        }
        Portals portals = getModularity().provide(Portals.class);
        if (portals != null)
        {
            portals.setRandomDestinationSetting(world, worldConfig.radius, world.getChunk(worldConfig.center.chunkX, 0, worldConfig.center.chunkZ).get());
        }
        return worldConfig;
    }

    public BorderConfig getConfig(World world)
    {
        BorderConfig worldConfig = this.worldConfigs.get(world.getUniqueId());
        if (worldConfig == null)
        {
            return this.loadConfig(world);
        }
        return worldConfig;
    }

    public BorderPerms perms()
    {
        return perms;
    }
}