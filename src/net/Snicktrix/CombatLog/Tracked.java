package net.Snicktrix.CombatLog;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by Luke on 7/26/14.
 */
public class Tracked {
    private CombatLog combatLog;

    //Integer acts as the time spent
    private HashMap<String, Integer> combatTime = new HashMap<String, Integer>();
    private HashMap<String, Location> combatLocation = new HashMap<String, Location>();
    private HashMap<String, Integer> lastCombatTime = new HashMap<String, Integer>();
    private int time;
    private int alertTime;

    public Tracked(CombatLog combatLog, int time, int alertTime) {
        this.time = time;
        this.alertTime = alertTime;
        this.combatLog = combatLog;

        this.startCountdown(combatLog);
    }

    //Add the player to the tracked list
    public void enable(Player player) {

        if (this.combatTime.containsKey(player.getName())) {
            this.combatTime.put(player.getName(), time);
        } else {
            this.combatTime.put(player.getName(), time);
            player.sendMessage(ChatColor.RED + "You are now in combat");
        }

        this.lastCombatTime.put(player.getName(), this.alertTime);
    }

    //The player must be in the clear, remove them from the tracked list
    public void disable(Player player) {
        if (this.combatTime.containsKey(player.getName())) {
            //Remove them from combat list
            combatTime.remove(player.getName());
            player.sendMessage(ChatColor.GREEN + "You are out of combat");
        }

        if (this.lastCombatTime.containsKey(player.getName())) {
            lastCombatTime.remove(player.getName());
        }

        if (this.combatLocation.containsKey(player.getName())) {
            combatLocation.remove(player.getName());
        }
    }

    public void quitDisable(Player player) {
        if (this.combatTime.containsKey(player.getName())) {
            //Remove them from combat list
            combatTime.remove(player.getName());
        }

        if (this.lastCombatTime.containsKey(player.getName())) {
            lastCombatTime.remove(player.getName());
        }

        if (this.combatLocation.containsKey(player.getName())) {
            combatLocation.remove(player.getName());
        }
    }

    //Check to see if the player is in combat
    public boolean inCombat(Player player) {
        //If we have them in our list
        if (this.combatTime.containsKey(player.getName())) {
            return true;
        }
        //They are in the clear :D
        return false;
    }

    private Location getLastLoc(Player player) {
        if (this.combatLocation.containsKey(player.getName())) {
            return this.combatLocation.get(player.getName());
        }
//        player.sendMessage("getLastLoc is returning null");
        return null;
    }

    //Lets start lowering the times every second
    private void startCountdown(final CombatLog combatLog) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(combatLog, new Runnable() {
            @Override
            public void run() {

                if (combatTime.size() == 0) return;

                for (String name : combatTime.keySet()) {
                    Player player = Bukkit.getPlayerExact(name);
                    Location loc = player.getLocation();
                    int previousTime = combatTime.get(name) - 1;

                    Location lastLoc = getLastLoc(player);
                    //Check for XYZ values to allow player to move head without canceling
                    if (lastLoc != null && loc.getX() == lastLoc.getX() && loc.getY() == lastLoc.getY() && loc.getZ() == lastLoc.getZ()) {
                        if (previousTime <= 0) {
                            //Out of combat
                            disable(player);
//                            player.sendMessage("disabled");
                        } else {
                            combatTime.put(player.getName(), previousTime);
                            player.sendMessage(ChatColor.RED + "[Combat Timer] "
                               + ChatColor.YELLOW + Integer.toString(previousTime));
                            player.playSound(player.getLocation(), Sound.NOTE_PLING, 10, 1);
                        }
                    //Last location is null or different
                    } else {
                        //Set to max time
                        combatTime.put(name, time);
                        combatLocation.put(name, loc);
//                        player.sendMessage(Integer.toString(time));
                    }
                }
                //***********************************************************//
                //Notify player how to leave combat

                for (String name : lastCombatTime.keySet()) {
                    lastCombatTime.put(name, lastCombatTime.get(name) - 1);

                    //Check if we should alert the player about the message
                    if (lastCombatTime.get(name) <= 0) {
                        //Alert the player
                        Player player = Bukkit.getPlayerExact(name);
                        player.sendMessage(ChatColor.RED + "Stand still to get out of combat!");
                        lastCombatTime.put(name, 10);
                    }
                }


            }
        }, 0, 20);
    }

}
