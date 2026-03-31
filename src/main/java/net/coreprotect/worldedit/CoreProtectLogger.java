package net.coreprotect.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.World;

import net.coreprotect.config.Config;
import net.coreprotect.utility.ItemUtils;

public class CoreProtectLogger extends AbstractDelegateExtent {
    private final Actor eventActor;
    private final World eventWorld;
    private final Extent eventExtent;

    protected CoreProtectLogger(Actor actor, World world, Extent extent) {
        super(extent);
        this.eventActor = actor;
        this.eventWorld = world;
        this.eventExtent = extent;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean setBlock(Vector position, BaseBlock block) throws WorldEditException {
        org.bukkit.World world = Bukkit.getWorld(eventWorld.getName());
        if (world == null || !Config.getConfig(world).WORLDEDIT) {
            return eventExtent.setBlock(position, block);
        }

        BaseBlock oldBlock = eventExtent.getBlock(position);
        Material oldType = Material.getMaterial(oldBlock.getId());
        if (oldType == null) {
            oldType = Material.AIR;
        }
        Location location = new Location(world, position.getBlockX(), position.getBlockY(), position.getBlockZ());

        ItemStack[] containerData = ItemUtils.getContainerContents(oldType, null, location);

        if (eventExtent.setBlock(position, block)) {
            WorldEditLogger.postProcess(eventActor, position, location, block, oldBlock, oldType, containerData);
            return true;
        }

        return false;
    }
}
