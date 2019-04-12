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
import eu.mcone.coresystem.api.bukkit.world.CoreLocation;
import eu.mcone.coresystem.api.core.gamemode.Gamemode;
import eu.mcone.game.setup.api.locations.DatabaseLocation;
import eu.mcone.game.setup.plugin.GameSetup;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SetupCMD extends CorePlayerCommand {

    public SetupCMD() {
        super("setup", "game.setup", "gs", "game", "gsetup");
    }

    private List<Player> setup = new ArrayList<>();

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
            } else if (args[0].equalsIgnoreCase("keys")) {
                GameSetup.getInstance().getMessager().sendSimple(player, "§8§m--------------- §3MCONE§8-§7GameSetup §8§m---------------");
                for (Map.Entry<String, DatabaseLocation> entry : GameSetup.getInstance().getLocationManager().getLocationKeys().entrySet()) {
                    Date date = new Date(entry.getValue().getLastUpdate() * 1000);
                    GameSetup.getInstance().getMessager().sendSimple(player, "§8» §f§l" + entry.getKey() + " §8│ §7Letztes Update: §7§o" + new SimpleDateFormat("dd-MM-yyy").format(date));
                }
            } else {
                GameSetup.getInstance().getMessager().send(player, "§cBitte benutze §f/setup help.");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                try {
                    Gamemode gamemode = Enum.valueOf(Gamemode.class, args[1]);
                    for (Map.Entry<String, DatabaseLocation> entry : GameSetup.getInstance().getLocationManager().getLocationKeys().entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(gamemode.toString())) {
                            //ByCommand MAP
                            GameSetup.getInstance().getMessager().send(player, "§8» §7Keys Type command:");
                            for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> keysEntry : entry.getValue().getByCommand().entrySet()) {
                                sendHover(player, keysEntry.getKey());
                            }

                            //ByClick MAP
                            GameSetup.getInstance().getMessager().send(player, "§8» §7Keys Type click:");
                            for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> keysEntry : entry.getValue().getByClick().entrySet()) {
                                sendHover(player, keysEntry.getKey());
                            }

                            //BySneak MAP
                            GameSetup.getInstance().getMessager().send(player, "§8» §7Keys Type sneak:");
                            for (Map.Entry<String, DatabaseLocation.ConfigurationAttributes> keysEntry : entry.getValue().getBySneak().entrySet()) {
                                sendHover(player, keysEntry.getKey());
                            }
                            break;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gibt einen gültigen spielmodus an §7{§f" + Joiner.on("").join(Gamemode.values()) + "§7}");
                }
            } else if (args[0].equalsIgnoreCase("check")) {
                String locationKey = args[1];
                CoreLocation coreLocation = corePlayer.getWorld().getLocation(locationKey);
                if (coreLocation != null) {
                    player.spigot().sendMessage(
                            new ComponentBuilder("§aDie Location mit dem Key `" + locationKey + "` existiert,")
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new ComponentBuilder(
                                                    "§8X: §7§o" + coreLocation.getX() +
                                                            " §8Y: §7§o" + coreLocation.getY() +
                                                            " §8Z: §7§o" + coreLocation.getZ() +
                                                            " §8Yaw: §7§o" + coreLocation.getYaw() +
                                                            " §8Pitch: §7§o" + coreLocation.getPitch()
                                            ).create())).create());
                } else {
                    GameSetup.getInstance().getMessager().send(player, "§cDie Location mit dem Key `§7" + locationKey + "§c` konnte nicht gefunden werden!");
                }
            } else {
                GameSetup.getInstance().getMessager().send(player, "§cBitte benutze §f/setup check §7{§clocationKey§7}.");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                try {
                    Gamemode gamemode = Enum.valueOf(Gamemode.class, args[1]);
                    String key = args[2];

                    DatabaseLocation databaseLocation = GameSetup.getInstance().getLocationManager().getLocationKeys().get(gamemode.toString());
                    if (databaseLocation != null) {
                        if (databaseLocation.getByCommand().containsKey(key)) {
                            corePlayer.getWorld().setLocation(key, player.getLocation()).save();
                            GameSetup.getInstance().getMessager().send(player, "§aDu hast die Location mit dem Key `§f" + key + "§a` erfolgreich gesetzt!");
                        } else if (databaseLocation.getByClick().containsKey(key)) {
                            GameSetup.getInstance().getMessager().send(player, "§aDu musst auf einen block klicken um die location zu setzen!");
                            setup.add(player);

                            CoreSystem.getInstance().registerEvents(new Listener() {
                                @EventHandler
                                public void on(PlayerInteractEvent e) {
                                    if (setup.contains(player)) {
                                        DatabaseLocation.ConfigurationAttributes configurationAttributes = databaseLocation.getByClick().get(key);

                                        switch (configurationAttributes) {
                                            case RIGHT_CLICK:
                                                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                                                    corePlayer.getWorld().setLocation(key, e.getClickedBlock().getLocation());
                                                    GameSetup.getInstance().getMessager().send(player, "§aDu hast die Location mit dem Key `§f" + key + "§a` erfolgreich gesetzt!");
                                                    setup.remove(player);
                                                }
                                                break;
                                            case LEFT_CLICK:
                                                if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                                                    corePlayer.getWorld().setLocation(key, e.getClickedBlock().getLocation());
                                                    GameSetup.getInstance().getMessager().send(player, "§aDu hast die Location mit dem Key `§f" + key + "§a` erfolgreich gesetzt!");
                                                    setup.remove(player);
                                                }
                                                break;
                                        }
                                    }
                                }
                            });
                        } else if (databaseLocation.getBySneak().containsKey(key)) {
                            GameSetup.getInstance().getMessager().send(player, "§aDu musst sneaken um die location zu setzen!");
                            setup.add(player);

                            CoreSystem.getInstance().registerEvents(new Listener() {
                                @EventHandler
                                public void on(PlayerToggleSneakEvent e) {
                                    if (setup.contains(player)) {
                                        if (e.isSneaking()) {
                                            corePlayer.getWorld().setLocation(key, player.getLocation());
                                            GameSetup.getInstance().getMessager().send(player, "§aDu hast die Location mit dem Key `§f" + key + "§a` erfolgreich gesetzt!");
                                        }
                                    }
                                }
                            });
                        } else {
                            GameSetup.getInstance().getMessager().send(player, "§cEs existiert kein Key mit dem Namen `§f" + key + "§c` in der Datenbank!");
                        }
                    } else {
                        GameSetup.getInstance().getMessager().send(player, "§cEs konnten keine Keys unter dem Spielmodus `§f" + gamemode.toString() + "§c` gefunden werden!");
                    }
                } catch (IllegalArgumentException e) {
                    GameSetup.getInstance().getMessager().send(player, "§cBitte gibt einen gültigen spielmodus an §8{§7" + Joiner.on("§8, §7").join(Gamemode.values()) + "§8}");
                }
            } else {
                GameSetup.getInstance().getMessager().send(player, "§cBitte benutze §f/setup set §7{§cgamemode§7} §7{§clocationKey§7}§f.");
            }
        } else {
            sendHelpTopic(player);
        }

        return false;
    }

    private void sendHelpTopic(Player player) {
        GameSetup.getInstance().getMessager().sendSimple(player, "§8§m--------------- §3MCONE§8-§7GameSetup §8§m---------------");
        GameSetup.getInstance().getMessager().send(player, "§7/setup help §8│ §3zeigt alle Befehle.");
        GameSetup.getInstance().getMessager().send(player, "§7/setup version §8│ §3gibt die aktuelle Version des Plugins zurück.");
        GameSetup.getInstance().getMessager().send(player, "§7/setup keys §8│ §3gibt alle geladenen locationKeys zurück.");
        GameSetup.getInstance().getMessager().send(player, "§7/setup list {gamemode} §8│ §3gibt eine Liste aller keys eines Spielmodus zurück.");
        GameSetup.getInstance().getMessager().send(player, "§7/setup check {locationKey} §8│ §3prüft ob die Location in der aktuellen Welt existiert, und gibt diese zurück.");
        GameSetup.getInstance().getMessager().send(player, "§7/setup set {gamemode} {locationKey} §8│ §3setzt eine Location in der aktuellen Welt.");
    }

    private void sendHover(Player player, String key) {
        CoreLocation coreLocation = CoreSystem.getInstance().getWorldManager().getWorld(player.getWorld()).getLocation(key);
        BaseComponent[] baseComponents;

        if (coreLocation != null) {
            baseComponents = new ComponentBuilder("§8➥ §f" + key + " §8│ §7Gesetzt: §aJA").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            "§8X: §7§o" + coreLocation.getX() +
                                    " §8Y: §7§o" + coreLocation.getY() +
                                    " §8Z: §7§o" + coreLocation.getZ() +
                                    " §8Yaw: §7§o" + coreLocation.getYaw() +
                                    " §8Pitch: §7§o" + coreLocation.getPitch()
                    ).create())).create();
        } else {
            baseComponents = new ComponentBuilder("§8➥ §f" + key + " §8│ §7Gesetzt: §cNEIN").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(
                            "§4Location nicht verfügbar!"
                    ).create())).create();
        }

        player.spigot().sendMessage(baseComponents);
    }
}
