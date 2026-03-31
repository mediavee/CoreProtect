package net.coreprotect.utility.entity;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.WorldUtils;

public class EntityUtil {

    private EntityUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void spawnEntity(final BlockState block, final EntityType type, final List<Object> list) {
        if (type == null) {
            return;
        }
        Scheduler.runTask(CoreProtect.getInstance(), () -> {
            try {
                Location location = block.getLocation();
                location.setX(location.getX() + 0.50);
                location.setZ(location.getZ() + 0.50);
                Entity entity = block.getLocation().getWorld().spawnEntity(location, type);

                if (list.isEmpty()) {
                    return;
                }

                @SuppressWarnings("unchecked")
                List<Object> age = (List<Object>) list.get(0);
                @SuppressWarnings("unchecked")
                List<Object> tame = (List<Object>) list.get(1);
                @SuppressWarnings("unchecked")
                List<Object> data = (List<Object>) list.get(2);

                if (list.size() >= 5) {
                    entity.setCustomNameVisible((Boolean) list.get(3));
                    entity.setCustomName((String) list.get(4));
                }

                int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
                int wid = WorldUtils.getWorldId(block.getWorld().getName());
                String token = "" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid + "." + type.name() + "";
                CacheHandler.entityCache.put(token, new Object[] { unixtimestamp, entity.getEntityId() });

                if (entity instanceof Ageable) {
                    int count = 0;
                    Ageable ageable = (Ageable) entity;
                    for (Object value : age) {
                        if (count == 0) {
                            int set = (Integer) value;
                            ageable.setAge(set);
                        }
                        else if (count == 1) {
                            boolean set = (Boolean) value;
                            ageable.setAgeLock(set);
                        }
                        else if (count == 2) {
                            boolean set = (Boolean) value;
                            if (set) {
                                ageable.setAdult();
                            }
                            else {
                                ageable.setBaby();
                            }
                        }
                        else if (count == 3) {
                            boolean set = (Boolean) value;
                            ageable.setBreed(set);
                        }
                        else if (count == 4 && value != null) {
                            double set = (Double) value;
                            ageable.setMaxHealth(set);
                        }
                        count++;
                    }
                }
                if (entity instanceof Tameable) {
                    int count = 0;
                    Tameable tameable = (Tameable) entity;
                    for (Object value : tame) {
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            tameable.setTamed(set);
                        }
                        else if (count == 1) {
                            String set = (String) value;
                            if (set.length() > 0) {
                                Player owner = Bukkit.getServer().getPlayer(set);
                                if (owner == null) {
                                    OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(set);
                                    if (offlinePlayer != null) {
                                        tameable.setOwner(offlinePlayer);
                                    }
                                }
                                else {
                                    tameable.setOwner(owner);
                                }
                            }
                        }
                        count++;
                    }
                }

                if (entity instanceof LivingEntity && list.size() >= 7) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    @SuppressWarnings("unchecked")
                    List<Object> details = (List<Object>) list.get(6);
                    int count = 0;
                    for (Object value : details) {
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            livingEntity.setRemoveWhenFarAway(set);
                        }
                        else if (count == 1) {
                            boolean set = (Boolean) value;
                            livingEntity.setCanPickupItems(set);
                        }
                        count++;
                    }
                }

                int count = 0;
                for (Object value : data) {
                    if (entity instanceof Creeper) {
                        Creeper creeper = (Creeper) entity;
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            creeper.setPowered(set);
                        }
                    }
                    else if (entity instanceof Enderman) {
                        Enderman enderman = (Enderman) entity;
                        if (count == 1) {
                            String blockDataString = (String) value;
                            if (blockDataString != null && !blockDataString.isEmpty()) {
                                try {
                                    Material material = Material.getMaterial(blockDataString.split("\\[")[0].replace("minecraft:", "").toUpperCase());
                                    if (material != null) {
                                        enderman.setCarriedMaterial(new MaterialData(material));
                                    }
                                }
                                catch (Exception e) {
                                    // ignore invalid block data
                                }
                            }
                        }
                    }
                    else if (entity instanceof IronGolem) {
                        IronGolem irongolem = (IronGolem) entity;
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            irongolem.setPlayerCreated(set);
                        }
                    }
                    else if (entity instanceof Pig) {
                        Pig pig = (Pig) entity;
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            pig.setSaddle(set);
                        }
                    }
                    else if (entity instanceof Sheep) {
                        Sheep sheep = (Sheep) entity;
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            sheep.setSheared(set);
                        }
                        else if (count == 1) {
                            DyeColor set = (DyeColor) value;
                            sheep.setColor(set);
                        }
                    }
                    else if (entity instanceof Slime) {
                        Slime slime = (Slime) entity;
                        if (count == 0) {
                            int set = (Integer) value;
                            slime.setSize(set);
                        }
                    }
                    else if (entity instanceof Villager) {
                        Villager villager = (Villager) entity;
                        if (count == 0) {
                            if (value instanceof String) {
                                try {
                                    value = Profession.valueOf(((String) value).toUpperCase());
                                }
                                catch (Exception e) {
                                    // ignore
                                }
                            }
                            if (value instanceof Profession) {
                                Profession set = (Profession) value;
                                villager.setProfession(set);
                            }
                        }
                    }
                    else if (entity instanceof Wolf) {
                        Wolf wolf = (Wolf) entity;
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            wolf.setSitting(set);
                        }
                        else if (count == 1) {
                            DyeColor set = (DyeColor) value;
                            wolf.setCollarColor(set);
                        }
                    }
                    else if (entity instanceof Zombie) {
                        Zombie zombie = (Zombie) entity;
                        if (count == 0) {
                            boolean set = (Boolean) value;
                            zombie.setBaby(set);
                        }
                    }
                    else if (entity instanceof Horse) {
                        Horse horse = (Horse) entity;
                        if (count == 1 && value != null) {
                            org.bukkit.entity.Horse.Color set = (org.bukkit.entity.Horse.Color) value;
                            horse.setColor(set);
                        }
                        else if (count == 2) {
                            int set = (Integer) value;
                            horse.setDomestication(set);
                        }
                        else if (count == 3) {
                            double set = (Double) value;
                            horse.setJumpStrength(set);
                        }
                        else if (count == 4) {
                            int set = (Integer) value;
                            horse.setMaxDomestication(set);
                        }
                        else if (count == 5 && value != null) {
                            Style set = (Style) value;
                            horse.setStyle(set);
                        }
                        else if (count == 8) {
                            if (value != null) {
                                @SuppressWarnings("unchecked")
                                ItemStack set = ItemStack.deserialize((Map<String, Object>) value);
                                horse.getInventory().setSaddle(set);
                            }
                        }
                        else if (count == 9) {
                            org.bukkit.entity.Horse.Color set = (org.bukkit.entity.Horse.Color) value;
                            horse.setColor(set);
                        }
                        else if (count == 10) {
                            Style set = (Style) value;
                            horse.setStyle(set);
                        }
                        else if (count == 11) {
                            if (value != null) {
                                @SuppressWarnings("unchecked")
                                ItemStack set = ItemStack.deserialize((Map<String, Object>) value);
                                horse.getInventory().setArmor(set);
                            }
                        }
                    }
                    else if (entity instanceof MushroomCow) {
                        // MushroomCow.Variant doesn't exist in 1.8, skip
                    }
                    else {
                        BukkitAdapter.ADAPTER.setEntityMeta(entity, value, count);
                    }
                    count++;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, block.getLocation());
    }

}
