package net.Snicktrix.CombatLog;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Luke on 7/26/14.
 */
public class Events implements Listener {
    private CombatLog combatLog;

    public Events(CombatLog combatLog) {
        this.combatLog = combatLog;
    }

    @EventHandler
    public void playerDamage(EntityDamageByEntityEvent event) {
        double damage = event.getDamage();
        if (event.getDamage() == 0) {
            return;
        }
        //Check to make sure its a player receiving the damage
        //And its a player dealing the damage
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player hurt = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            this.combatLog.tracked.enable(hurt);
            this.combatLog.tracked.enable(attacker);
            return;
        }

        //Check to see if a player shot an arrow at another player
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            Player hurt = (Player) event.getEntity();

            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player) {
                Player attacker = (Player) arrow.getShooter();
                this.combatLog.tracked.enable(attacker);
                this.combatLog.tracked.enable(hurt);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (this.combatLog.tracked.inCombat(player)) {
            this.deathDrop(player);
            this.combatLog.tracked.quitDisable(player);

            //Lets publicly mock the player for doing this!
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName()
                    + ChatColor.RED + " has combat logged!");
        }
    }

    private void deathDrop(Player player) {
        player.setHealth(0);

        if (player.getInventory().getContents() == null) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                player.getLocation().getWorld().dropItem(player.getLocation(), item);
            }
        }

        player.getInventory().clear();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (this.combatLog.tracked.inCombat(player)) {
            this.combatLog.tracked.quitDisable(player);
        }
    }

    @EventHandler
    public void preProcessCommand(PlayerCommandPreprocessEvent event) {
        //Check if player is an op or if the player is not in combat
        if (event.getPlayer().isOp() || !this.combatLog.tracked.inCombat(event.getPlayer())) {
            return;
        }

        String msg = event.getMessage();
        if (msg.contains("/tpa")
                || msg.contains("/spawn")
                || msg.contains("/home")
                || msg.contains("/warp")
                || msg.contains("spawn")
                || msg.contains("/back")
                || msg.contains("outpost")) {

            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot use this command while in combat");
        }
    }
}
