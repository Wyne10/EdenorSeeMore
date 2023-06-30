package com.froobworld.seemore.config;

import com.froobworld.nabconfiguration.*;
import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.seemore.SeeMore;
import me.wyne.wutils.config.ConfigField;
import me.wyne.wutils.config.ConfigParameter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SeeMoreConfig extends NabConfiguration {
    private static final int VERSION = 1;

    public SeeMoreConfig(SeeMore seeMore) {
        super(
                new File(seeMore.getDataFolder(), "config.yml"),
                () -> seeMore.getResource("config.yml"),
                i -> seeMore.getResource("config-patches/" + i + ".patch"),
                VERSION
        );
    }

    @Entry(key = "update-delay")
    public final ConfigEntry<Integer> updateDelay = ConfigEntries.integerEntry();

    @SectionMap(key = "world-settings", defaultKey = "default")
    public final ConfigSectionMap<World, WorldSettings> worldSettings = new ConfigSectionMap<>(World::getName, WorldSettings.class, true);

    @ConfigField(path = "permission-settings")
    public PermissionSettings permissionSettings = new PermissionSettings();

    public static class WorldSettings extends ConfigSection {

        @Entry(key = "maximum-view-distance")
        public final ConfigEntry<Integer> maximumViewDistance = ConfigEntries.integerEntry();

    }

    public static class PermissionSettings implements ConfigParameter {

        private final int defaultMaxViewDistance = Bukkit.getViewDistance();
        private final Map<String, Integer> maximumViewDistance = new HashMap<>();

        @Override
        public Object getValue(@NotNull FileConfiguration config, @NotNull String path) {
            maximumViewDistance.clear();
            for (String key : config.getConfigurationSection(path).getKeys(false))
            {
                maximumViewDistance.put(config.getString(path + "." + key + ".permission"), config.getInt(path + "." + key + ".maximum-view-distance"));
            }
            return null;
        }

        @Nullable
        private String getBestPermission(@NotNull final Player player)
        {
            int highestViewDistance = defaultMaxViewDistance;
            String bestPermission = null;
            for (String permission : maximumViewDistance.keySet())
            {
                if (player.hasPermission(permission))
                {
                    if (getMaximumViewDistance(permission) > highestViewDistance)
                    {
                        highestViewDistance = getMaximumViewDistance(permission);
                        bestPermission = permission;
                    }
                }
            }
            return bestPermission;
        }

        private int getMaximumViewDistance(@Nullable final String permission)
        {
            return permission == null ? defaultMaxViewDistance : maximumViewDistance.get(permission);
        }

        public int getMaximumViewDistance(@NotNull final Player player)
        {
            return getMaximumViewDistance(getBestPermission(player));
        }

    }

}
