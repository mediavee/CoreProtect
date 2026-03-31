package net.coreprotect.patch.script;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.patch.Patch;
import net.coreprotect.utility.BlockUtils;
import net.coreprotect.utility.MaterialUtils;

public class __2_18_0 {

    protected static boolean createIndexes = true;

    protected static boolean patch(Statement statement) {
        try {

            try {
                if (Config.getGlobal().MYSQL) {
                    statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "block ADD COLUMN blockdata BLOB");
                }
            }
            catch (Exception e) {
                String error = e.getMessage().toLowerCase();
                if (!error.contains("duplicate") && !error.contains("error 1060")) {
                    e.printStackTrace();
                    return false;
                }
            }

            if (!Patch.continuePatch()) {
                return false;
            }

            String query = "SELECT rowid, id, material FROM " + ConfigHandler.prefix + "material_map WHERE material LIKE 'minecraft:legacy_%' LIMIT 0, 1";
            String preparedBlockQuery = "SELECT rowid as id, data, blockdata FROM " + ConfigHandler.prefix + "block WHERE type = ? AND action < '3'";
            String preparedContainerQuery = "SELECT rowid as id FROM " + ConfigHandler.prefix + "container WHERE type = ?";
            String preparedBlockUpdateQuery = "UPDATE " + ConfigHandler.prefix + "block SET type = ?, blockdata = ? WHERE rowid = ?";
            String preparedContainerUpdateQuery = "UPDATE " + ConfigHandler.prefix + "container SET type = ? WHERE rowid = ?";
            String preparedMaterialDeleteQuery = "DELETE FROM " + ConfigHandler.prefix + "material_map WHERE rowid = ?";

            boolean hasLegacy = true;
            while (hasLegacy) {
                hasLegacy = false;

                PreparedStatement preparedBlockStatement = statement.getConnection().prepareStatement(preparedBlockQuery);
                PreparedStatement preparedBlockUpdateStatement = statement.getConnection().prepareStatement(preparedBlockUpdateQuery);
                PreparedStatement preparedContainerStatement = statement.getConnection().prepareStatement(preparedContainerQuery);
                PreparedStatement preparedContainerUpdateStatement = statement.getConnection().prepareStatement(preparedContainerUpdateQuery);
                PreparedStatement preparedMaterialDeleteStatement = statement.getConnection().prepareStatement(preparedMaterialDeleteQuery);
                Database.beginTransaction(statement, Config.getGlobal().MYSQL);
                try {
                    ResultSet resultSet = statement.executeQuery(query);
                    while (resultSet.next()) {
                        int blockCount = 1;
                        int containerCount = 1;
                        int rowid = resultSet.getInt("rowid");
                        int oldID = resultSet.getInt("id");
                        String materialName = resultSet.getString("material");

                        boolean legacy = true;
                        switch (materialName) {
                            case "minecraft:legacy_wall_sign":
                                materialName = "minecraft:oak_wall_sign";
                                legacy = false;
                                break;
                            case "minecraft:legacy_skull":
                                materialName = "minecraft:skeleton_skull";
                                legacy = false;
                                break;
                            case "minecraft:legacy_long_grass":
                                materialName = "minecraft:grass";
                                legacy = false;
                                break;
                            case "minecraft:legacy_double_plant":
                                materialName = "minecraft:tall_grass";
                                legacy = false;
                                break;
                        }

                        Material material = Material.matchMaterial(materialName);
                        int newID = MaterialUtils.getBlockId(material);

                        preparedBlockStatement.setInt(1, oldID);
                        ResultSet blockResults = preparedBlockStatement.executeQuery();
                        while (blockResults.next()) {
                            int blockID = blockResults.getInt("id");
                            int blockData = blockResults.getInt("data");
                            byte[] blockBlockData = blockResults.getBytes("blockdata");

                            Material validatedMaterial = material;
                            int validatedID = newID;
                            if (validatedMaterial == Material.WOOL) {
                                // In 1.8, wool is a single material with data values for colors
                                // No conversion needed - data value already encodes the color
                            }

                            // In 1.8, no BlockData conversion needed - data byte is sufficient

                            preparedBlockUpdateStatement.setInt(1, validatedID);
                            preparedBlockUpdateStatement.setObject(2, blockBlockData);
                            preparedBlockUpdateStatement.setInt(3, blockID);
                            preparedBlockUpdateStatement.addBatch();
                            if (blockCount % 1000 == 0) {
                                preparedBlockUpdateStatement.executeBatch();
                            }
                            blockCount++;
                        }
                        preparedBlockUpdateStatement.executeBatch();
                        blockResults.close();

                        preparedContainerStatement.setInt(1, oldID);
                        ResultSet containerResults = preparedContainerStatement.executeQuery();
                        while (containerResults.next()) {
                            int containerID = containerResults.getInt("id");
                            preparedContainerUpdateStatement.setInt(1, newID);
                            preparedContainerUpdateStatement.setInt(2, containerID);
                            preparedContainerUpdateStatement.addBatch();
                            if (containerCount % 1000 == 0) {
                                preparedContainerUpdateStatement.executeBatch();
                            }
                            containerCount++;
                        }
                        preparedContainerUpdateStatement.executeBatch();
                        containerResults.close();

                        preparedMaterialDeleteStatement.setInt(1, rowid);
                        preparedMaterialDeleteStatement.executeUpdate();
                        hasLegacy = true;
                    }
                    resultSet.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                Database.commitTransaction(statement, Config.getGlobal().MYSQL);

                preparedBlockStatement.close();
                preparedBlockUpdateStatement.close();
                preparedContainerStatement.close();
                preparedContainerUpdateStatement.close();
                preparedMaterialDeleteStatement.close();

                if (!Patch.continuePatch()) {
                    return false;
                }
            }

            if (createIndexes) {
                try {
                    if (Config.getGlobal().MYSQL) {
                        statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "art_map ADD INDEX(id)");
                        statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "entity_map ADD INDEX(id)");
                        statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "material_map ADD INDEX(id)");
                        statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "world ADD INDEX(id)");
                        statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "blockdata_map ADD INDEX(id)");
                    }
                    else {
                        statement.executeUpdate("CREATE INDEX IF NOT EXISTS art_map_id_index ON " + ConfigHandler.prefix + "art_map(id);");
                        statement.executeUpdate("CREATE INDEX IF NOT EXISTS blockdata_map_id_index ON " + ConfigHandler.prefix + "blockdata_map(id);");
                        statement.executeUpdate("CREATE INDEX IF NOT EXISTS entity_map_id_index ON " + ConfigHandler.prefix + "entity_map(id);");
                        statement.executeUpdate("CREATE INDEX IF NOT EXISTS material_map_id_index ON " + ConfigHandler.prefix + "material_map(id);");
                        statement.executeUpdate("CREATE INDEX IF NOT EXISTS world_id_index ON " + ConfigHandler.prefix + "world(id);");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    protected static Material getWoolColor(int data) {
        // In 1.8, wool is a single material with data values for colors
        return Material.WOOL;
    }

    private static BlockFace getLegacyDirection(int data) {
        switch (data) {
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            case 5:
                return BlockFace.EAST;
            default:
                return BlockFace.NORTH;
        }
    }

    private static BlockFace getLegacyRotation(int data) {
        switch (data) {
            case 1:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 2:
                return BlockFace.SOUTH_WEST;
            case 3:
                return BlockFace.WEST_SOUTH_WEST;
            case 4:
                return BlockFace.WEST;
            case 5:
                return BlockFace.WEST_NORTH_WEST;
            case 6:
                return BlockFace.NORTH_WEST;
            case 7:
                return BlockFace.NORTH_NORTH_WEST;
            case 8:
                return BlockFace.NORTH;
            case 9:
                return BlockFace.NORTH_NORTH_EAST;
            case 10:
                return BlockFace.NORTH_EAST;
            case 11:
                return BlockFace.EAST_NORTH_EAST;
            case 12:
                return BlockFace.EAST;
            case 13:
                return BlockFace.EAST_SOUTH_EAST;
            case 14:
                return BlockFace.SOUTH_EAST;
            case 15:
                return BlockFace.SOUTH_SOUTH_EAST;
            default:
                return BlockFace.SOUTH;
        }
    }

}
