package com.personoid;

import com.personoid.api.npc.NPCRegistry;
import com.personoid.api.utils.bukkit.Logger;
import com.personoid.api.utils.bukkit.Task;
import com.personoid.commands.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PersonoidPlugin extends JavaPlugin {

    public static final boolean DEVELOPMENT = "true".equals(System.getenv("DEVELOPMENT")) || System.getProperty("os.name").contains("Win");
    private final Logger LOGGER = Logger.get("Personoid");
    private final NPCRegistry baseRegistry = new NPCRegistry();
    private final Map<String, NPCRegistry> registries = new HashMap<>();
    private static PersonoidPlugin plugin;

    @Override
    public void onEnable() {
        LOGGER.info("Successfully loaded Personoid plugin.");
        getCommand("personoid").setExecutor(new CommandManager());
        CommandManager.registerCommands();
        Config.reload();
        initReloader();
    }

    @Override
    public void onDisable() {
        LOGGER.info("Successfully unloaded Personoid plugin.");
    }

    public void addProvidingPlugin(String name) {
        registries.put(name, new NPCRegistry());
    }

    public void initReloader() {
        File file = new File("plugins/Personoid-1.0.0.jar");
        long lastModified = file.lastModified();
        new Task(() -> {
            if (file.lastModified() != lastModified && Config.isAutoReload()) {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[Personoid] " + ChatColor.GREEN + "Plugin modified, reloading...");
                Bukkit.reload();
            }
        }, this).repeat(0, 20);
    }

    public static JavaPlugin getPlugin() {
        if (plugin == null) plugin = getPlugin(PersonoidPlugin.class);
        return plugin;
    }

    public NPCRegistry getBaseRegistry() {
        return baseRegistry;
    }

    public NPCRegistry getRegistry(String name) {
        return registries.get(name);
    }
}
