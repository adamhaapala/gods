package com.dogonfire.gods.tasks;

import com.dogonfire.gods.GodManager;
import com.dogonfire.gods.Gods;
import com.dogonfire.gods.LanguageManager;
import com.dogonfire.gods.LanguageManager.LANGUAGESTRING;
import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealPlayerTask implements Runnable {
	private Gods plugin;
	private Player player = null;
	private String godName = null;
	private LanguageManager.LANGUAGESTRING languageString;

	public HealPlayerTask(Gods instance, String god, Player p, LanguageManager.LANGUAGESTRING speak) 
	{
		this.plugin = instance;
		this.player = p;
		this.godName = new String(god);
		this.languageString = speak;
	}

	private boolean healPlayer() 
	{
		player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 10, 1));

		player.getLocation()
				.getWorld()
				.playEffect(this.player.getLocation(),
						Effect.MOBSPAWNER_FLAMES, 4);
		return true;
	}

	public void run() 
	{
		Random random = new Random();

		if (healPlayer()) 
		{
			plugin.getLanguageManager().setPlayerName(player.getName());
			plugin.getGodManager().GodSay(this.godName, this.player, languageString, 2 + random.nextInt(10));

			plugin.log(godName + " healed " + player.getName());
		}
	}
}
