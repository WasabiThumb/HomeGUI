package com.technovision.homegui.home;

import com.technovision.homegui.Homegui;
import com.technovision.homegui.util.TryBuilder;
import org.bukkit.entity.Player;

public interface HomeAPI {

    TryBuilder<HomeAPI> BUILDER = TryBuilder.of(
            HomeAPI.class,
            "com.technovision.homegui.home.impl.essentials.EssentialsHomeAPI",
            "com.technovision.homegui.home.impl.command.CommandHomeAPI"
    ).logger(Homegui.PLUGIN.getLogger());

    static HomeAPI get() {
        return BUILDER.get();
    }

    void goToHome(Player player, String home);

    void deleteHome(Player player, String home);

}
