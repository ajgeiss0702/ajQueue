package us.ajg0702.queue.utils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeMessages {
	static BungeeMessages INSTANCE = null;
	public static BungeeMessages getInstance() {
		return INSTANCE;
	}
	public static BungeeMessages getInstance(Plugin pl) {
		if(INSTANCE == null) {
			INSTANCE = new BungeeMessages(pl);
		}
		return INSTANCE;
	}
	
	Plugin pl;
	ConfigurationProvider cv = ConfigurationProvider.getProvider(YamlConfiguration.class);
	Configuration msgs;
	File msgFile;
	private BungeeMessages(Plugin pl) {
		this.pl = pl;
		loadMessagesFile();
	}
	
	private void loadMessagesFile() {
		msgFile = new File(pl.getDataFolder(), "messages.yml");
		if(!msgFile.exists()) {
			try {
				pl.getDataFolder().mkdirs();
				msgFile.createNewFile();
			} catch (IOException e) {
				pl.getLogger().severe("Unable to create messages file:");
				e.printStackTrace();
			}
		}
		try {
			msgs = cv.load(msgFile);
		} catch (IOException e) {
			pl.getLogger().severe("Unable to load messages file:");
			e.printStackTrace();
			return;
		}
		LinkedHashMap<String, String> d = new LinkedHashMap<>();
		
		
		d.put("status.offline.base", "&cThe server you are queued for is {STATUS}. &7You are in position &f{POS}&7 of &f{LEN}&7.");
		d.put("status.offline.offline", "offline");
		d.put("status.offline.restarting", "restarting");
		d.put("status.online.base", "&7You are in position &f{POS}&7 of &f{LEN}&7. Estimated time: {TIME}");
		d.put("status.left-last-queue", "&aYou left the last queue you were in.");
		d.put("status.now-in-queue", "&aYou are now queued! &7You are in position &f{POS}&7 of &f{LEN}&7.\n&7Type &f/leavequeue&7 to leave the queue!");
		
		d.put("errors.server-not-exist", "&cThat server does not exist!");
		d.put("errors.already-queued", "&cYou are already queued! &7You are in position &f{POS}&7 of &f{LEN}&7.");
		d.put("errors.player-only", "&cThis command can only be executed as a player!");
		d.put("errors.already-connected", "&cYou are already connected to this server!");
		
		d.put("commands.leave-queue", "&aYou left the queue!");
		d.put("commands.reload", "&aConfig and messages reloaded successfully!");
		
		d.put("noperm", "&cYou do not have permission to do this!");
		
		d.put("format.time.mins", "{m}m {s}s");
		d.put("format.time.secs", "{s} seconds");
		
		d.put("list.format", "&b{SERVER} &7({COUNT}): {LIST}");
		d.put("list.playerlist", "&9{NAME}&7, ");
		d.put("list.total", "&7Total players in queues: &f{TOTAL}");
		d.put("list.none", "&7None");
		
		d.put("send", "&aAdded &f{PLAYER}&a to the queue for &f{SERVER}");
		
		
		for(String k : d.keySet()) {
			if(!msgs.contains(k)) {
				msgs.set(k, d.get(k));
			}
		}
		try {
			cv.save(msgs, msgFile);
		} catch (IOException e) {
			pl.getLogger().severe("Unable to save messages file:");
			e.printStackTrace();
		}
	}
	
	public String get(String key) {
		String msg = msgs.get(key, "&cMessage '"+key+"' does not exist!");
		msg = color(msg);
		return msg;
	}
	public BaseComponent[] getBC(String key) {
		String m = get(key);
		return TextComponent.fromLegacyText(m);
	}
	public String color(String msg) {
		return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public void reload() {
		loadMessagesFile();
	}
}
