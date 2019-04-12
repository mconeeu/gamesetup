/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.plugin.locations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import eu.mcone.coresystem.api.bukkit.CoreSystem;
import eu.mcone.coresystem.api.bukkit.world.CoreWorld;
import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import eu.mcone.game.setup.api.locations.DatabaseLocation;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.mongodb.client.model.Filters.eq;

@Log
@SuppressWarnings("Duplicates")
public class LocationManager implements eu.mcone.game.setup.api.locations.LocationManager {

    @Getter
    private Map<String, DatabaseLocation> locationKeys;

    @Getter
    private MongoCollection<DatabaseLocation> databaseLocationCollection;

    public LocationManager() {
        locationKeys = new HashMap<>();
        databaseLocationCollection = CoreSystem.getInstance().getMongoDB().getCollection("game_setup", DatabaseLocation.class);
    }

    public void storeLocationKeysFromDatabase() {
        for (DatabaseLocation databaseLocation : databaseLocationCollection.find(DatabaseLocation.class)) {
            if (databaseLocation != null) {
                System.out.println(databaseLocation.getGamemode() != null);
                locationKeys.put(databaseLocation.getGamemode().toString(), databaseLocation);
            } else {
                break;
            }
        }
    }

    public void addObjectToDB(final Gamemode gamemode, final DatabaseLocation databaseLocation) {
        databaseLocation.setGamemode(gamemode);
        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                databaseLocation,
                ReplaceOptions.createReplaceOptions
                        (
                                new UpdateOptions().upsert(true)
                        )
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by inserting the DatabaseLocation object in the database");
        }
    }

   /* public void test(Gamemode gamemode) {
        DatabaseLocation databaseLocation = collection.find(eq("gamemode", gamemode.toString())).first();

        DatabaseLocation databaseLocation = databaseLocationCollection.find(eq("gamemode", gamemode.toString())).first();

        Map<String, DatabaseLocation.ConfigurationAttributes> test = new HashMap<String, DatabaseLocation.ConfigurationAttributes>() {{
            put("Hallo10", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
            put("Hallo11", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
            put("Hallo12", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
        }};

        if (databaseLocation != null) {
            databaseLocation.getByCommand().forEach((k, v) -> System.out.println("Database: Key: " + k + " Value: " + v));
            test.forEach((k, v) -> System.out.println("Test: Key: " + k + " Value: " + v));
            test.forEach((k, v) -> databaseLocation.getByCommand().put(k, v));

            databaseLocation.getByCommand().forEach((k, v) -> System.out.println("Database: Key: " + k + " Value: " + v));
            databaseLocationCollection.replaceOne(eq("gamemode", gamemode.toString()), databaseLocation, ReplaceOptions.createReplaceOptions(new UpdateOptions().upsert(true)));
        } else {
            System.out.println("Object is null!");
        }
    }*/


    public void addLocationsToDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final Map<String, DatabaseLocation.ConfigurationAttributes> locationKeys) {
        locationKeys.forEach((k, v) -> this.locationKeys.get(gamemode.toString()).getMapByConfigurationType(configurationType).put(k, v));
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void addSingleLocatioKeyToDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final DatabaseLocation.ConfigurationAttributes configurationAttributes, final String locationKey) {
        this.locationKeys.get(gamemode.toString()).getMapByConfigurationType(configurationType).put(locationKey, configurationAttributes);
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void updateLocationListInDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final Map<String, DatabaseLocation.ConfigurationAttributes> locationKeys) {
        this.locationKeys.get(gamemode.toString()).getMapByConfigurationType(configurationType).clear();
        locationKeys.forEach((k, v) -> this.locationKeys.get(gamemode.toString()).getMapByConfigurationType(configurationType).put(k, v));
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void updateSingleLocatioKeyInDatabase(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType, final String oldLocationKey, final String newLocationKey, final DatabaseLocation.ConfigurationAttributes newConfigurationAttributes) {
        this.locationKeys.get(gamemode.toString()).getMapByConfigurationType(configurationType).remove(oldLocationKey);
        this.locationKeys.get(gamemode.toString()).getMapByConfigurationType(configurationType).put(newLocationKey, newConfigurationAttributes);
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void checkLocations(final DatabaseLocation.ConfigurationType configurationType) {
        for (CoreWorld coreWorld : CoreSystem.getInstance().getWorldManager().getWorlds()) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getMapByConfigurationType(configurationType).entrySet()) {
                    if (coreWorld.getLocation(entry_location.getKey()) == null) {
                        log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                    }
                }
            }
        }
    }

    public void checkLocations(final String world, final Gamemode gamemode) {
        CoreWorld coreWorld = CoreSystem.getInstance().getWorldManager().getWorld(world);

        if (coreWorld != null) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(gamemode.toString())) {
                    for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getByCommand().entrySet()) {
                        if (coreWorld.getLocation(entry_location.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }

                    log.info("By command entry finished");

                    for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getByClick().entrySet()) {
                        if (coreWorld.getLocation(entry_location.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }

                    log.info("By click entry finished");

                    for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getBySneak().entrySet()) {
                        if (coreWorld.getLocation(entry_location.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }

                    log.info("By sneak entry finished");
                }
            }
        } else {
            log.log(Level.SEVERE, "The core world with the name `" + world + "` dose not exists...");
        }
    }

    public void checkLocations(final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType) {
        for (CoreWorld coreWorld : CoreSystem.getInstance().getWorldManager().getWorlds()) {
            loopEntry(coreWorld, gamemode, configurationType);
        }
    }

    public void checkLocations(final String worldName, final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType) {
        CoreWorld coreWorld = CoreSystem.getInstance().getWorldManager().getWorld(worldName);

        if (coreWorld != null) {
            loopEntry(coreWorld, gamemode, configurationType);
        } else {
            log.log(Level.SEVERE, "The world with the name `" + worldName + "` could not found!");
        }
    }

    public void checkLocations(final String world) {
        CoreWorld coreWorld = CoreSystem.getInstance().getWorldManager().getWorld(world);

        if (coreWorld != null) {
            loopAllEntries(coreWorld);
        } else {
            log.log(Level.SEVERE, "The world with the name `" + world + "` could not found!");
        }
    }

    public void checkLocations(final Gamemode gamemode) {
        for (CoreWorld coreWorld : CoreSystem.getInstance().getWorldManager().getWorlds()) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(gamemode.toString())) {
                    for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getByCommand().entrySet()) {
                        if (coreWorld.getLocation(entry_location.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }

                    log.info("By command entry finished");

                    for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getByClick().entrySet()) {
                        if (coreWorld.getLocation(entry_location.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }

                    log.info("By click entry finished");

                    for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getBySneak().entrySet()) {
                        if (coreWorld.getLocation(entry_location.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }

                    log.info("By sneak entry finished");
                }
            }
        }
    }

    private boolean loopAllEntries(final CoreWorld coreWorld) {
        for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
            for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getByCommand().entrySet()) {
                if (coreWorld.getLocation(entry_location.getKey()) == null) {
                    log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                    return false;
                }
            }

            log.info("By command entry finished");

            for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getByClick().entrySet()) {
                if (coreWorld.getLocation(entry_location.getKey()) == null) {
                    log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                    return false;
                }
            }

            log.info("By click entry finished");

            for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getBySneak().entrySet()) {
                if (coreWorld.getLocation(entry_location.getKey()) == null) {
                    log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                    return false;
                }
            }

            log.info("By sneak entry finished");
        }

        return false;
    }

    private void loopEntry(final CoreWorld coreWorld, final Gamemode gamemode, final DatabaseLocation.ConfigurationType configurationType) {
        for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(gamemode.toString())) {
                for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> entry_location : entry.getValue().getMapByConfigurationType(configurationType).entrySet()) {
                    if (coreWorld.getLocation(entry_location.getKey()) == null) {
                        log.log(Level.WARNING, "The location `" + entry_location.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                    }
                }
            }
        }
    }
}
