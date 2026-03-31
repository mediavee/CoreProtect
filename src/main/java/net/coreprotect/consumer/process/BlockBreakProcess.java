package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;

import net.coreprotect.database.logger.BlockBreakLogger;
import net.coreprotect.database.logger.SkullBreakLogger;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.MaterialUtils;

class BlockBreakProcess {

    static void process(PreparedStatement preparedStmt, PreparedStatement preparedStmtSkulls, int batchCount, int processId, int id, Material blockType, int blockDataId, Material replaceType, int forceData, String user, Object object, String blockData) {
        if (object instanceof BlockState) {
            BlockState block = (BlockState) object;
            List<Object> meta = BlockUtils.processMeta(block);
            if (block instanceof Skull) {
                SkullBreakLogger.log(preparedStmt, preparedStmtSkulls, batchCount, user, block);
            }
            else {
                BlockBreakLogger.log(preparedStmt, batchCount, user, block.getLocation(), MaterialUtils.getBlockId(blockType), blockDataId, meta, null, blockData);

                // When a support block is broken and a door bottom half is detected above (blockNumber==5),
                // also log the door top half at Y+1 since the scanner doesn't reach that far
                if (forceData == 5
                        && (BlockGroup.DOORS.contains(blockType) || blockType == Material.IRON_DOOR_BLOCK)
                        && (replaceType == null || (!BlockGroup.DOORS.contains(replaceType) && replaceType != Material.IRON_DOOR_BLOCK))
                        && blockDataId < 8) {
                    int topData = blockDataId | 0x8;
                    try {
                        Block topBlock = block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ());
                        Material topType = topBlock.getType();
                        if (BlockGroup.DOORS.contains(topType) || topType == Material.IRON_DOOR_BLOCK) {
                            topData = topBlock.getData();
                        }
                    }
                    catch (Exception ignored) {
                    }
                    Location topLocation = block.getLocation().clone();
                    topLocation.setY(topLocation.getY() + 1);
                    BlockBreakLogger.log(preparedStmt, batchCount, user, topLocation, MaterialUtils.getBlockId(blockType), topData, null, null, null);
                }
            }
        }
    }
}
