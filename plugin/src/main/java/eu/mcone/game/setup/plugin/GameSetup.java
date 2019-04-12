/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.plugin;

import eu.mcone.coresystem.api.bukkit.CoreSystem;
import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import eu.mcone.game.setup.api.GameSetupApi;
import eu.mcone.game.setup.api.locations.DatabaseLocation;
import eu.mcone.game.setup.plugin.cmd.SetupCMD;
import eu.mcone.game.setup.plugin.locations.LocationManager;
import lombok.Getter;

import java.util.HashMap;

public class GameSetup extends GameSetupApi {

    @Getter
    private LocationManager locationManager;

    @Override
    public void onEnable() {
        setInstance(this);

        sendConsoleMessage("§aStarte GameSetup plugin...");

        sendConsoleMessage("§aErstelle locationManager und lade alle LocationKeys von der Datenbank herunter...");
        locationManager = new LocationManager();

        locationManager.addObjectToDB(Gamemode.MINEWAR, new DatabaseLocation()
                .addLocationKeys(DatabaseLocation.ConfigurationType.byClick, new HashMap<String, DatabaseLocation.ConfigurationAttributes>() {{
                    put("location_byCommand_6", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
                    put("location_byCommand_7", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
                    put("location_byCommand_8", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
                    put("location_byCommand_9", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
                    put("location_byCommand_10", DatabaseLocation.ConfigurationAttributes.RIGHT_CLICK);
                }}));
        
        locationManager.storeLocationKeysFromDatabase();

        sendConsoleMessage("§aLade Translations...");
        CoreSystem.getInstance().getTranslationManager().loadCategories(this);

        sendConsoleMessage("§aRegistriere all Commands...");
        CoreSystem.getInstance().registerCommands(
                new SetupCMD()
        );
    }

    @Override
    public void onDisable() {
        sendConsoleMessage("§cDeaktiviere GameSetup plugin ");
    }

    public DatabaseLocation createDatabaseLocationObject() {
        return new DatabaseLocation();
    }
}
