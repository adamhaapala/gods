package com.dogonfire.gods;

import java.util.Random;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BelieverManager 
{
	private Gods plugin = null;
	private Random random = new Random();
	private FileConfiguration believersConfig = null;
	private File believersConfigFile = null;
//	private HashMap<String, Set<String>> onlineBelievers = new HashMap<String, Set<String>>();
	//private HashMap<String, Set<String>> believers = new HashMap<String, Set<String>>();
	//private long lastCheckTime = -99999;
	//private long lastOnlineCheckTime = -99999;

	BelieverManager(Gods p) 
	{
		this.plugin = p;
	}

	public void load() 
	{
		if (this.believersConfigFile == null) 
		{
			believersConfigFile = new File(this.plugin.getDataFolder(), "believers.yml");
		}

		believersConfig = YamlConfiguration.loadConfiguration(this.believersConfigFile);

		plugin.log("Loaded " + believersConfig.getKeys(false).size() + " believers.");
	}

	public void save() 
	{
		if ((this.believersConfig == null) || (this.believersConfigFile == null)) 
		{
			return;
		}

		try {
			this.believersConfig.save(this.believersConfigFile);
		} catch (Exception ex) {
			this.plugin.log("Could not save config to "
					+ this.believersConfigFile + ": " + ex.getMessage());
		}
	}

	public void setReligionChat(String believerName, boolean enabled) 
	{
		if (enabled) 
		{
			believersConfig.set(believerName + ".ReligionChat", Boolean.valueOf(true));
		} 
		else 
		{
			believersConfig.set(believerName + ".ReligionChat", null);
		}

		save();
	}

	public boolean getReligionChat(String believerName) 
	{
		return believersConfig.getBoolean(believerName + ".ReligionChat");
	}

	public String getGodForBeliever(String believerName) 
	{
		return believersConfig.getString(believerName + ".God");
	}

	public Set<String> getBelievers() 
	{
		Set<String> allBelievers = believersConfig.getKeys(false);

		return allBelievers;
	}

	public String getNearestBeliever(Location location) 
	{
		Set<String> allBelievers = believersConfig.getKeys(false);

		double minLength = 5.0D;
		Player minPlayer = null;

		for (String believerName : allBelievers) 
		{
			Player player = this.plugin.getServer().getPlayer(believerName);

			if ((player != null) && (player.getWorld() == location.getWorld())) 
			{
				double length = player.getLocation().subtract(location).length();

				if (length < minLength) 
				{
					minLength = length;
					minPlayer = player;
				}
			}
		}
		
		if (minPlayer == null) 
		{
			return null;
		}

		return minPlayer.getName();
	}

	//NOTE: Concurrent hash issue
	public Set<String> getBelieversForGod(String godName) 
	{
		//HashSet<String> allBelievers = new HashSet<String>();
		
//		if(!believers.containsKey(godName))
//		{
//			believers.put(godName, new HashSet<String>());
//		}

	//	if(System.currentTimeMillis() - lastCheckTime - random.nextInt(2000) > 60000)
	//	{
			Set<String> allBelievers = believersConfig.getKeys(false);
			Set<String> believers = new HashSet<String>();
			
			//believers.get(godName).clear();

			for (String believerName : allBelievers) 
			{
				String believerGod = getGodForBeliever(believerName);

				if ((believerGod != null) && (believerGod.equals(godName))) 
				{
					believers.add(believerName);
				}
			}

		//	lastCheckTime = System.currentTimeMillis();
		//}

		return believers;
	}

	public Set<String> getOnlineBelieversForGod(String godName) 
	{		
/*
 		if(!onlineBelievers.containsKey(godName))
 
		{
			onlineBelievers.put(godName, new HashSet<String>());
		}

		if(System.currentTimeMillis() - lastOnlineCheckTime - random.nextInt(2000) > 60000)
		{
			Set<String> allBelievers = this.believersConfig.getKeys(false);

			onlineBelievers.get(godName).clear();
		
			for (String believerName : allBelievers) 
			{
				if (this.plugin.getServer().getPlayer(believerName) != null) 
				{
					String believerGod = getGodForBeliever(believerName);

					if ((believerGod != null) && (believerGod.equals(godName))) 
					{
						onlineBelievers.get(godName).add(believerName);
					}
				}
			}
			
			lastOnlineCheckTime = System.currentTimeMillis();
		}
		
		return onlineBelievers.get(godName);
		*/

		Set<String> allBelievers = believersConfig.getKeys(false);
		Set<String> believers = new HashSet<String>();
	
		for (String believerName : allBelievers) 
		{
			if (plugin.getServer().getPlayer(believerName) != null) 
			{
				String believerGod = getGodForBeliever(believerName);

				if ((believerGod != null) && (believerGod.equals(godName))) 
				{
					believers.add(believerName);
				}
			}
		}

		return believers;
	}

	public boolean hasRecentPriestOffer(String believerName) 
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();
		Date offerDate = null;

		String offerDateString = this.believersConfig.getString(believerName + ".LastPriestOffer");
		
		try 
		{
			offerDate = formatter.parse(offerDateString);
		} 
		catch (Exception ex) 
		{
			offerDate = new Date();
			offerDate.setTime(0L);
		}

		long diff = thisDate.getTime() - offerDate.getTime();
		long diffMinutes = diff / 60000L;

		return diffMinutes <= 60L;
	}

	public void clearPendingPriest(String believerName) 
	{
		this.believersConfig.set(believerName + ".LastPriestOffer", null);
		save();
	}

	public void setPendingPriest(String believerName) 
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".LastPriestOffer", formatter.format(thisDate));
		save();
	}

	boolean getChangingGod(String believerName) 
	{
		String changingGodString = this.believersConfig.getString(believerName + ".ChangingGod");

		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date changingGodDate = null;
		boolean changing = false;
		Date thisDate = new Date();
		
		try 
		{
			changingGodDate = formatter.parse(changingGodString);

			long diff = thisDate.getTime() - changingGodDate.getTime();
			long diffSeconds = diff / 1000L;

			changing = diffSeconds <= 10L;
		} 
		catch (Exception ex) 
		{
			changing = false;
		}

		return changing;
	}

	void clearChangingGod(String believerName) 
	{
		this.believersConfig.set(believerName + ".ChangingGod", null);

		save();
	}

	void setChangingGod(String believerName) 
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".ChangingGod", formatter.format(thisDate));
		save();
	}

	public boolean isHunting(String believerName) 
	{
		boolean hunting = false;
						
		try 
		{
			hunting = believersConfig.getBoolean(believerName + ".Hunting");
			//plugin.logDebug("isHunting string '" + huntingString + "' (" + hunting + ") for " + believerName);
		} 
		catch (Exception ex) 
		{
			//plugin.logDebug("Invalid isHunting string '" + huntingString + "' for " + believerName + ". Setting to false.");
			setHunting(believerName, false);
		}

		return hunting;
	}
	
	public void setHunting(String believerName, boolean hunting) 
	{
		plugin.logDebug("Setting hunting string to '" + hunting + "' for " + believerName);

		believersConfig.set(believerName + ".Hunting", hunting);

		save();
	}

	public void setInvitationTime(String believerName) 
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".LastInvitationTime", formatter.format(thisDate));

		save();
	}
	
	public Date getLastPrayerTime(String believerName) 
	{
		String lastPrayerString = believersConfig.getString(believerName + ".LastPrayer");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastPrayerDate = null;
		
		try 
		{
			lastPrayerDate = formatter.parse(lastPrayerString);
		} 
		catch (Exception ex) 
		{
			lastPrayerDate = new Date();
			lastPrayerDate.setTime(0L);
		}

		return lastPrayerDate;
	}

	public void setItemBlessingTime(String believerName) 
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".LastItemBlessingTime", formatter.format(thisDate));

		save();
	}

	public void setBlessingTime(String believerName) 
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		this.believersConfig.set(believerName + ".LastBlessingTime", formatter.format(thisDate));

		save();
	}

	public void setCursingTime(String believerName) 
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".LastCursingTime", formatter.format(thisDate));

		save();
	}

	public boolean hasRecentItemBlessing(String believerName) 
	{
		String lastItemBlessingString = this.believersConfig.getString(believerName + ".LastItemBlessingTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastItemBlessingDate = null;
		Date thisDate = new Date();
		try 
		{
			lastItemBlessingDate = formatter.parse(lastItemBlessingString);
		} 
		catch (Exception ex) 
		{
			lastItemBlessingDate = new Date();
			lastItemBlessingDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastItemBlessingDate.getTime();
		long diffMinutes = diff / 60000L;

		return diffMinutes < this.plugin.minItemBlessingTime;
	}

	public boolean hasRecentBlessing(String believerName) 
	{
		String lastItemBlessingString = this.believersConfig.getString(believerName + ".LastBlessingTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastItemBlessingDate = null;
		Date thisDate = new Date();
		try 
		{
			lastItemBlessingDate = formatter.parse(lastItemBlessingString);
		} 
		catch (Exception ex) 
		{
			lastItemBlessingDate = new Date();
			lastItemBlessingDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastItemBlessingDate.getTime();
		long diffMinutes = diff / 60000L;
		long diffSeconds = diff / 1000L;

		return diffSeconds < plugin.minBlessingTime;
	}

	public boolean hasRecentCursing(String believerName) 
	{
		String lastItemBlessingString = this.believersConfig.getString(believerName + ".LastCursingTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastItemBlessingDate = null;
		Date thisDate = new Date();
		try {
			lastItemBlessingDate = formatter.parse(lastItemBlessingString);
		} catch (Exception ex) {
			lastItemBlessingDate = new Date();
			lastItemBlessingDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastItemBlessingDate.getTime();
		long diffSeconds = diff / 1000L;

		return diffSeconds < this.plugin.minCursingTime;
	}

	public void setInvitation(String believerName, String godName) 
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".Invitation.Time", formatter.format(thisDate));
		believersConfig.set(believerName + ".Invitation.God", godName);

		save();
	}

	public String getInvitation(String believerName) 
	{
		String pattern = "HH:mm:ss dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();
		Date offerDate = null;

		String offerDateString = believersConfig.getString(believerName + ".Invitation.Time");
		
		try 
		{
			offerDate = formatter.parse(offerDateString);
		} 
		catch (Exception ex) 
		{
			offerDate = new Date();
			offerDate.setTime(0L);
		}

		long diff = thisDate.getTime() - offerDate.getTime();
		long diffSeconds = diff / 1000L;

		if(diffSeconds>30)
		{
			believersConfig.set(believerName + ".Invitation", null);
			
			save();
			
			return null;
		}
		
		return believersConfig.getString(believerName + ".Invitation.God");
	}

	public void clearInvitation(String believerName) 
	{
		believersConfig.set(believerName + ".Invitation", null);
		
		save();
	}
/*	
	public boolean hasExpiredInvitation(String believerName) 
	{
		String lastItemBlessingString = believersConfig.getString(believerName + ".LastInvitationTime");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastItemBlessingDate = null;
		Date thisDate = new Date();

		try 
		{
			lastItemBlessingDate = formatter.parse(lastItemBlessingString);
		} 
		catch (Exception ex) 
		{
			lastItemBlessingDate = new Date();
			lastItemBlessingDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastItemBlessingDate.getTime();
		long diffSeconds = diff / 1000L;

		return diffSeconds > plugin.maxInvitationTimeSeconds;
	}
*/
	public void removeInvitation(String invitedPlayer)
	{
		believersConfig.set(invitedPlayer + ".LastInvitationTime", null);
		
		save();
	}
	
	public int getPrayers(String believerName) 
	{
		int prayers = this.believersConfig.getInt(believerName + ".Prayers");

		return prayers;
	}

	public void clearGodForBeliever(String believerName) 
	{
		this.believersConfig.set(believerName, null);

		save();
	}
	
	public void removeBeliever(String godName, String believerName) 
	{
		String believerGodName = believersConfig.getString(believerName + ".God");

		if (believerGodName != null && !believerGodName.equals(godName)) 
		{
			return;
		}

		believersConfig.set(believerName, null);

		plugin.log(godName + " lost " + believerName + " as believer");

		save();
	}

	public float getBelieverPower(String believer) 
	{
		Date date = new Date();

		float time = 1.0F + 2.5E-008F * (float) (date.getTime() - plugin.getBelieverManager().getLastPrayerTime(believer).getTime());
		
		return plugin.getBelieverManager().getPrayers(believer) / time;
	}

	public void believerLeave(String godName, String believerName) 
	{
		String believerGodName = believersConfig.getString(believerName + ".God");

		if (!believerGodName.equals(godName)) 
		{
			return;
		}

		believersConfig.set(believerName + ".God", null);

		save();
	}

	public void removePrayer(String believerName) 
	{
		believersConfig.set(believerName + ".LastPrayer", null);

		save();
	}

	public boolean addPrayer(String believerName, String godName) 
	{
		String lastPrayer = this.believersConfig.getString(believerName + ".LastPrayer");

		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date lastPrayerDate = null;
		Date thisDate = new Date();
		
		try 
		{
			lastPrayerDate = formatter.parse(lastPrayer);
		} 
		catch (Exception ex) 
		{
			lastPrayerDate = new Date();
			lastPrayerDate.setTime(0L);
		}

		int prayers = believersConfig.getInt(believerName + ".Prayers");
		String oldGod = believersConfig.getString(believerName + ".God");
		
		if ((oldGod != null) && (!oldGod.equals(godName))) 
		{
			prayers = 0;
			lastPrayerDate.setTime(0L);
		}

		long diff = thisDate.getTime() - lastPrayerDate.getTime();

		long diffMinutes = diff / 60000L;

		if (diffMinutes < plugin.minBelieverPrayerTime) 
		{
			return false;
		}

		prayers++;

		this.believersConfig.set(believerName + ".LastPrayer", formatter.format(thisDate));
		this.believersConfig.set(believerName + ".God", godName);
		this.believersConfig.set(believerName + ".Prayers", Integer.valueOf(prayers));

		save();

		return true;
	}

	public void setLastPrayerDate(String believerName)
	{
		String pattern = "HH:mm dd-MM-yyyy";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date thisDate = new Date();

		believersConfig.set(believerName + ".LastPrayer", formatter.format(thisDate));
	}
	
	public boolean reducePrayer(String believerName, int n) 
	{
		int prayers = this.believersConfig.getInt(believerName + ".Prayers");

		prayers -= n;

		if (prayers < 0) 
		{
			prayers = 0;
		}

		this.believersConfig.set(believerName + ".Prayers", Integer.valueOf(prayers));

		save();

		return true;
	}

	public boolean incPrayer(String believerName, String godName) 
	{
		int prayers = this.believersConfig.getInt(believerName + ".Prayers");

		prayers++;

		this.believersConfig.set(believerName + ".God", godName);
		this.believersConfig.set(believerName + ".Prayers", Integer.valueOf(prayers));

		save();

		return true;
	}
}