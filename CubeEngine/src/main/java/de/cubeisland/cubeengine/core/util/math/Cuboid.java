package de.cubeisland.cubeengine.core.util.math;

/**
 * Represents a Cuboid specified by two corners
 *
 * @author Phillip Schichtel
 */
public class Cuboid
{
    private final Vector3 corner1;
    private final Vector3 corner2;

    /**
     * Creates a Cuboid with the 2 Vectors
     *
     * @param corner1 Vektor to the first corner
     * @param corner2 Vektor to the second corner
     */
    public Cuboid(Vector3 corner1, Vector3 corner2)
    {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    /**
     * Gets a Vektor3 pointing to the minium point
     *
     * @return the Vektor pointing to the minimum point
     */
    public Vector3 getMinimumPoint()
    {
        return new Vector3(
            Math.min(this.corner1.x, this.corner2.x),
            Math.min(this.corner1.y, this.corner2.y),
            Math.min(this.corner1.z, this.corner2.z));
    }

    /**
     * Gets a Vektor3 pointing to the maximum point
     *
     * @return the Vektor pointing to the maximum point
     */
    public Vector3 getMaximumPoint()
    {
        return new Vector3(
            Math.max(this.corner1.x, this.corner2.x),
            Math.max(this.corner1.y, this.corner2.y),
            Math.max(this.corner1.z, this.corner2.z));
    }

    /**
     * Check whether the given point is in this Cuboid
     *
     * @param point the point to check
     * @return whether the point is in the cuboid or not
     */
    public boolean contains(Vector3 point)
    {
        Vector3 min = this.getMinimumPoint();
        Vector3 max = this.getMaximumPoint();

        return (point.x >= min.x && point.x <= max.x
            && point.y >= min.y && point.y <= max.y
            && point.z >= min.z && point.z <= max.z);
    }
}