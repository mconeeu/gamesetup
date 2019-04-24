/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.api.locations;

import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@AllArgsConstructor
public class DatabaseLocation {

    private Gamemode gamemode;

    private long lastUpdate;

    /* 0 = Command / 1 = Sneak */
    private TreeMap<String, Integer> locations = new TreeMap<>(Comparator.naturalOrder());

    public DatabaseLocation() {
        lastUpdate = System.currentTimeMillis() / 1000;
    }

    public DatabaseLocation addLocationKeys(final Map<String, Integer> locations) {
        updateTimestamp();
        this.locations.putAll(locations);
        return this;
    }

    public DatabaseLocation addLocationKey(final String locationKey, int setType) {
        updateTimestamp();
        locations.put(locationKey, setType);
        return this;
    }

    private void updateTimestamp() {
        lastUpdate = System.currentTimeMillis() / 1000;
    }
}
