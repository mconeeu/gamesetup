/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.api.locations;

import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import org.bukkit.entity.Player;

import java.util.Map;

public interface LocationManager {

    Map<String, DatabaseLocation> getLocationKeys();

    void storeLocationKeysFromDatabase();

    void addObjectToDB(final Gamemode gamemode, final DatabaseLocation databaseLocation);

    void checkLocations(final String world, final Gamemode gamemode, final Player player);

    void checkLocations(final String world, final Gamemode gamemode);

    void checkLocations(final String world);

    void checkLocations(final Gamemode gamemode);
}
