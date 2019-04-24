/*
 * Copyright (c) 2017 - 2019 Dominik L., Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.game.setup.plugin.cmd;

import com.google.common.base.Joiner;
import eu.mcone.coresystem.api.bukkit.CoreSystem;
import eu.mcone.coresystem.api.bukkit.command.CorePlayerCommand;
import eu.mcone.coresystem.api.bukkit.player.CorePlayer;
import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import eu.mcone.game.setup.api.locations.DatabaseLocation;
import eu.mcone.game.setup.plugin.GameSetup;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.text.SimpleDateFormat;
import java.util.*;

public class SetupCMD extends CorePlayerCommand {

    public SetupCMD() {
        super("setup", "game.setup", "gs", "game", "gsetup");
    }

    private Map<Player, String> setup = new HashMap<>();
    private TreeMap<String, Integer> inProgress = new TreeMap<>(Comparator.naturalOrder());
    private TreeMap<String, Integer> finishedLocations = new TreeMap<>(Comparator.naturalOrder());
    private Map<Player, Map<String, Integer>> setLog = new HashMap<>();

    // 0 == Command 1 == Click 2 == Sneak

    @Override
    public boolean onPlayerCommand(Player player, String[] args) {
        CorePlayer corePlayer = CoreSystem.getInstance().getCorePlayer(player.getUniqueId());

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sendHelpTopic(player);
            } else if (args[0].equalsIgnoreCase("version")) {
                GameSetup.getInstance().getMessager().sendSimple(player, "§8§m--------------- §3MCONE§8-§7GameSetup §8§m---------------");
                GameSetup.getInstance().getMessager().sendSimple(player, "§7Entwickelt von: §f" + GameSetup.getInstance().getDescription().getAuthors());
                GameSetup.getInstance().getMessager().sendSimple(player, "§7Aktuelle Version: §f" + GameSetup.getInstance().getDescription().getVersion());
                GameSetup.getInstance().getMessager().sendSimple(player, "\n");
                GameSetup.getInstance().getMessager().sendSimple(player, "§7Dieses Plugin ist zur Einrichtung aller MCONE Minigame gedacht, " +
                        "\nsoltest du in diesem Plugin einen Bug entdecken melde diesen bitte einem MCONE Teamitglied!");
                GameSetup.getInstance().getMessager().sendSimple(player, "§8§m--------------- §3MCONE§8-§7GameSetup §8§m---------------");
            } else if (args[0].equalsIgnoreCase("setups")) {
                GameSetup.getInstance().getMessager().sendSimple(player, "§8§m--------------- §3MCONE§8-§7GameSetup §8§m---------------");
                GameSetup.getInstance().getLocationManager().getLocationKeys().forEach((key, value) -> {
                    Date date = new Date(value.getLastUpdate() * 1000);
                    GameSetup.getInstance().getMessager().sendSimple(player, "§8» §f§l" + key + " §8│ §7Letztes Update: §7§o" + new SimpleDateFormat("dd-MM-yyy").format(date));
                });
            } else if (args[0].equalsIgnoreCase("add")) {
                if (setup.containsKey(player)) {
                    String currentKey = setup.get(player);
                    if (inProgress.get(currentKey) == 0) {
                        CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).setLocation(currentKey, player.getLocation()).save();
                        GameSetup.getInstance().getMessager().send(player, "§7Du hast erfolgreich die Location mit dem Key §a" + currentKey + "§7 gesetzt.");

                        if (inProgress.lastKey().equalsIgnoreCase(currentKey)) {
                            finishedLocations.put(currentKey, inProgress.get(currentKey));
                            //Beendet das laufende Setup und gibt die gesetzten Locations zurück!
                            finish(player);
                        } else {
                            //Löst ein neues Event aus
                            fireEvent(player, getNextKeyInMap(player));
                        }
                    }
                } else {
                    GameSetup.getInstance().getMessager().send(player, "§cDu must ihm Setupmodus sein um diesen Befehl benutzen zu können!");
                }
            } else if (args[0].equalsIgnoreCase("finish")) {
                if (setup.containsKey(player)) {
                    //Beendet das laufende Setup und gibt die gesetzten Locations zurück!
                    finish(player);
                } else {
                    GameSetup.getInstance().getMessager().send(player, "§cDu must ihm Setupmodus sein um diesen Befehl benutzen zu können!");
                }
            } else if (args[0].equalsIgnoreCase("logs")) {
                if (!setLog.isEmpty()) {
                    GameSetup.getInstance().getMessager().sendSimple(player, "§8§m--------------- §3MCONE§8-§7GameSetup §8§m---------------");
                    GameSetup.getInstance().getMessager().send(player, "§7Folgende Spieler haben bereits locations gesetzt...");
                    setLog.forEach((key, value) -> GameSetup.getInstance().getMessager().send(player, "§8➥ §f" + key.getName() + " §8│ §7Anzhal der gesetzte Locations " + value.size()));
                } else {
                    GameSetup.getInstance().getMessager().send(player, "§7Es sind keine Einträge verfügbar!");
                }
            } else {
                sendHelpTopic(player);
            }
        } else if (args.length == 2) {
            //Setup Commands
            if (args[0].equalsIgnoreCase("start")) {
                try {
                    Gamemode gamemode = Gamemode.valueOf(args[1]);
                    if (!setup.containsKey(player)) {
                        //Getet alle Location Keys aus der Datenbank und speichert sie in einer Map (inProgress<TreeMap>)
                        storeLocationKeys(player, gamemode);
                        //Löst ein neues Event aus
                        fireEvent(player, "first");
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cDu bist bearbeitest bereits ein Setup! §8(§7Aktueller Key: §f" + setup.get(player) + "§8)");
                    }
                } catch (IllegalArgumentException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gibt einen der folgenden Spielmodi an! §8{§7" + Joiner.on("§8, §7").join(Gamemode.values()) + "§8}");
                }
            } else if (args[0].equalsIgnoreCase("skip")) {
                try {
                    int skips = Integer.valueOf(args[1]);

                    if (setup.containsKey(player)) {
                        //Löst ein neues Event aus
                        fireEvent(player, getNextKeyInMap(player, skips));
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cDu must ihm Setupmodus sein um diesen Befehl benutzen zu können!");
                    }
                } catch (NumberFormatException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gebe eine gültige Zahl an!");
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                try {
                    Gamemode gamemode = Gamemode.valueOf(args[1]);

                    if (GameSetup.getInstance().getLocationManager().getLocationKeys().containsKey(gamemode.toString())) {
                        GameSetup.getInstance().getMessager().send(player, "§7Liste aller location Keys des Spielmodus: §f" + gamemode.toString());
                        GameSetup.getInstance().getLocationManager().getLocationKeys().get(gamemode.toString()).getLocations().forEach((key, value) -> sendHover(player, key));
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cEs existieren keine Locations Keys für den Spielmodus §f" + gamemode.toString());
                    }
                } catch (IllegalArgumentException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gibt einen der folgenden Spielmodi an! §8{§7" + Joiner.on("§8, §7").join(Gamemode.values()) + "§8}");
                }
            } else if (args[0].equalsIgnoreCase("check")) {
                try {
                    Gamemode gamemode = Gamemode.valueOf(args[1]);
                    if (GameSetup.getInstance().getLocationManager().getLocationKeys().containsKey(gamemode.toString())) {
                        GameSetup.getInstance().getMessager().send(player, "§7Es wurde folgende Locations für den Spielmodus §f" + gamemode.toString() + " §7gesetzt...");
                        GameSetup.getInstance().getLocationManager().getLocationKeys().get(gamemode.toString()).getLocations().forEach((key, value) -> sendHover(player, key));
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cEs existieren keine Locations Keys für den Spielmodus §f" + gamemode.toString());
                    }
                } catch (IllegalArgumentException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gibt einen der folgenden Spielmodi an! §8{§7" + Joiner.on("§8, §7").join(Gamemode.values()) + "§8}");
                }
            } else if (args[0].equalsIgnoreCase("logs")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    if (setLog.containsKey(target)) {
                        if (!setLog.get(target).isEmpty()) {
                            GameSetup.getInstance().getMessager().send(player, "§7Der Spieler §f" + args[1] + " §7 hat folgende Locations gesetzt...");
                            setLog.get(target).forEach((key, value) -> GameSetup.getInstance().getMessager().send(player, "§8➥ §f" + key + " §8│ §7type ID " + value));
                        } else {
                            GameSetup.getInstance().getMessager().send(player, "§cDas Log von dem Spieler §f" + args[1] + " §cist leer!");
                        }
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cDer Spieler §f" + args[1] + " §cist im Log nicht aufgeführt!");
                    }
                } else {
                    GameSetup.getInstance().getMessager().send(player, "§cDer Spieler mit dem Namen §f" + args[1] + " §cexistiert nicht!");
                }
            } else {
                sendHelpTopic(player);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                try {
                    Gamemode gamemode = Gamemode.valueOf(args[1]);
                    String key = args[2];
                    DatabaseLocation databaseLocation = GameSetup.getInstance().getLocationManager().getLocationKeys().get(gamemode.toString());

                    if (databaseLocation != null) {
                        if (databaseLocation.getLocations().containsKey(key)) {
                            int typeID = databaseLocation.getLocations().get(key);
                            setup.put(player, key);

                            switch (typeID) {
                                    // 0 = Command
                                case 0:
                                    if (setup.containsKey(player)) {
                                        CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).setLocation(key, player.getLocation()).save();
                                        GameSetup.getInstance().getMessager().send(player, "§7Du hast die Location mit dem Key §a`" + key + "` §7erfolgreich gesetzt!");
                                    }

                                    // 1 = Click
                                case 1:
                                    GameSetup.getInstance().getMessager().send(corePlayer.bukkit(), "§7Du musst auf einen Block §fklicken §7um die Location zu setzen!");

                                    CoreSystem.getInstance().registerEvents(new Listener() {
                                        @EventHandler
                                        public void on(PlayerInteractEvent e) {
                                            if (setup.containsKey(player)) {
                                                CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).setLocation(key, e.getClickedBlock().getLocation()).save();
                                                GameSetup.getInstance().getMessager().send(player, "§7Du hast die Location mit dem Key §a`" + key + "` §7erfolgreich gesetzt!");

                                                //Siehe Begründung (Zeile 423 und 445)
                                                e.getHandlers().unregister(this);
                                            } else {
                                                e.setCancelled(true);
                                            }
                                        }
                                    });

                                    //2 = Sneak
                                case 2:
                                    GameSetup.getInstance().getMessager().send(corePlayer.bukkit(), "§aDu musst §fsneaken §7um die Location zu setzen!");

                                    CoreSystem.getInstance().registerEvents(new Listener() {
                                        @EventHandler
                                        public void on(PlayerToggleSneakEvent e) {
                                            if (!player.isFlying()) {
                                                if (setup.containsKey(player)) {
                                                    if (e.isSneaking()) {
                                                        CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).setLocation(key, player.getLocation()).save();
                                                        GameSetup.getInstance().getMessager().send(player, "§7Du hast die Location mit dem Key §a`" + key + "` §7erfolgreich gesetzt!");
                                                    }

                                                    //Siehe Begründung (Zeile 423 und 445)
                                                    e.getHandlers().unregister(this);
                                                } else {
                                                    e.setCancelled(true);
                                                }
                                            }
                                        }
                                    });
                                    break;
                            }
                        } else {
                            GameSetup.getInstance().getMessager().send(player, "§cDer Location Key §f" + key + " §cexistiert in der Datenbank nicht!");
                        }
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cEs existiert kein Setup für den Spielmodus §f" + gamemode.toString() + "§c!");
                    }
                } catch (IllegalArgumentException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gibt einen der folgenden Spielmodi an! §8{§7" + Joiner.on("§8, §7").join(Gamemode.values()) + "§8}");
                }
            }
        } else {
            sendHelpTopic(player);
        }

        return false;
    }

    private boolean storeLocationKeys(final Player player, final Gamemode gamemode) {
        DatabaseLocation databaseLocation = GameSetup.getInstance().getLocationManager().getLocationKeys().get(gamemode.toString());
        if (databaseLocation != null) {
            inProgress.putAll(databaseLocation.getLocations());
            GameSetup.getInstance().getMessager().send(player, "§7Es wurden §a" + databaseLocation.getLocations().size() + " §7Keys gefunden!");
            return true;
        } else {
            GameSetup.getInstance().getMessager().send(player, "§cEs ist ein Fehler beim getten der Keys aus der Datenbank aufgetreten!");
            return false;
        }
    }

    /**
     * Nimmt den aktuellen Key aus der Map und getten den nächsten aus der Map
     */
    private String getNextKeyInMap(final Player player) {
        Iterator<Map.Entry<String, Integer>> iterator = inProgress.entrySet().iterator();

        Map.Entry<String, Integer> returnEntry = null;
        String currentKey = setup.get(player);

        //Der aktuelle Key wurde gesetzt
        finishedLocations.put(currentKey, inProgress.get(currentKey));

        if (currentKey != null) {
            boolean oneMore = false;

            if (inProgress.size() > 1) {
                while (iterator.hasNext()) {
                    Map.Entry<String, Integer> entry = iterator.next();
                    if (entry.getKey().equalsIgnoreCase(currentKey)) {
                        if (!iterator.hasNext()) {
                            if (CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).getLocation(currentKey) != null) {
                                //Beendet das laufende Setup und gibt die gesetzten Locations zurück!
                                finish(player);
                            } else {
                                return currentKey;
                            }
                        } else {
                            oneMore = true;
                        }
                    } else if (oneMore) {
                        returnEntry = entry;
                        break;
                    }
                }
            } else {
                returnEntry = inProgress.lastEntry();
            }

            if (returnEntry != null) {
                //Überpruefe ob der Size von inProgress groeßer als 1 ist
                if (inProgress.size() > 1) {
                    //Entferen den alten Key von der Map
                    inProgress.remove(currentKey);
                }

                //Füge den neuen Key der InProgress map hinzu
                inProgress.put(returnEntry.getKey(), inProgress.get(returnEntry.getKey()));

                return returnEntry.getKey();
            } else {
                GameSetup.getInstance().getMessager().send(player, "§4Es ist ein Fehler bei getten des nächsten Keys aufgetreten!");
                return null;
            }
        } else {
            GameSetup.getInstance().getMessager().send(player, "§4Der aktuelle Key konnte nicht gefunden werden!");
            return null;
        }
    }

    private String getNextKeyInMap(final Player player, int jumps) {
        String currentKey = setup.get(player);
        List<String> keySkips = new ArrayList<>();

        System.out.println("CurrentKey: " + currentKey);

        //Der aktuelle Key wurde gesetzt
        finishedLocations.put(currentKey, inProgress.get(currentKey));

        if (currentKey != null) {
            Iterator<Map.Entry<String, Integer>> iterator = inProgress.entrySet().iterator();
            Map.Entry<String, Integer> staticEntry = null;
            boolean happen = false;

            if (inProgress.size() > 1) {
                while (iterator.hasNext()) {
                    Map.Entry<String, Integer> entry = iterator.next();
                    if (entry.getKey().equalsIgnoreCase(currentKey)) {
                        happen = true;
                    } else if (happen) {
                        if (jumps == 1) {
                            staticEntry = entry;
                            break;
                        } else {
                            keySkips.add(entry.getKey());
                            jumps--;
                        }
                    }
                }
            } else {
                staticEntry = inProgress.lastEntry();
            }

            if (staticEntry != null) {
                GameSetup.getInstance().getMessager().send(player, "§7Du hast folgende(n) Key(s) geskippt, ");
                keySkips.forEach((value) -> {
                    //Überpruefe ob der Size von inProgress groeßer als 1 ist
                    if (inProgress.size() > 1) {
                        //Entferen den/die uebersprungenen Key(s) von der Map
                        inProgress.remove(value);
                    }

                    GameSetup.getInstance().getMessager().sendSimple(player, "§8➥ §f" + value);
                });

                return staticEntry.getKey();
            }
        } else {
            GameSetup.getInstance().getMessager().send(player, "§cEs ist ein Fehler beim getten des aktuellen Keys aufgetreten!");
        }
        return null;
    }

    /**
     * Note: String key = getNextKeyInMap!
     * <p>
     * 0 == Command / 1 == Click / 2 == Sneak
     */
    private void fireEvent(final Player player, String key) {
        int value = 0;

        if (key == null) {
            GameSetup.getInstance().getMessager().send(player, "§cDer übergebene Key ist null!");
        } else if (key.equalsIgnoreCase("first")) {
            key = inProgress.firstEntry().getKey();
            value = inProgress.firstEntry().getValue();
        } else {
            value = inProgress.get(key);
        }

        if (key != null) {
            final String finalKey = key;

            setup.put(player, finalKey);

            if (value == 0) {
                //Command == 0
                GameSetup.getInstance().getMessager().send(player, "§7Setzte nun die Location mit dem Key §f" + finalKey + " §7(/setup add)");
            } else if (value == 1) {
                //Click == 1
                GameSetup.getInstance().getMessager().send(player, "§7Setzte nun die Location mit dem Key §f" + finalKey + " §7(Click)");

                CoreSystem.getInstance().registerEvents(new Listener() {
                    @EventHandler
                    public void on(PlayerInteractEvent e) {
                        if (setup.containsKey(player)) {
                            if (inProgress.get(setup.get(player)) == 1) {
                                CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).setLocation(finalKey, e.getClickedBlock().getLocation()).save();

                                setAndUnregister(player, finalKey);

                                //Der listener wird hier wieder unregister da es sonst zu Bugs kommen würde. (Falsche Reihenfolge in den Maps, Einträge werden ausgelassen, NullPointerException etc.)
                                e.getHandlers().unregister(this);
                            } else {
                                e.setCancelled(true);
                            }
                        }
                    }
                });
            } else if (value == 2) {
                //Sneak == 2
                GameSetup.getInstance().getMessager().send(player, "§7Setzte nun die Location mit dem Key §f" + finalKey + " (Sneak)");

                CoreSystem.getInstance().registerEvents(new Listener() {
                    @EventHandler
                    public void on(PlayerToggleSneakEvent e) {
                        if (!player.isFlying()) {
                            if (setup.containsKey(player)) {
                                if (inProgress.get(setup.get(player)) == 2) {
                                    if (e.isSneaking()) {
                                        CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).setLocation(finalKey, player.getLocation()).save();

                                        setAndUnregister(player, finalKey);

                                        //Der listener wird hier wieder unregister da es sonst zu Bugs kommen würde. (Falsche Reihenfolge in den Maps, Einträge werden ausgelassen, NullPointerException etc.)
                                        e.getHandlers().unregister(this);
                                    }
                                } else {
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                });
            } else {
                GameSetup.getInstance().getMessager().send(player, "§cEs ist ein Fehler in der Datenbankstruktur aufgetreten, bitte überprüfe alle Einträge auf ihre Richtigkeit!");
            }
        }
    }

    private void setAndUnregister(final Player player, final String key) {
        GameSetup.getInstance().getMessager().send(player, "§7Du hast die Location mit dem Key §a" + key + " §7erfolgreich gesetzt!");

        if (inProgress.lastKey().equalsIgnoreCase(key)) {
            finishedLocations.put(key, inProgress.get(key));
            //Beendet das laufende Setup und gibt die gesetzten Locations zurück!
            finish(player);
        } else {
            //Löst ein neues Event aus
            fireEvent(player, getNextKeyInMap(player));
        }
    }

    /**
     * Beendet das aktuelle Setup, und gibt alle gesetzten Keys zurück
     */
    private void finish(final Player player) {
        if (!finishedLocations.isEmpty()) {
            setLog.put(player, finishedLocations);

            GameSetup.getInstance().getMessager().send(player, "§7Du hast folgende locations gesetzt:");
            finishedLocations.forEach((key, value) -> sendHover(player, key));

            //Alle Maps überschreiben
            setup = new HashMap<>();
            inProgress = new TreeMap<>(Comparator.naturalOrder());
            finishedLocations = new TreeMap<>(Comparator.naturalOrder());
        } else {
            GameSetup.getInstance().getMessager().send(player, "§cDu hast keine Locations gesetzt!");
        }
    }

    private void sendHover(final Player player, final String key) {
        Location location = CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).getLocation(key);
        BaseComponent[] baseComponents;

        if (location != null) {
            baseComponents = new ComponentBuilder("§8➥ §f" + key + " §8│ §7Gesetzt: §aJA").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            "§8X: §7§o" + location.getX() +
                                    " §8Y: §7§o" + location.getY() +
                                    " §8Z: §7§o" + location.getZ() +
                                    " §8Yaw: §7§o" + location.getYaw() +
                                    " §8Pitch: §7§o" + location.getPitch()
                    ).create())).create();
        } else {
            baseComponents = new ComponentBuilder("§8➥ §f" + key + " §8│ §7Gesetzt: §cNEIN").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            "§4Location nicht verfügbar!"
                    ).create())).create();
        }

        player.spigot().sendMessage(baseComponents);
    }

    private void sendHelpTopic(final Player player) {
        GameSetup.getInstance().getMessager().sendSimple(player, "§8§m-------- §7GameSetup Befehle §8§m--------");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup help §8│ §fzeigt alle Befehle.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup version §8│ §fgibt die aktuelle Version des Plugins zurück.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup setups §8│ §fgibt alle verfügbaren Setups in der Datenbank zurück.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup list {gamemode} §8│ §fgibt eine Liste aller keys eines Spielmodus zurück.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup set {gamemode} {locationKey} §8│ §fsetzt eine Location für den angegebenen Key in der aktuellen Welt.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup check {gamemode} §8│ §fprüft ob die Locations des Spielmodus in der Aktuellen Welt gesetzt sind.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§8§m-------------------- §3Setup §8§m--------------------");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup start {gamemode} §8│ §fstartet das Setup für den angegebenen Spielmodus.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup add §8│ §ffügt einen loaction hinzu.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup skip {skips} §8│ §füberspringt die angegebene Anzahl (skips) an Locations.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup finish §8│ §fbeendet den Setupmodus und gibt alle gesetzten locations zurück.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup show log §8│ §fgibt für jeden Spieler der diese Plugin benutzt hat ein Liste aller gesetzten Locations zurück.");
        GameSetup.getInstance().getMessager().sendSimple(player, "§7/setup get log {spieler} §8│ §fgibt eine Liste der Locations zurück der der angegebenen Spieler gesetzt hat!");
    }
}
