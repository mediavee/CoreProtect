package net.coreprotect.thread;

import org.bukkit.scheduler.BukkitTask;

import net.coreprotect.CoreProtect;

public class Scheduler {

    private Scheduler() {
        throw new IllegalStateException("Scheduler class");
    }

    public static void scheduleSyncDelayedTask(CoreProtect plugin, Runnable task, Object regionData, int delay) {
        if (delay == 0) {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
        else {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static Object scheduleSyncRepeatingTask(CoreProtect plugin, Runnable task, Object regionData, int delay, int period) {
        return plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
    }

    public static void scheduleAsyncDelayedTask(CoreProtect plugin, Runnable task, int delay) {
        if (delay == 0) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
        else {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }

    public static void scheduleSyncDelayedTask(CoreProtect plugin, Runnable task, int delay) {
        scheduleSyncDelayedTask(plugin, task, null, delay);
    }

    public static void runTask(CoreProtect plugin, Runnable task) {
        scheduleSyncDelayedTask(plugin, task, null, 0);
    }

    public static void runTask(CoreProtect plugin, Runnable task, Object regionData) {
        scheduleSyncDelayedTask(plugin, task, regionData, 0);
    }

    public static void runTaskAsynchronously(CoreProtect plugin, Runnable task) {
        scheduleAsyncDelayedTask(plugin, task, 0);
    }

    public static void runTaskLaterAsynchronously(CoreProtect plugin, Runnable task, int delay) {
        scheduleAsyncDelayedTask(plugin, task, delay);
    }

    public static void cancelTask(Object task) {
        if (task instanceof BukkitTask) {
            BukkitTask bukkitTask = (BukkitTask) task;
            bukkitTask.cancel();
        }
        else if (task instanceof Integer) {
            CoreProtect.getInstance().getServer().getScheduler().cancelTask((Integer) task);
        }
    }
}
