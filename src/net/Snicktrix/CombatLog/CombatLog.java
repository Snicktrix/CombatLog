package net.Snicktrix.CombatLog;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Luke on 7/26/14.
 */
public class CombatLog extends JavaPlugin {
    public Tracked tracked;
    private Events events;

    public void onEnable() {
        //Register Tracked Class
        this.tracked = new Tracked(this, 4, 7);

        //Register Event Listener
        this.events = new Events(this);
        Bukkit.getPluginManager().registerEvents(this.events, this);

        //Were done! Lets tell the admins that!
        System.out.println("Combat Log prevention has been enabled");
    }


}
