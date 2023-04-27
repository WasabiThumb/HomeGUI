package com.technovision.homegui.home.impl.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsCommand;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.technovision.homegui.Homegui;
import com.technovision.homegui.home.HomeAPI;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

public class EssentialsHomeAPI implements HomeAPI {

    private final Essentials plugin;

    public EssentialsHomeAPI() {
        PluginManager pm = Bukkit.getPluginManager();
        Plugin p = pm.getPlugin("Essentials");
        if (p == null) p = pm.getPlugin("EssentialsX");

        if (p == null || (!p.isEnabled()))
            throw new IllegalStateException("Essentials plugin is not present or is disabled");
        if (!(p instanceof Essentials))
            throw new IllegalStateException("Essentials plugin is unsupported class " + p.getClass().getName());

        this.plugin = (Essentials) p;
    }

    @Override
    public void goToHome(Player player, String home) {
        User user = this.plugin.getUser(player);
        if (home == null) home = user.getHomes().stream().findFirst().orElse(null);

        this.runCommand("home", user, home != null ? new String[]{ home } : new String[0]);
    }

    @Override
    public void deleteHome(Player player, String home) {
        User user = this.plugin.getUser(player);

        this.runCommand("delhome", user, new String[]{ home });
    }

    private void runCommand(String cmd, User user, String[] args) {
        IEssentialsCommand ob = null;

        try {
            Method m = Essentials.class.getDeclaredMethod("loadCommand", String.class, String.class, IEssentialsModule.class, ClassLoader.class);
            m.setAccessible(true);
            ob = (IEssentialsCommand) m.invoke(
                    this.plugin,
                    "com.earth2me.essentials.commands.Command",
                    cmd,
                    null,
                    Essentials.class.getClassLoader()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ob == null) {
            Homegui.PLUGIN.getLogger().warning("Cannot find essentials command \"" + cmd + "\"");
            return;
        }

        try {
            if (ob instanceof EssentialsCommand) {
                EssentialsCommand qual = (EssentialsCommand) ob;

                Class<? extends EssentialsCommand> clazz = qual.getClass();
                Method m = clazz.getMethod(
                        "run",
                        Server.class,
                        CommandSource.class,
                        String.class,
                        String[].class
                );
                m.setAccessible(true);

                m.invoke(qual, user.getServer(), user.getSource(), cmd, args);
            } else {
                PluginCommand pc = this.plugin.getPluginCommand(cmd);
                if (pc == null) {
                    Homegui.PLUGIN.getLogger().warning("Cannot find essentials plugin command \"" + cmd + "\"");
                    return;
                }

                ob.run(user.getServer(), user, cmd, pc, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Homegui.PLUGIN.getLogger().warning("Exception while running essentials command \"" + cmd + "\", see details above");
        }
    }

}
