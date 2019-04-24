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
import eu.mcone.game.setup.plugin.GameSetup;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;

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

    public void addLocationsToDatabase(final Gamemode gamemode, final Map<String, Integer> locationKeys) {
        locationKeys.forEach((k, v) -> this.locationKeys.get(gamemode.toString()).getLocations().put(k, v));
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void addSingleLocatioKeyToDatabase(final Gamemode gamemode, final String locationKey, final int setType) {
        this.locationKeys.get(gamemode.toString()).getLocations().put(locationKey, setType);
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void updateLocationListInDatabase(final Gamemode gamemode, final Map<String, Integer> locationKeys) {
        this.locationKeys.get(gamemode.toString()).getLocations().clear();
        locationKeys.forEach((k, v) -> this.locationKeys.get(gamemode.toString()).getLocations().put(k, v));
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void updateSingleLocatioKeyInDatabase(final Gamemode gamemode, final String oldLocationKey, final String newLocationKey, final int newSetType) {
        this.locationKeys.get(gamemode.toString()).getLocations().remove(oldLocationKey);
        this.locationKeys.get(gamemode.toString()).getLocations().put(newLocationKey, newSetType);
        this.locationKeys.get(gamemode.toString()).setLastUpdate(System.currentTimeMillis() / 1000);

        if (!(databaseLocationCollection.replaceOne(
                eq("gamemode", gamemode.toString()),
                this.locationKeys.get(gamemode.toString())
        ).wasAcknowledged())) {
            log.log(Level.WARNING, "Error by replacing the object!");
        }
    }

    public void checkLocations(final int setType) {
        for (CoreWorld coreWorld : CoreSystem.getInstance().getWorldManager().getWorlds()) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                for (Map.Entry<String, Integer> location_entry : entry.getValue().getLocations().entrySet()) {
                    if (location_entry.getValue() == setType) {
                        if (coreWorld.getLocation(location_entry.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + location_entry.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
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
                    for (Map.Entry<String, Integer> locationEntry : entry.getValue().getLocations().entrySet()) {
                        if (coreWorld.getLocation(locationEntry.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + locationEntry.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }
                }
            }
        } else {
            log.log(Level.SEVERE, "The core world with the name `" + world + "` dose not exists...");
        }
    }

    public void checkLocations(final String world) {
        CoreWorld coreWorld = CoreSystem.getInstance().getWorldManager().getWorld(world);

        if (coreWorld != null) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                for (Map.Entry<String, Integer> locationEntry : entry.getValue().getLocations().entrySet()) {
                    if (coreWorld.getLocation(locationEntry.getKey()) == null) {
                        log.log(Level.WARNING, "The location `" + locationEntry.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                    }
                }
            }
        } else {
            log.log(Level.SEVERE, "The world with the name `" + world + "` could not found!");
        }
    }

    public void checkLocations(final Gamemode gamemode) {
        for (CoreWorld coreWorld : CoreSystem.getInstance().getWorldManager().getWorlds()) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(gamemode.toString())) {
                    for (Map.Entry<String, Integer> locationEntry : entry.getValue().getLocations().entrySet()) {
                        if (coreWorld.getLocation(locationEntry.getKey()) == null) {
                            log.log(Level.WARNING, "The location `" + locationEntry.getKey() + "` in the world `" + coreWorld.getName() + "` could not found!, ConfigurationType: Command");
                        }
                    }
                }
            }
        }
    }

    public void checkLocations(final String world, final Gamemode gamemode, final Player player) {
        CoreWorld coreWorld = CoreSystem.getInstance().getWorldManager().getWorld(world);

        if (coreWorld != null) {
            for (Map.Entry<String, DatabaseLocation> entry : locationKeys.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(gamemode.toString())) {
                    for (Map.Entry<String, Integer> locationEntry : entry.getValue().getLocations().entrySet()) {
                        if (coreWorld.getLocation(locationEntry.getKey()) == null) {
                            player.sendMessage("§8➥ §f" + locationEntry.getKey() + " §8│ §7Gesetzt: §cNEIN");
                        }
                    }
                }
            }
        } else {
            GameSetup.getInstance().getMessager().send(player, "§cDie CoreWorld mit dem Namen `" + world + "` konnte nicht gefunden werden!");
        }
    }

}
