package net.coreprotect.paper;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.papermc.lib.PaperLib;
import net.coreprotect.bukkit.BukkitAdapter;

public class PaperAdapter implements PaperInterface {

    public static PaperInterface ADAPTER;

    public static void loadAdapter() {
        ADAPTER = new PaperAdapter();
    }

    @Override
    public InventoryHolder getHolder(Inventory holder, boolean useSnapshot) {
        return holder.getHolder();
    }

    @Override
    public boolean isStopping(Server server) {
        return false;
    }

    @Override
    public String getLine(Sign sign, int line) {
        return BukkitAdapter.ADAPTER.getLine(sign, line);
    }

    @Override
    public void teleportAsync(Entity entity, Location location) {
        PaperLib.teleportAsync(entity, location);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getSkullOwner(Skull skull) {
        String owner = skull.getOwner();
        return owner != null ? owner : "";
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setSkullOwner(Skull skull, String owner) {
        if (owner != null && !owner.isEmpty()) {
            skull.setOwner(owner);
        }
    }

    @Override
    public String getSkullSkin(Skull skull) {
        return null;
    }

    @Override
    public void setSkullSkin(Skull skull, String skin) {
    }
}
