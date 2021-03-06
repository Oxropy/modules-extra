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
package org.cubeengine.module.squelch.storage;

import org.cubeengine.libcube.util.Version;
import org.cubeengine.module.sql.database.Table;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

import java.util.UUID;

public class TableIgnorelist extends Table<IgnoreList>
{
    public static TableIgnorelist TABLE_IGNORE_LIST;
    public final TableField<IgnoreList, UUID> ID = createField("id", SQLDataType.UUID.nullable(false), this);
    public final TableField<IgnoreList, UUID> IGNORE = createField("ignore", SQLDataType.UUID.nullable(false), this);

    public TableIgnorelist()
    {
        super(IgnoreList.class, "chat_ignores", new Version(1));
        this.setPrimaryKey(ID, IGNORE);
        this.addFields(ID, IGNORE);
        TABLE_IGNORE_LIST = this;
    }
}
