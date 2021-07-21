package us.ajg0702.queue.spigot;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class will be registered through the register-method in the 
 * plugins onEnable-method.
 */
public class Placeholders extends PlaceholderExpansion {

    private Main plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public Placeholders(Main plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "ajqueue";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }
    
    HashMap<Player, HashMap<String, String>> responseCache = new HashMap<>();
    
    public void cleanCache() {
    	Iterator<Player> it = responseCache.keySet().iterator();
    	while(it.hasNext()) {
    		Player p = it.next();
    		if(p == null) {
    			it.remove();
    			continue;
    		}
    		if(!p.isOnline()) {
    			it.remove();
    		}
    	}
    }

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A org.bukkit.PkPlayer Player.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, final String identifier){
    	//Bukkit.getLogger().info("itentifier: "+identifier);


        if(player == null) return "No player";
        
        
    	String noc = "_nocache";
    	if(identifier.length() > noc.length()) {
    		int olen = identifier.length()-noc.length();
        	if(identifier.indexOf(noc) == olen) {
        		String idfr = identifier.substring(0, olen);
        		return this.parsePlaceholder(player, idfr);
        	}
    	}
    	
    	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
    		public void run() {
    			HashMap<String, String> playerCache;
    			if(responseCache.containsKey(player)) {
    				playerCache = responseCache.get(player);
    			} else {
    				playerCache = new HashMap<String, String>();
    			}
    			if(playerCache.size() > 75) {
    				try {
    					playerCache.remove(playerCache.keySet().toArray()[0]);
    				} catch(ConcurrentModificationException e) {
    					Bukkit.getScheduler().runTask(plugin, new Runnable() {
    						public void run() {
    							playerCache.remove(playerCache.keySet().toArray()[0]);
    						}
    					});
    				}
    			}
    			String resp = parsePlaceholder(player, identifier);
    			if(resp == null) return;
    			playerCache.put(identifier, resp);
    			responseCache.put(player, playerCache);
    		}
    	});
    	
    	
    	if(responseCache.containsKey(player)) {
    		HashMap<String, String> playerCache = responseCache.get(player);
    		if(playerCache.containsKey(identifier)) {
    			return playerCache.get(identifier);
    		}
    	} else {
    		if(identifier.equalsIgnoreCase("queued")) {
    			return "None";
    		}
    		if(identifier.equalsIgnoreCase("position") || identifier.equalsIgnoreCase("of")) {
    			return "None";
    		}
    		if(identifier.equalsIgnoreCase("inqueue")) {
    			return "false";
    		}
    		if(identifier.matches("queuedfor_*.*")) {
        		return "0";
        	}
    	}
    	

        return null;
    }
    
    private String parsePlaceholder(Player player, String identifier) {
    	if(identifier.equalsIgnoreCase("queued")) {
        	plugin.sendMessage(player, "queuename", "");
        	return null;
        }
    	if(identifier.equalsIgnoreCase("position")) {
    		plugin.sendMessage(player, "position", "");
    		return null;
    	}
    	if(identifier.equalsIgnoreCase("of")) {
    		plugin.sendMessage(player, "positionof", "");
    		return null;
    	}
    	if(identifier.equalsIgnoreCase("inqueue")) {
    		plugin.sendMessage(player, "inqueue", "");
    		return null;
    	}
    	if(identifier.matches("queuedfor_*.*")) {
    		plugin.sendMessage(player, "queuedfor", identifier.split("_")[1]);
    		return null;
    	}
        
        
        return null;
    }
}
