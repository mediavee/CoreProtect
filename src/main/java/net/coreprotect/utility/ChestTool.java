package net.coreprotect.utility;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class ChestTool {

    private ChestTool() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * In 1.8, double chests are automatically formed by adjacency.
     * No BlockData manipulation is needed.
     */
    public static void updateDoubleChest(Block block, Material type, byte data, boolean forceValidation) {
        // No-op: 1.8 handles double chest pairing automatically
    }

}
