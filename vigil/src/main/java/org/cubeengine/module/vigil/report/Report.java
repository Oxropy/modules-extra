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
/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cubeengine.module.vigil.report;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.cubeengine.module.vigil.Receiver;
import org.spongepowered.api.data.DataQuery;

import static java.util.Collections.emptyList;

public interface Report
{
    DataQuery WORLD = DataQuery.of("WorldUuid");
    DataQuery X = DataQuery.of("Position", "X");
    DataQuery Y = DataQuery.of("Position", "Y");
    DataQuery Z = DataQuery.of("Position", "Z");
    String CAUSE = "cause";
    String CAUSE_TYPE = "type";
    String CAUSE_PLAYER_UUID = "UUID";
    String CAUSE_NAME = "name";
    String CAUSE_TARGET = "target";
    String CAUSE_INDIRECT = "indirect";
    String LOCATION = "location";

    String MULTIACTION = "multiaction";
    String FULLCAUSELIST = "fullcauselist";
    String CAUSECONTEXT = "causecontext";

    static Optional<? extends Class<? extends Report>> getReport(String name)
    {
        Class<? extends Report> clazz = null;
        try
        {
            try
            {
                clazz = (Class<? extends Report>) Class.forName(name);
            }
            catch (ClassNotFoundException e)
            {
                try
                {
                    clazz = (Class<? extends Report>) Class.forName("org.cubeengine.module.vigil.report." + name);
                }
                catch (ClassNotFoundException e1)
                {
                    System.err.println("Cannot find Report for: " + name);
                }
            }
        }
        catch (ClassCastException e)
        {
            System.err.println("Class of " + name + " is not a Report class.");
        }
        return Optional.ofNullable(clazz);
    }

    enum CauseType
    {
        CAUSE_PLAYER,
        CAUSE_BLOCK, // Indirect
        CAUSE_TNT,
        CAUSE_DAMAGE,
        CAUSE_ENTITY
    }

    /**
     * Shows the action to given CommandSource
     *  @param actions   the action to show
     * @param receiver the CommandSource
     */
    void showReport(List<Action> actions, Receiver receiver);

    /**
     * Returns whether the actions can be grouped
     *
     *
     * @param lookup
     * @param action      the first action
     * @param otherAction
     * @param otherReport
     * @return whether the actions can be grouped
     */
    boolean group(Object lookup, Action action, Action otherAction, Report otherReport);

    /**
     * Applies the action to the world
     *
     * @param action   the action to apply
     * @param noOp true if permanent or false transient
     */
    void apply(Action action, boolean noOp);

    /**
     * Applies the reverse action to the world
     *
     * @param action the action to unapply
     * @param noOp true if permanent or false if transient
     */
    void unapply(Action action, boolean noOp);

    interface SimpleGrouping extends Report
    {
        @Override
        default boolean group(Object lookup, Action action, Action otherAction, Report otherReport)
        {
            if (!this.equals(otherReport))
            {
                return false;
            }
            if (!Recall.cause(action).equals(Recall.cause(otherAction)))
            {
                return false;
            }
            // TODO compare cause
            return !groupBy().stream().anyMatch(key -> !Objects.equals(action.getData(key), otherAction.getData(key)));
        }

        default List<String> groupBy()
        {
            return emptyList();
        }
    }

    interface ReportGrouping extends Report
    {

        @Override
        default boolean group(Object lookup, Action action, Action otherAction, Report otherReport)
        {
            if (!getReportsList().contains(otherReport.getClass()))
            {
                return false;
            }

            if (!Recall.cause(action).equals(Recall.cause(otherAction)))
            {
                return false;
            }

            return true;
        }

        List<Class<? extends Report>> getReportsList();
    }

    interface Readonly extends Report
    {
        default void apply(Action action, boolean noOp) {}
        default void unapply(Action action, boolean noOp) {}
    }

    interface NonGrouping extends Report
    {
        @Override
        default boolean group(Object lookup, Action action, Action otherAction, Report otherReport)
        {
            return false;
        }
    }
}
