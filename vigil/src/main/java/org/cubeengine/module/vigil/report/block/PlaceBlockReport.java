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
package org.cubeengine.module.vigil.report.block;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import com.flowpowered.math.vector.Vector3d;
import org.cubeengine.module.vigil.Receiver;
import org.cubeengine.module.vigil.report.Action;
import org.cubeengine.module.vigil.report.Recall;
import org.cubeengine.module.vigil.report.Report;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

import static org.cubeengine.module.vigil.report.ReportUtil.containsSingle;
import static org.cubeengine.module.vigil.report.ReportUtil.name;
import static org.spongepowered.api.block.BlockTypes.AIR;

/* TODO Place
Bucket
grow

ignite (fire spreads)
-fireball
-lava
-lighter
-lightning
-other
-spread

flow
-lava
-water

form
grow

pistonmove
spread

entityform
entitychange
endermanplace
 */
public class PlaceBlockReport extends BlockReport<ChangeBlockEvent.Place>
{
    @Override
    public boolean group(Object lookup, Action action, Action otherAction, Report otherReport)
    {
        if (!this.equals(otherReport))
        {
            return false;
        }
        List<Optional<BlockSnapshot>> snaps = action.getCached(BLOCKS_ORIG, Recall::origSnapshot);
        snaps.addAll(otherAction.getCached(BLOCKS_ORIG, Recall::origSnapshot));
        if (!containsSingle(snaps, el -> el.map(BlockSnapshot::getState).map(BlockState::getType).orElse(null))
                || !containsSingle(snaps, el -> el.map(BlockSnapshot::getWorldUniqueId)))
        {
            return false;
        }

        snaps = action.getCached(BLOCKS_REPL, Recall::replSnapshot);
        snaps.addAll(otherAction.getCached(BLOCKS_REPL, Recall::replSnapshot));
        if (!containsSingle(snaps, el -> el.map(BlockSnapshot::getState).map(BlockState::getType).orElse(null)))
        {
            return false;
        }

        // TODO in short timeframe (minutes? configurable)
        return true;
    }

    @Override
    public void showReport(List<Action> actions, Receiver receiver)
    {
        Action action = actions.get(0);

        Iterator<Optional<BlockSnapshot>> orig = action.getCached(BLOCKS_ORIG, Recall::origSnapshot).iterator();
        for (Optional<BlockSnapshot> repl : action.getCached(BLOCKS_REPL, Recall::replSnapshot))
        {
            if (!repl.isPresent())
            {
                throw new IllegalStateException();
            }
            if (!repl.get().getLocation().get().getPosition().equals(((Vector3d)receiver.getLookup())))
            {
                continue;
            }
            showReport(actions, receiver, action, orig.next(), repl.get());
        }
    }

    private void showReport(List<Action> actions, Receiver receiver, Action action, Optional<BlockSnapshot> orig, BlockSnapshot repl)
    {
        Text cause = Recall.cause(action);

        if (orig.isPresent() && !orig.get().getState().getType().equals(AIR))
        {
            receiver.sendReport(actions, actions.size(),
                                "{txt} replace {txt} with {txt}",
                                "{txt} replace {txt} with {txt} x{}",
                                cause, name(orig.get()), name(repl), actions.size());
        }
        else
        {
            receiver.sendReport(actions, actions.size(),
                                "{txt} place {txt}",
                                "{txt} place {txt} x{}",
                                cause, name(repl), actions.size());
        }
    }

    @Listener(order = Order.POST)
    public void listen(ChangeBlockEvent.Place event, @First Player player)
    {
        // TODO cause filtering
        report(observe(event));
    }
}
