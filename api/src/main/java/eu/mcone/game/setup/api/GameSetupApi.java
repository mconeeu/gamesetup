/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.api;

import eu.mcone.coresystem.api.bukkit.CorePlugin;
import eu.mcone.game.setup.api.locations.DatabaseLocation;
import eu.mcone.game.setup.api.locations.LocationManager;
import lombok.Getter;
import org.bukkit.ChatColor;

public abstract class GameSetupApi extends CorePlugin {

    public GameSetupApi() {
        super("GameSetup", ChatColor.GRAY, "game.setup.prefix");
    }

    @Getter
    private static GameSetupApi instance;

    protected void setInstance(final GameSetupApi instance) {
        if (instance == null) {
            System.err.println("LobbyPlugin instance cannot be set twice!");
        } else {
            GameSetupApi.instance = instance;
        }
    }

    public abstract DatabaseLocation createDatabaseLocationObject();

    public abstract LocationManager getLocationManager();
}
