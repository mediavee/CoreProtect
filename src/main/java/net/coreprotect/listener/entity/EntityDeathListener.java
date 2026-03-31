package net.coreprotect.listener.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.projectiles.ProjectileSource;

import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.thread.Scheduler;

public final class EntityDeathListener extends Queue implements Listener {

    public static void parseEntityKills(String message) {
        message = message.trim().toLowerCase(Locale.ROOT);
        if (!message.contains(" ")) {
            return;
        }

        String[] args = message.split(" ");
        if (args.length < 2 || !args[0].replaceFirst("/", "").equals("kill") || !args[1].startsWith("@e")) {
            return;
        }

        List<LivingEntity> entityList = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            List<LivingEntity> livingEntities = world.getLivingEntities();
            for (LivingEntity entity : livingEntities) {
                if (entity instanceof Player) {
                    continue;
                }

                if (entity.isValid()) {
                    entityList.add(entity);
                }
            }
        }

        for (LivingEntity entity : entityList) {
            Scheduler.runTask(CoreProtect.getInstance(), () -> {
                if (entity != null && entity.isDead()) {
                    logEntityDeath(entity, "#command");
                }
            }, entity);
        }
    }

    protected static void logEntityDeath(LivingEntity entity, String e) {
        if (!Config.getConfig(entity.getWorld()).ENTITY_KILLS) {
            return;
        }

        EntityDamageEvent damage = entity.getLastDamageCause();
        if (damage == null) {
            return;
        }

        boolean isCommand = (damage.getCause() == DamageCause.VOID && entity.getLocation().getBlockY() >= 0);
        if (e == null) {
            e = isCommand ? "#command" : "";
        }

        List<DamageCause> validDamageCauses = Arrays.asList(DamageCause.SUICIDE, DamageCause.POISON, DamageCause.THORNS, DamageCause.MAGIC, DamageCause.WITHER);

        boolean skip = true;
        EntityDamageEvent.DamageCause cause = damage.getCause();
        if (!Config.getConfig(entity.getWorld()).SKIP_GENERIC_DATA || (!(entity instanceof Zombie) && !(entity instanceof Skeleton)) || (validDamageCauses.contains(cause) || cause.name().equals("KILL"))) {
            skip = false;
        }

        if (damage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attack = (EntityDamageByEntityEvent) damage;
            Entity attacker = attack.getDamager();

            if (attacker instanceof Player) {
                Player player = (Player) attacker;
                e = player.getName();
            }
            else if (attacker instanceof Arrow) {
                Arrow arrow = (Arrow) attacker;
                ProjectileSource shooter = arrow.getShooter();

                if (shooter instanceof Player) {
                    Player player = (Player) shooter;
                    e = player.getName();
                }
                else if (shooter instanceof LivingEntity) {
                    EntityType entityType = ((LivingEntity) shooter).getType();
                    if (entityType != null) { // Check for MyPet plugin
                        String name = entityType.name().toLowerCase(Locale.ROOT);
                        e = "#" + name;
                    }
                }
            }
            else if (attacker instanceof ThrownPotion) {
                ThrownPotion potion = (ThrownPotion) attacker;
                ProjectileSource shooter = potion.getShooter();

                if (shooter instanceof Player) {
                    Player player = (Player) shooter;
                    e = player.getName();
                }
                else if (shooter instanceof LivingEntity) {
                    EntityType entityType = ((LivingEntity) shooter).getType();
                    if (entityType != null) { // Check for MyPet plugin
                        String name = entityType.name().toLowerCase(Locale.ROOT);
                        e = "#" + name;
                    }
                }
            }
            else if (attacker.getType().name() != null) {
                e = "#" + attacker.getType().name().toLowerCase(Locale.ROOT);
            }
        }
        else {
            if (cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
                e = "#fire";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
                if (!skip) {
                    e = "#fire";
                }
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.LAVA)) {
                e = "#lava";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                e = "#explosion";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.MAGIC)) {
                e = "#magic";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.WITHER)) {
                e = "#wither_effect";
            }
            else if (!cause.name().contains("_")) {
                e = "#" + cause.name().toLowerCase(Locale.ROOT);
            }
        }

        if (entity instanceof ArmorStand) {
            Location entityLocation = entity.getLocation();
            if (!Config.getConfig(entityLocation.getWorld()).ITEM_TRANSACTIONS) {
                entityLocation.setY(entityLocation.getY() + 0.99);
                Block block = entityLocation.getBlock();
                Queue.queueBlockBreak(e, block.getState(), Material.ARMOR_STAND, null, (int) entityLocation.getYaw());
            }
            /*
            else if (isCommand) {
                entityLocation.setY(entityLocation.getY() + 0.99);
                Block block = entityLocation.getBlock();
                Database.containerBreakCheck(e, Material.ARMOR_STAND, entity, null, block.getLocation());
                Queue.queueBlockBreak(e, block.getState(), Material.ARMOR_STAND, null, (int) entityLocation.getYaw());
            }
            */
            return;
        }

        EntityType entity_type = entity.getType();
        if (e.length() == 0) {
            // assume killed self
            if (!skip) {
                if (!(entity instanceof Player) && entity_type.name() != null) {
                    // Player player = (Player)entity;
                    // e = player.getName();
                    e = "#" + entity_type.name().toLowerCase(Locale.ROOT);
                }
                else if (entity instanceof Player) {
                    e = entity.getName();
                }
            }
        }

        if (e.startsWith("#wither") && !e.equals("#wither_effect")) {
            e = "#wither";
        }

        if (e.startsWith("#enderdragon")) {
            e = "#enderdragon";
        }

        if (e.startsWith("#primedtnt") || e.startsWith("#tnt")) {
            e = "#tnt";
        }

        if (e.startsWith("#lightning")) {
            e = "#lightning";
        }

        if (e.length() > 0) {
            List<Object> data = new ArrayList<>();
            List<Object> age = new ArrayList<>();
            List<Object> tame = new ArrayList<>();
            List<Object> attributes = new ArrayList<>();
            List<Object> details = new ArrayList<>();
            List<Object> info = new ArrayList<>();
            EntityType type = entity_type;

            // Basic LivingEntity attributes
            details.add(entity.getRemoveWhenFarAway());
            details.add(entity.getCanPickupItems());

            if (entity instanceof Ageable) {
                Ageable ageable = (Ageable) entity;
                age.add(ageable.getAge());
                age.add(ageable.getAgeLock());
                age.add(ageable.isAdult());
                age.add(ageable.canBreed());
                age.add(null);
            }

            if (entity instanceof Tameable) {
                Tameable tameable = (Tameable) entity;
                tame.add(tameable.isTamed());
                if (tameable.isTamed()) {
                    if (tameable.getOwner() != null) {
                        tame.add(tameable.getOwner().getName());
                    }
                }
            }

            if (entity instanceof Creeper) {
                Creeper creeper = (Creeper) entity;
                info.add(creeper.isPowered());
            }
            else if (entity instanceof Enderman) {
                Enderman enderman = (Enderman) entity;
                info.add(null);

                try {
                    @SuppressWarnings("deprecation")
                    MaterialData carriedMaterial = enderman.getCarriedMaterial();
                    if (carriedMaterial != null) {
                        info.add(carriedMaterial.getItemType().name());
                    }
                }
                catch (Exception endermanException) {
                }
            }
            else if (entity instanceof IronGolem) {
                IronGolem irongolem = (IronGolem) entity;
                info.add(irongolem.isPlayerCreated());
            }
            else if (entity instanceof Pig) {
                Pig pig = (Pig) entity;
                info.add(pig.hasSaddle());
            }
            else if (entity instanceof Sheep) {
                Sheep sheep = (Sheep) entity;
                info.add(sheep.isSheared());
                info.add(sheep.getColor());
            }
            else if (entity instanceof Skeleton) {
                info.add(null);
            }
            else if (entity instanceof Slime) {
                Slime slime = (Slime) entity;
                info.add(slime.getSize());
            }
            else if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                info.add(villager.getProfession().name());
            }
            else if (entity instanceof Wolf) {
                Wolf wolf = (Wolf) entity;
                info.add(wolf.isSitting());
                info.add(wolf.getCollarColor());
            }
            else if (entity instanceof Zombie) {
                Zombie zombie = (Zombie) entity;
                info.add(zombie.isBaby());
                info.add(null);
                info.add(null);
            }
            else if (entity instanceof Horse) {
                Horse horse = (Horse) entity;
                info.add(horse.getColor());
                info.add(horse.getStyle());

                ItemStack saddle = horse.getInventory().getSaddle();
                if (saddle != null) {
                    info.add(saddle.serialize());
                }
                else {
                    info.add(null);
                }

                ItemStack horseArmor = horse.getInventory().getArmor();
                if (horseArmor != null) {
                    ItemStack armor = horseArmor.clone();
                    ItemMeta itemMeta = armor.getItemMeta();
                    Color color = null;
                    if (itemMeta instanceof LeatherArmorMeta) {
                        LeatherArmorMeta meta = (LeatherArmorMeta) itemMeta;
                        color = meta.getColor();
                        meta.setColor(null);
                        armor.setItemMeta(meta);
                    }
                    info.add(armor.serialize());
                    if (color != null) {
                        info.add(color.serialize());
                    }
                    else {
                        info.add(null);
                    }
                }
                else {
                    info.add(null);
                    info.add(null);
                }
            }

            data.add(age);
            data.add(tame);
            data.add(info);
            data.add(entity.isCustomNameVisible());
            data.add(entity.getCustomName());
            data.add(attributes);
            data.add(details);

            if (!(entity instanceof Player)) {
                Queue.queueEntityKill(e, entity.getLocation(), data, type);
            }
            else {
                Queue.queuePlayerKill(e, entity.getLocation(), entity.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        /*
        System.out.println("ENTITY DEATH - " + event.getEntity().getName());
        if (event.getEntity().getKiller() != null) {
            System.out.println("^ (killer): " + event.getEntity().getKiller().getName());
        }
        else if (event.getEntity().getLastDamageCause() != null) {
            System.out.println("^ (damage cause): " + event.getEntity().getLastDamageCause().getEntity().getName());
        }
        */

        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        logEntityDeath(entity, null);
    }
}
