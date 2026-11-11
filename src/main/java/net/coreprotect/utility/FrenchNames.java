package net.coreprotect.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public final class FrenchNames {

    private static final Properties NAMES = load();

    private FrenchNames() {
        throw new IllegalStateException("Utility class");
    }

    private static Properties load() {
        Properties names = new Properties();
        try (InputStream in = FrenchNames.class.getResourceAsStream("/hadaria/names_fr.properties")) {
            if (in != null) {
                names.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return names;
    }

    @SuppressWarnings("deprecation")
    public static String block(Material material, int data) {
        if (material == Material.MOB_SPAWNER) {
            return data > 0 ? "Spawner (" + entity(EntityUtils.getSpawnerType(data)) + ")" : "Spawner";
        }
        if (material == Material.MONSTER_EGG) {
            EntityType type = EntityType.fromId(data);
            return type != null ? "Oeuf de " + entity(type) : "Oeuf d'apparition";
        }

        String name = NAMES.getProperty(material.name() + ":" + data);
        if (name == null) {
            name = NAMES.getProperty(material.name() + ":0");
        }
        return name != null ? name : readable(material.name());
    }

    public static String entity(EntityType type) {
        String name = NAMES.getProperty("entity." + type.name());
        return name != null ? name : readable(type.name());
    }

    private static String readable(String id) {
        String name = id.toLowerCase(Locale.ROOT).replace('_', ' ');
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
