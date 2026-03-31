package net.coreprotect.listener.block;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import net.coreprotect.consumer.Queue;

public final class BlockFadeListener extends Queue implements Listener {

    @EventHandler
    protected void onBlockFade(BlockFadeEvent event) {
        // Only handled turtle eggs in modern versions — no-op in 1.8
    }

}
