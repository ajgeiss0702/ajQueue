package us.ajg0702.queue.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfig {
	
	File f;
	Configuration config;
	
	ConfigurationProvider cv = ConfigurationProvider.getProvider(YamlConfiguration.class);
	
	String nfm = "Could not find KEY in config! Try restarting the server or deleting the config and allowing the plugin to re-create it.";
	
	public Object get(String key) {
		Object r = config.get(key);
		if(r == null) {
			pl.getLogger().severe(nfm.replace("KEY", key));;
		}
		return r;
	}
	public Integer getInt(String key) {
		int r = config.getInt(key, -38964298);
		if(r == -38964298) {
			pl.getLogger().severe(nfm.replace("KEY", key));;
		}
		return r;
	}
	public String getString(String key) {
		String r = config.getString(key);
		if(r == null) {
			pl.getLogger().severe(nfm.replace("KEY", key));;
		}
		return r;
	}
	public List<String> getStringList(String key) {
		List<String> r = config.getStringList(key);
		if(r == null) {
			pl.getLogger().severe(nfm.replace("KEY", key));;
		}
		return r;
	}
	public boolean getBoolean(String key) {
		return config.getBoolean(key);
	}
	
	public String getDefaultConfig() throws IOException {
		BufferedReader stream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/config.yml")));
		StringBuilder configfile = new StringBuilder();
		String line;
		while((line = stream.readLine()) != null) {
			configfile.append(line+"\n");
		}
		return configfile.toString();
	}
	
	public String getConfigString() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(pl.getDataFolder().getPath().toString(), "config.yml"));
		String end = "";
		for(String line : lines) {
			end += line+"\n";
		}
		return end;
	}
	Plugin pl;
	@SuppressWarnings("unused")
	public BungeeConfig(Plugin plugin) {
		pl = plugin;
		f = new File(plugin.getDataFolder(), "config.yml");
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
			//Bukkit.getLogger().info(configfile.toString());
		} else {
			try {
				f = new File(pl.getDataFolder(), "config.yml");
				Configuration oldconfig = cv.load(f);
				String newConfig = getDefaultConfig();
				int oldver = oldconfig.getInt("config-version", 0);
				String strv = newConfig
						.split("config\\-version: ")[1]
						.split("\n")[0];
				//pl.getLogger().info("New config version: "+strv);
				int newver = Integer.parseInt(strv);
				if(oldver < newver) {
					pl.getLogger().info("Starting config converter!");
					Date date = Calendar.getInstance().getTime();  
	                DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");  
	                String strDate = dateFormat.format(date);  
					duplicateFile(f, new File(pl.getDataFolder(), "config.yml.old."+strDate));
					String oldConfig = getConfigString();
					f = new File(pl.getDataFolder(), "config.yml");
					config = cv.load(f);
					for(String key : oldconfig.getKeys()) {
						pl.getLogger().info("Key: "+key);
						String[] keyParts = key.split("\\.");
						//pl.getLogger().info("Key len: "+keyParts.length);
						String keySec = keyParts[keyParts.length-1];
						pl.getLogger().info("keySec: "+keySec);
						String newVal = newConfig.split(keySec)[1].split("\n")[0];
						int i = 0;
						for(String l : newConfig.split(keySec)[1].split("\n")) {
							if(i == 0) {
								i++;
								continue;
							}
							i++;
							pl.getLogger().info("Scanning: "+l);
							if(l.startsWith("-")) {
								newVal += "\n"+l;
							} else {
								break;
							}
						}
						String find = keySec+ newVal;
						String replace = keySec + ": " + oldconfig.get(key);
						replace = replace.replaceAll("\\[", "\n- ");
						replace = replace.replaceAll("\\, ", "\n- ");
						replace = replace.replaceAll("\\]", "");
						pl.getLogger().info("Find: "+find +" Replace w: "+replace);
						if(!keySec.equals("config-version")) {
							newConfig = newConfig.replaceAll(find, replace);
						}
					}
					PrintWriter writer = new PrintWriter(f, "UTF-8");
					String[] lines = newConfig.split("\n");
					for(String line : lines) {
						writer.println(line);
					}
					writer.close();
				}
			} catch (IOException e) {
				pl.getLogger().severe("Unable to load default config! " + e.getMessage() + "\n" + e.getStackTrace().toString());;
			}
		}
		
		f = new File(pl.getDataFolder(), "config.yml");
		try {
			config = cv.load(f);
		} catch (IOException e) {
			pl.getLogger().warning("Unable to reload the config:");
			e.printStackTrace();
		}
	}
	public void reload() {
		try {
			config = cv.load(f);
		} catch (IOException e) {
			pl.getLogger().warning("Unable to reload the config:");
			e.printStackTrace();
		}
	}
	
	
	
	private static void duplicateFile(File source, File destination) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(destination);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
}
