package com.dogonfire.gods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dogonfire.gods.HolyBook;

public class HolyBookManager 
{
	private Gods plugin = null;
	private FileConfiguration biblesConfig = null;
	private File biblesConfigFile = null;

	HolyBookManager(Gods gods) 
	{
		this.plugin = gods;
	}

	public void load() 
	{
		if (this.biblesConfigFile == null) 
		{
			this.biblesConfigFile = new File(this.plugin.getDataFolder(), "bibles.yml");
		}

		this.biblesConfig = YamlConfiguration.loadConfiguration(this.biblesConfigFile);

		this.plugin.log("Loaded " + this.biblesConfig.getKeys(false).size() + " bibles.");
	}

	public void save() 
	{
		if (biblesConfig == null || biblesConfigFile == null) 
		{
			return;
		}

		try 
		{
			biblesConfig.save(this.biblesConfigFile);
		} 
		catch (Exception ex) 
		{
			plugin.log("Could not save config to " + this.biblesConfigFile + ": " + ex.getMessage());
		}
	}

	private void initBible(String godName) 
	{
		this.plugin.logDebug("Creating bible for '" + godName + "' ...");

		List<String> pages = new ArrayList<String>();

		this.biblesConfig.set(godName + ".Title", "Holy Book of " + godName);

		this.biblesConfig.set(godName + ".Author", godName);

		this.plugin.getLanguageManager().setPlayerName(godName);

		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText1));

		plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(plugin.getGodManager().getUnholyMobTypeForGod(godName)));
		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText2));

		plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(plugin.getGodManager().getEatFoodTypeForGod(godName)));
		
		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText3));

		plugin.getLanguageManager().setType(plugin.getLanguageManager().getItemTypeName(plugin.getGodManager().getNotEatFoodTypeForGod(godName)));

		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText4));

		plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(plugin.getGodManager().getHolyMobTypeForGod(godName)));
		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText5));

		plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(plugin.getGodManager().getHolyMobTypeForGod(godName)));
		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText6));

		plugin.getLanguageManager().setType(plugin.getLanguageManager().getMobTypeName(plugin.getGodManager().getHolyMobTypeForGod(godName)));
		pages.add(plugin.getLanguageManager().getLanguageStringForBook(godName, LanguageManager.LANGUAGESTRING.DefaultBibleText7));

		biblesConfig.set(godName + ".Pages", pages);

		save();
		
		if(plugin.propheciesEnabled)
		{
			plugin.getProphecyManager().generateProphecies(godName);
		}
	}
	
	public void clearBible(String godName) 
	{
		biblesConfig.set(godName + "", null);

		save();
	}
	

	private void setBible(String godName, String priestName, ItemStack book) 
	{
		HolyBook b = null;
	
		try 
		{
			b = new HolyBook(book);
		} 
		catch (Exception ex) 
		{
			plugin.logDebug("ERROR: Could not set a bible for '" + godName + ": " + ex.getMessage());
		}

		biblesConfig.set(godName + ".Title", b.getTitle());
		biblesConfig.set(godName + ".Author", priestName);
		biblesConfig.set(godName + ".Pages", b.getPages());

		save();
	}
	

	public boolean setBible(String godName, String priestName) 
	{
		Player player = plugin.getServer().getPlayer(priestName);

		if (player == null) 
		{
			return false;
		}

		ItemStack item = player.getItemInHand();

		if ((item == null) || ((item.getType() != Material.WRITTEN_BOOK) && (item.getType() != Material.BOOK_AND_QUILL))) 
		{
			return false;
		}

		//CraftItemStack book = (CraftItemStack) item;

		setBible(godName, player.getName(), item);

		item.setType(Material.WRITTEN_BOOK);

		return true;
	}

	public String getBibleTitle(String godName) 
	{
		return this.biblesConfig.getString(godName + ".Title");
	}

	public ItemStack getBible(String godName) 
	{
		List<String> pages = biblesConfig.getStringList(godName + ".Pages");

		if (pages.size() == 0) 
		{
			initBible(godName);
			pages = biblesConfig.getStringList(godName + ".Pages");
		}

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		
		try 
		{
			HolyBook b = new HolyBook(book);

			b.setTitle(biblesConfig.getString(godName + ".Title"));
			//b.setAuthor(biblesConfig.getString(godName + ".Author"));
			b.setAuthor(godName);
			b.setPages(pages);
			
			return b.getItem();
		} 
		catch (Exception ex) 
		{
			plugin.logDebug("ERROR: Could not instance a bible for '" + godName + ": " + ex.getMessage());
			return null;
		}
	}

	public String getGodForBible(ItemStack book) 
	{
		//CraftItemStack craftBook = (CraftItemStack) book;
		
		try 
		{
			HolyBook bible = new HolyBook(book);

			for (String god : biblesConfig.getKeys(false)) 
			{
				if (god.equals(bible.getAuthor())) 
				{
					return god;
				}
			}
		} 
		catch (Exception ex) 
		{
			plugin.log("ERROR: Could not get bible for " + book + ": " + ex.getMessage());
		}

		return null;
	}

	public ItemStack getEditBible(String godName) 
	{
		List<String> pages = this.biblesConfig.getStringList(godName + ".Pages");

		if (pages.size() == 0) 
		{
			initBible(godName);
			pages = biblesConfig.getStringList(godName + ".Pages");
		}

		ItemStack book = new ItemStack(Material.BOOK_AND_QUILL);
		try 
		{
			HolyBook b = new HolyBook(book);

			b.setTitle(this.biblesConfig.getString(godName + ".Title"));
			b.setAuthor(godName);

			for (int i = 0; i < pages.size(); i++) 
			{
				pages.set(i, (String) pages.get(i));
			}

			b.setPages(pages);
			
			return b.getItem();
		} 
		catch (Exception ex) 
		{
			this.plugin.logDebug("ERROR: Could not instance a bible for '" + godName + ": " + ex.getMessage());
			return null;
		}
	}

	public boolean giveBible(String godName, String playerName) 
	{
		ItemStack bible = getBible(godName);

		if (bible == null) 
		{
			return false;
		}

		Player player = this.plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			plugin.logDebug("ERROR: Could give bible to offline player '" + playerName);
			return false;
		}

		int amount = player.getPlayer().getItemInHand().getAmount();
		ItemStack[] itemStack = { player.getPlayer().getItemInHand() };
		itemStack[0].setAmount(amount);
		player.getInventory().addItem(itemStack);

		player.setItemInHand(bible);

		return true;
	}

	public boolean giveEditBible(String godName, String playerName) 
	{
		ItemStack bible = getEditBible(godName);

		if (bible == null) 
		{
			return false;
		}

		Player player = this.plugin.getServer().getPlayer(playerName);

		if (player == null) 
		{
			this.plugin.logDebug("ERROR: Could give editable bible to offline player '" + playerName);
			return false;
		}

		int amount = player.getPlayer().getItemInHand().getAmount();
		ItemStack[] itemStack = { player.getPlayer().getItemInHand() };
		itemStack[0].setAmount(amount);
		player.getInventory().addItem(itemStack);

		player.setItemInHand(bible);

		return true;
	}

	public void handleQuestCompleted(String godName, QuestManager.QUESTTYPE type, String playerName) 
	{
	}

	public void setProphecyPages(String godName, List<String> prophecyPages) 
	{
		List<String> pages = biblesConfig.getStringList(godName + ".Pages");
		List<String> newPages = new ArrayList<String>();

		// Get current prophecy index
		for(String page : pages)
		{
			if(page.contains("Prophecies of " + godName))
			{
				break;
			}
		
			newPages.add(page);			
		}

		// Add the prophecy pages
		for(String page : prophecyPages)
		{
			newPages.add(page); 
		}
		
		biblesConfig.set(godName + ".Pages", newPages);

		save();
	}
}
