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
package org.cubeengine.module.itemrepair;

import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.database.Database;
import org.cubeengine.libcube.service.database.ModuleTables;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.filesystem.ModuleConfig;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.cubeengine.module.itemrepair.material.BaseMaterialContainer;
import org.cubeengine.module.itemrepair.material.BaseMaterialContainerConverter;
import org.cubeengine.module.itemrepair.repair.RepairBlockManager;
import org.cubeengine.module.itemrepair.repair.storage.TableRepairBlock;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

@ModuleInfo(name = "ItemRepair", description = "Repair your tools for money")
/*
TODO blocked by custom inventories
TODO blocked by custom data on any block
*/
@ModuleTables(TableRepairBlock.class)
public class Itemrepair extends Module
{
    @ModuleConfig private ItemrepairConfig config;
    public RepairBlockManager repairBlockManager;
    @Inject private Database db;
    @Inject private EventManager em;
    @Inject private CommandManager cm;
    @Inject private I18n i18n;
    @Inject private Log logger;
    @Inject private PermissionManager pm;
    @Inject private EconomyService economy;
    @Inject private PluginContainer plugin;


    @Inject
    public Itemrepair(Reflector reflector)
    {
        reflector.getDefaultConverterManager().registerConverter(new BaseMaterialContainerConverter(), BaseMaterialContainer.class);
    }

    @Enable
    public void onEnable()
    {
        this.repairBlockManager = new RepairBlockManager(this, db, em, i18n, economy, pm);
        em.registerListener(Itemrepair.class, new ItemRepairListener(this, i18n));
        cm.addCommand(new ItemRepairCommands(cm, this, em, i18n));
    }

    public ItemrepairConfig getConfig()
    {
        return config;
    }

    public RepairBlockManager getRepairBlockManager()
    {
        return repairBlockManager;
    }

    public Log getLog()
    {
        return logger;
    }

    public PluginContainer getPlugin() {
        return plugin;
    }
}
