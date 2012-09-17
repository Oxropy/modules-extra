package de.cubeisland.cubeengine.core.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Anselm Brehme
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Revision
{
    /**
     * The Revision of this Config
     *
     * @return the revision
     */
    public int value();
}