/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.api.locations;

import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DatabaseLocation {

    public enum ConfigurationAttributes {

        RIGHT_CLICK(1, "byClick"),
        LEFT_CLICK(2, "byClick"),
        EMPTY(3, "EMPTY");

        @Getter
        private int ID;
        @Getter
        private String configurationType;

        ConfigurationAttributes(final int ID, final String configurationType) {
            this.ID = ID;
            this.configurationType = configurationType;
        }
    }

    public enum ConfigurationType {
        byCommand(1, "Command"),
        byClick(2, "Click"),
        bySneak(3, "Sneak");

        @Getter
        private final int ID;

        @Getter
        private final String type;

        ConfigurationType(final int ID, final String type) {
            this.ID = ID;
            this.type = type;
        }
    }


    /**
     * DatabaseLocation.class
     */
    private Gamemode gamemode;

    private long lastUpdate;

    private Map<String, ConfigurationAttributes> byCommand = new HashMap<>();
    private Map<String, ConfigurationAttributes> byClick = new HashMap<>();
    private Map<String, ConfigurationAttributes> bySneak = new HashMap<>();

    public DatabaseLocation() {
        lastUpdate = System.currentTimeMillis() / 1000;
    }

    public DatabaseLocation addLocationKeys(final ConfigurationType configurationType, final Map<String, ConfigurationAttributes> locations) {
        updateTimestamp();

        System.out.println(configurationType);

        switch (configurationType) {
            case byCommand:
                setByCommand(locations);
                break;
            case byClick:
               setByClick(locations);
                break;
            case bySneak:
                setBySneak(locations);
                break;
        }

        return this;
    }

    public DatabaseLocation addLocationKey(final ConfigurationType configurationType, final ConfigurationAttributes configurationAttributes, final String locationKey) {
        updateTimestamp();

        switch (configurationType) {
            case byCommand:
                byCommand.put(locationKey, configurationAttributes);
                break;
            case byClick:
                byClick.put(locationKey, configurationAttributes);
                break;
            case bySneak:
                bySneak.put(locationKey, configurationAttributes);
                break;
        }

        return this;
    }

    public Map<String, ConfigurationAttributes> getMapByConfigurationType(final ConfigurationType configurationType) {
        switch (configurationType) {
            case byCommand:
                return byCommand;
            case byClick:
                return byClick;
            case bySneak:
                return bySneak;
        }

        return null;
    }

    private void updateTimestamp() {
        lastUpdate = System.currentTimeMillis() / 1000;
    }
}
