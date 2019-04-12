/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.api.locations;

import eu.mcone.coresystem.api.core.gamemode.Gamemode;

import java.util.Map;

public interface LocationManager {

    Map<String, DatabaseLocation> getLocationKeys();

    void storeLocationKeysFromDatabase();

    void addObjectToDB(final Gamemode gamemode, final DatabaseLocation databaseLocation);

    void addLocationsToDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final Map<String, DatabaseLocation.ConfigurationAttributes> locationKeys);

    void addSingleLocatioKeyToDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final DatabaseLocation.ConfigurationAttributes configurationAttributes, final String locationKey);

    void updateLocationListInDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final Map<String, DatabaseLocation.ConfigurationAttributes> locationKeys);

    void updateSingleLocatioKeyInDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final String oldLocationKey, final String newLocationKey, final DatabaseLocation.ConfigurationAttributes newConfigurationAttributes);

    void checkLocations(final DatabaseLocation.ConfigurationType configurationType);

    void checkLocations(final String world, final Gamemode gamemode);

    void checkLocations(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType);

    void checkLocations(final String worldName, final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType);

    void checkLocations(final String world);

    void checkLocations(final Gamemode gamemode);

}
