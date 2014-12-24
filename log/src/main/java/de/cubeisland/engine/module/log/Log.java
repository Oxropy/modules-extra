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
package de.cubeisland.engine.module.log;

import org.bukkit.Art;
import org.bukkit.Note;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.cubeisland.engine.module.bigdata.Bigdata;
import de.cubeisland.engine.module.bigdata.MongoDBCodec;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.module.Inject;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.module.log.action.ActionManager;
import de.cubeisland.engine.module.log.action.block.player.worldedit.LogEditSessionFactory;
import de.cubeisland.engine.module.log.action.player.item.container.ContainerType;
import de.cubeisland.engine.module.log.action.player.item.container.ContainerTypeConverter;
import de.cubeisland.engine.module.log.commands.LogCommands;
import de.cubeisland.engine.module.log.commands.LookupCommands;
import de.cubeisland.engine.module.log.converter.ArtConverter;
import de.cubeisland.engine.module.log.converter.BlockFaceConverter;
import de.cubeisland.engine.module.log.converter.DamageCauseConverter;
import de.cubeisland.engine.module.log.converter.EntityTypeConverter;
import de.cubeisland.engine.module.log.converter.ItemStackConverter;
import de.cubeisland.engine.module.log.converter.NoteConverter;
import de.cubeisland.engine.module.log.storage.LogManager;
import de.cubeisland.engine.module.log.tool.ToolListener;
import de.cubeisland.engine.messagecompositor.macro.example.DateFormatter;
import de.cubeisland.engine.messagecompositor.macro.example.DateFormatter.DateReader;
import de.cubeisland.engine.reflect.codec.ConverterManager;

public class Log extends Module implements Listener
{
    private LogManager logManager;
    private LogConfiguration config;
    private ObjectMapper objectMapper = null;
    private ActionManager actionManager;
    private boolean worldEditFound = false;

    @Inject
    private Bigdata bigdata;

    @Override
    public void onEnable()
    {
        this.getCore().getI18n().getCompositor().registerMacro(new DateFormatter());
        this.getCore().getI18n().getCompositor().registerReader(DateFormatter.class, "format", new DateReader());
        this.config = this.loadConfig(LogConfiguration.class);
        ConverterManager cMan = this.getCore().getConfigFactory().getDefaultConverterManager();
        cMan.registerConverter(ContainerType.class, new ContainerTypeConverter());
        cMan.registerConverter(EntityType.class, new EntityTypeConverter());
        cMan.registerConverter(DamageCause.class, new DamageCauseConverter());
        cMan.registerConverter(BlockFace.class, new BlockFaceConverter());
        cMan.registerConverter(Art.class, new ArtConverter());
        cMan.registerConverter(Note.class, new NoteConverter());
        this.getCore().getConfigFactory().getCodecManager().getCodec(MongoDBCodec.class).
            getConverterManager().registerConverter(ItemStack.class, new ItemStackConverter());
        this.logManager = new LogManager(this, bigdata);
        this.actionManager = new ActionManager(this);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.addCommands(cm, this, new LookupCommands(this));
        cm.addCommand(new LogCommands(this));
        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            LogEditSessionFactory.initialize(this);
            this.getCore().getEventManager().registerListener(this, this); // only register if worldEdit is available
        }
        catch (ClassNotFoundException ignored)
        {
            this.getLog().warn("No WorldEdit found!");
        }
        this.getCore().getEventManager().registerListener(this, new ToolListener(this));
    }

    @EventHandler
    public void onWorldEditEnable(PluginEnableEvent event)
    {
        if (event.getPlugin() instanceof WorldEditPlugin)
        {
            LogEditSessionFactory.initialize(this);
            worldEditFound = true;
        }
    }

    @Override
    public void onDisable()
    {
        this.logManager.disable();
        if (worldEditFound)
        {
            LogEditSessionFactory.shutdown();
        }
        super.onDisable();
    }

    public LogManager getLogManager()
    {
        return this.logManager;
    }

    public LogConfiguration getConfiguration() {
        return this.config;
    }

    public ObjectMapper getObjectMapper()
    {
        if (this.objectMapper == null)
        {
            this.objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

    public ActionManager getActionManager()
    {
        return actionManager;
    }

    public boolean hasWorldEdit()
    {
        return this.worldEditFound;
    }
}
