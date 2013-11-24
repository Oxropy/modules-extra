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
package de.cubeisland.engine.core.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.cubeisland.engine.core.logging.Log;

public class LogbackLog extends Log
{
    private final Logger logger;

    public LogbackLog(Logger logger)
    {
        this.logger = logger;
    }

    final Logger getHandle()
    {
        return this.logger;
    }

    public void log(Level level, Throwable throwable, String message, Object... args)
    {
        if (level == Level.TRACE)
        {
            this.logger.trace(message, args);
            if (throwable != null)
            {
                this.logger.trace(throwable.getLocalizedMessage(), throwable);
            }
            return;
        }
        else if (level == Level.DEBUG)
        {
            this.logger.debug(message, args);
        }
        else if (level == Level.INFO)
        {
            this.logger.info(message, args);
        }
        else if (level == Level.WARN)
        {
            this.logger.warn(message, args);
        }
        else if (level == Level.ERROR)
        {
            this.logger.error(message, args);
        }
        if (throwable != null)
        {
            this.logger.debug(throwable.getLocalizedMessage(), throwable);
        }
    }

    @Override
    public void trace(Throwable throwable, String message, Object... args)
    {
        this.log(Level.TRACE, throwable, message, args);
    }

    @Override
    public void debug(Throwable throwable, String message, Object... args)
    {
        this.log(Level.DEBUG, throwable, message, args);
    }

    @Override
    public void info(Throwable throwable, String message, Object... args)
    {
        this.log(Level.INFO, throwable, message, args);
    }

    @Override
    public void warn(Throwable throwable, String message, Object... args)
    {
        this.log(Level.WARN, throwable, message, args);
    }

    @Override
    public void error(Throwable throwable, String message, Object... args)
    {
        this.log(Level.ERROR, throwable, message, args);
    }

    @Override
    public void setLevel(de.cubeisland.engine.core.logging.Level level)
    {
        this.logger.setLevel(Level.toLevel(level.name()));
    }

    @Override
    public de.cubeisland.engine.core.logging.Level getLevel()
    {
        return de.cubeisland.engine.core.logging.Level.toLevel(this.logger.getLevel().toString());
    }
}