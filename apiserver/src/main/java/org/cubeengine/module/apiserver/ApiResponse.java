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
package org.cubeengine.module.apiserver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.google.common.base.Preconditions;

/**
 * this class wrapps the data which will be send to the client
 */
public final class ApiResponse
{
    private final Map<String, List<String>> headers = new HashMap<>();
    private Object content;

    /**
     * Initializes the the response
     */
    public ApiResponse()
    {
        this(null);
    }

    public ApiResponse(Object content)
    {
        this.content = content;
    }

    /**
     * Returns an header
     *
     * @param name the name of the header
     * @return the value of the header
     */
    public List<String> getHeader(String name)
    {
        if (name != null)
        {
            return this.headers.get(name.toLowerCase());
        }
        return null;
    }

    /**
     * Sets an header
     *
     * @param name  the name of the param
     * @param value the value of the header
     * @return fluent interface
     */
    public ApiResponse setHeader(String name, String value)
    {
        Preconditions.checkNotNull(name, "The name must not be null!");

        if (value == null)
        {
            this.headers.remove(name);
        }
        else
        {
            List<String> values = new LinkedList<>();
            values.add(value);
            this.headers.put(name, values);
        }
        return this;
    }

    public ApiResponse addHeader(String name, String value)
    {
        Preconditions.checkNotNull(name, "The name must not be null!");
        Preconditions.checkNotNull(value, "The alue must not be null!");

        List<String> values = this.headers.get(name);
        if (values == null)
        {
            this.headers.put(name, values = new LinkedList<>());
        }
        values.add(value);

        return this;
    }

    /**
     * Returns a copy of the header map
     *
     * @return the header map
     */
    public Map<String, List<String>> getHeaders()
    {
        return this.headers;
    }

    /**
     * Clears the headers
     *
     * @return fluent interface
     */
    public ApiResponse clearHeaders()
    {
        this.headers.clear();
        return this;
    }

    /**
     * Sets the whole header map
     *
     * @param headers the header map
     * @return fluent interface
     */
    public ApiResponse setHeaders(Map<String, List<String>> headers)
    {
        if (headers != null)
        {
            for (Map.Entry<String, List<String>> header : headers.entrySet())
            {
                this.headers.put(header.getKey().toLowerCase(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Returns the content of the response
     *
     * @return the response object
     */
    public Object getContent()
    {
        return this.content;
    }

    /**
     * Sets the response content#
     *
     * @param content an object which will per serialized
     * @return fluent interface
     */
    public ApiResponse setContent(Object content)
    {
        this.content = content;
        return this;
    }
}
