package net.coreprotect.listener.entity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;

import net.coreprotect.consumer.Queue;

public final class EntityInteractListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityInteractEntity(EntityInteractEvent event) {
        // Only handled turtle eggs in modern versions — no-op in 1.8
    }

}
