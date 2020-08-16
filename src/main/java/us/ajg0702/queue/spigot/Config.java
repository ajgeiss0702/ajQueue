package us.ajg0702.queue.spigot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	File f;
	YamlConfiguration yml;
	
	JavaPlugin pl;
	
	
	public boolean getBoolean(String key) {
		return yml.getBoolean(key);
	}
	
	public Config(JavaPlugin pl) {
		this.pl = pl;
		f = new File(pl.getDataFolder(), "config.yml");
		if(!f.exists()) {
			if(!Files.exists(pl.getDataFolder().toPath())) {
				try {
					Files.createDirectory(pl.getDataFolder().toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				PrintWriter writer = new PrintWriter(pl.getDataFolder()+File.separator+"config.yml", "UTF-8");
				String[] lines = getDefaultConfig().split("\n");
				for(String line : lines) {
					writer.println(line);
					
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		yml = YamlConfiguration.loadConfiguration(f);
	}
	
	public String getDefaultConfig() {
		return    "# This is the config for the spigot side.\n"
				+ "# You can find more settings in the config of bungee.\n"
				+ "\n\n"
				+ "# Should we send queue requests from commands in batches?\n"
				+ "# Enable this if you have issues with players sometimes not executing commands correctly\n"
				+ "# Note though that it could delay queue commands by up to 1 second!\n"
				+ "send-queue-commands-in-batches: false";
	}
}
