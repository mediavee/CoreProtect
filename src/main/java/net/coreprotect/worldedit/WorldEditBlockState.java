package net.coreprotect.worldedit;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public final class WorldEditBlockState implements BlockState {

    private final Location location;
    private Material material;
    private final byte data;

    public WorldEditBlockState(Location loc, Material type, byte data) {
        this.location = loc;
        this.material = type;
        this.data = data;
    }

    @Override
    public Block getBlock() {
        return location.getBlock();
    }

    @Override
    public MaterialData getData() {
        return material.getNewData(data);
    }

    @Override
    public Material getType() {
        return material;
    }

    @Override
    public int getTypeId() {
        return material.getId();
    }

    @Override
    public boolean setTypeId(int type) {
        this.material = Material.getMaterial(type);
        return true;
    }

    @Override
    public byte getLightLevel() {
        return 0;
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(location.getWorld());
            loc.setX(location.getX());
            loc.setY(location.getY());
            loc.setZ(location.getZ());
            loc.setYaw(location.getYaw());
            loc.setPitch(location.getPitch());
        }
        return loc;
    }

    @Override
    public Chunk getChunk() {
        return location.getChunk();
    }

    @Override
    public void setData(MaterialData data) {
    }

    @Override
    public void setType(Material type) {
        this.material = type;
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean update(boolean force) {
        return false;
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return false;
    }

    @Override
    public byte getRawData() {
        return data;
    }

    @Override
    public void setRawData(byte data) {
    }

    @Override
    public boolean isPlaced() {
        return false;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return null;
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
    }
}
