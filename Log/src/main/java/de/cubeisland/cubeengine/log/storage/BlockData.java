package de.cubeisland.cubeengine.log.storage;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;

public class BlockData
{
    public Material mat;
    public Byte data;

    public BlockData(Material mat, Byte data)
    {
        this.mat = mat;
        this.data = data;
    }

    public BlockData(int material, byte data)
    {
        this(Material.getMaterial(material), data);
    }

    public static BlockData get(Integer mat, Byte data)
    {
        if (mat == null)
        {
            return null;
        }
        return new BlockData(Material.getMaterial(mat), data);
    }

    public static BlockData get(BlockState state)
    {
        if (state == null)
        {
            return null;
        }
        if (state.getType() == Material.NOTE_BLOCK)
        {
            return new BlockData(state.getType(), ((NoteBlock)state).getRawNote());
        }
        return new BlockData(state.getType(), state.getRawData());
    }

    public static BlockData get(BlockState state, byte customData)
    {
        if (state == null)
        {
            return null;
        }
        return new BlockData(state.getType(), customData);
    }

    public BlockData(Block block)
    {
        this.mat = block.getType();
        this.data = block.getData();
    }

    public BlockState applyTo(BlockState state)
    {
        state.setType(mat);
        state.setRawData(data);
        return state;
    }

    @Override
    public String toString()
    {
        //TODO get name with reverse matcher
        return mat.name() + ":" + data;
    }
}
