/*
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
package org.cubeengine.module.vigil.commands;

import org.bson.Document;
import org.cubeengine.butler.alias.Alias;
import org.cubeengine.butler.parametric.Command;
import org.cubeengine.butler.parametric.Flag;
import org.cubeengine.butler.parametric.Named;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.command.ContainerCommand;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.module.vigil.Lookup;
import org.cubeengine.module.vigil.Vigil;
import org.cubeengine.module.vigil.data.LookupData;
import org.cubeengine.module.vigil.report.Report;
import org.cubeengine.module.vigil.storage.QueryManager;
import org.spongepowered.api.entity.living.player.Player;

@Command(name = "vigil", alias = "log", desc = "Vigil-Module Commands")
public class VigilLookupCommands extends ContainerCommand
{

    private Vigil module;
    private I18n i18n;
    private QueryManager qm;

    public VigilLookupCommands(Vigil module, CommandManager cm, I18n i18n, QueryManager qm)
    {
        super(cm, Vigil.class);
        this.module = module;
        this.i18n = i18n;
        this.qm = qm;
    }

    @Alias(value = "lookup")
    @Command(desc = "Performs a lookup.")
    public void lookup(Player context, @Named("radius") Integer radius, @Named("report") Report report,
            @Named("prepared") String preparedLookup,
            @Flag boolean last)
    {
        LookupData ld = new LookupData();
        Lookup lookup = preparedLookup == null ? new Lookup(ld) : new Lookup(Document.parse(module.getConfig().preparedReports.get(preparedLookup)));
        if (last)
        {
            lookup = this.qm.getLast(context).orElse(lookup);
        }

        lookup = lookup.with(context.getLocation()).withRadius(radius)
            .withReport(report);

        System.out.print(report + "\n");

        this.qm.queryAndShow(lookup, context);
    }

    @Command(desc = "Performs a lookup nearby")
    public void nearby(Player context, @Named("report") Report report)
    {
        this.lookup(context, 5, report, null, false);
    }

    // TODO rollback
    // TODO redo

}
