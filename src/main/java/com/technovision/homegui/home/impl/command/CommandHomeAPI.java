package com.technovision.homegui.home.impl.command;

import com.technovision.homegui.home.HomeAPI;
import org.bukkit.entity.Player;

public class CommandHomeAPI implements HomeAPI {

    @Override
    public void goToHome(Player player, String home) {
        if (home == null) {
            player.performCommand("essentials:home");
        } else {
            player.performCommand("essentials:home " + home);
        }
    }

    @Override
    public void deleteHome(Player player, String home) {
        player.performCommand("essentials:delhome " + home);
    }

}
