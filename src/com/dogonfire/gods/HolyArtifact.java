package com.dogonfire.gods;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_4_R1.ItemStack;
import net.minecraft.server.v1_4_R1.NBTTagCompound;
import net.minecraft.server.v1_4_R1.NBTTagList;
import net.minecraft.server.v1_4_R1.NBTTagString;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;

public class HolyArtifact 
{
	private final net.minecraft.server.v1_4_R1.ItemStack s;

	public boolean isNewItemRank(int oldKills) 
	{
		if (oldKills == 5)
			return true;
		if (oldKills == 20)
			return true;
		if (oldKills == 50)
			return true;
		if (oldKills == 100)
			return true;
		if (oldKills == 150)
			return true;
		if (oldKills == 200)
			return true;
		if (oldKills == 300)
			return true;
		if (oldKills == 500)
			return true;
		if (oldKills == 1000) 
		{
			return true;
		}
		return false;
	}

	public String getItemRankName(int kills) 
	{
		if (kills < 5)
			return "";
		if (kills < 20)
			return ChatColor.GREEN + "Shiny";
		if (kills < 50)
			return ChatColor.GREEN + "Honorable";
		if (kills < 100)
			return ChatColor.GREEN + "Holy";
		if (kills < 150)
			return ChatColor.GREEN + "Saintly";
		if (kills < 200)
			return ChatColor.BLUE + "Amazing";
		if (kills < 300)
			return ChatColor.BLUE + "Mega";
		if (kills < 500)
			return ChatColor.GREEN + "Awesome";
		if (kills < 1000) 
		{
			return ChatColor.DARK_PURPLE + "Epic";
		}
		return ChatColor.GOLD + "Legendary";
	}

	public String generateName(Material itemType, String godName) 
	{
		String name = "Holy Artifact of " + godName;

		switch (itemType) 
		{
		case REDSTONE_TORCH_ON: name = godName + "'s Magic Stick"; break;
		case SKULL_ITEM: name = "Skull of " + godName; break;
		case RED_ROSE: name = "Red Rose of " + godName; break;
		case SPONGE: name = godName + "'s Shower sponge"; break;
		case LONG_GRASS: name = godName + "'s Weed"; break;
		case JACK_O_LANTERN: name = godName + "'s Lantern"; break;
		case BUCKET: name = godName + "'s Paperbasket"; break;
		case PAPER: name = godName + "'s Cinematicket"; break;
		case BONE: name = "Bone of " + godName; break;
		case TORCH: name = "Matchstick of " + godName; break;
		case SHEARS: name = godName + "'s nailcutter"; break;
		case WATCH: name = "Bedside Clock of " + godName; break;
		case FISHING_ROD: name = "Fishing pole of " + godName; break;
		case GOLD_BOOTS: name = godName + "'s boots"; break;
		case RECORD_5:
		case RECORD_6:
		case RECORD_7:
		case RECORD_8:
		case RECORD_9: name = godName + "'s favorite song"; break;
		case GOLD_SPADE:
			name = godName + "'s gardening shovel";
			break;
		case GOLD_SWORD:
			name = "Butterknife of " + godName;
			break;
		case STICK:
			name = godName + "'s toothpick";
			break;
		}
		
		return name;
	}

	public HolyArtifact(org.bukkit.inventory.ItemStack itemStack, Material itemType, String godName) 
	{
		//this.s = itemstack.getHandle();
		this.s = CraftItemStack.asNMSCopy(itemStack);		
	}
	
	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack setHolyArtifactName(Material item, String godName) 
	{
		setName(generateName(item, godName));
		
		return CraftItemStack.asCraftMirror(s);
	}

	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack enchant(org.bukkit.inventory.ItemStack item) 
	{
		Random random = new Random();

		switch (random.nextInt(9)) 
		{
			case 0: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1 + random.nextInt(5)); break;
			case 1: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_UNDEAD, 1 + random.nextInt(5)); break;
			case 2: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DIG_SPEED, 1 + random.nextInt(5)); break;
			case 3: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT, 1 + random.nextInt(5)); break;
			case 4: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1 + random.nextInt(5)); break;
			case 5: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK, 1 + random.nextInt(5)); break;
			case 6: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH,1 + random.nextInt(5)); break;
			case 7: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ARTHROPODS, 1 + random.nextInt(5)); break;
			case 8: item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS, 1 + random.nextInt(5)); break;
		}
		
		return CraftItemStack.asCraftMirror(s);
	}
/*
	public void addEnchantment(String name, int level) 
	{
		NBTTagList list = new NBTTagList();

		NBTTagCompound c = new NBTTagCompound(name);

		c.setName("TestEnchant");
		c.setInt(net.minecraft.server.Enchantment.KNOCKBACK.toString(), 5);

		list.add(c);

		list.setName("ench");

		this.s.tag.set("ench", list);
	}

	
	public void addExplosion(ItemStack item, Enchantment ench, short lvl) 
	{		
		NBTTagCompound tag = itemStack.tag;
		if (tag == null) 
		{
			tag = new NBTTagCompound();
			tag.set("StoredEnchantments", new NBTTagList());
			itemStack.tag = tag;
		}

		NBTTagList list = tag.getList("StoredEnchantments");
		NBTTagCompound tag1 = new NBTTagCompound();
		tag1.setShort("id", (short) ench.getId());
		tag1.setShort("lvl", lvl);
		list.add(tag1);
		tag.set("StoredEnchantments", list);
		return CraftItemStack.asCraftMirror(itemStack);
	}
*/

	
	
	public boolean hasTitle() 
	{
		return this.s.tag.hasKey("title");
	}

	public boolean hasAuthor() 
	{
		return this.s.tag.hasKey("author");
	}

	public boolean hasPages() 
	{
		return this.s.tag.hasKey("pages");
	}

	public String getTitle() 
	{
		return this.s.tag.getString("title");
	}

	public String getAuthor() 
	{
		return this.s.tag.getString("author");
	}

	public String[] getPages() 
	{
		NBTTagList list = (NBTTagList) this.s.tag.get("pages");
		String[] pages = new String[list.size()];

		for (int i = 0; i < list.size(); i++) 
		{
			pages[i] = ((NBTTagString) list.get(i)).data;
		}

		return pages;
	}

	public List<String> getListPages() 
	{
		NBTTagList list = (NBTTagList) this.s.tag.get("pages");

		List<String> pages = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) 
		{
			pages.add(((NBTTagString) list.get(i)).data);
		}

		return pages;
	}

	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack setTitle(String title) 
	{
		if (title.length() > 16) 
		{
			title = title.substring(0, 16);
		}

		this.s.tag.setString("title", title);

		return CraftItemStack.asCraftMirror(s);
	}

	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack setAuthor(String author) 
	{
		if (author.length() > 16) 
		{
			author = author.substring(0, 16);
		}

		this.s.tag.setString("author", author);
		
		return CraftItemStack.asCraftMirror(s);
	}

	public void setPages(List<String> pages) 
	{
		NBTTagList list = new NBTTagList();

		int size = pages.size();

		for (int i = 0; i < size; i++) 
		{
			String page = (String) pages.get(i);

			if (page.length() > 256) 
			{
				page = page.substring(0, 256);
			}

			if ((page != null) && (!page.equals("")) && (!page.isEmpty())) 
			{
				NBTTagString p = new NBTTagString(page);
				p.setName(page);
				p.data = page;
				list.add(p);
			}
		}

		list.setName("pages");

		this.s.tag.set("pages", list);
	}

	public ItemStack getItemStack() 
	{
		return this.s;
	}

	public boolean isHolyArtifact() 
	{
		if (this.s.tag == null) 
		{
			return false;
		}

		return this.s.tag.hasKey("HolyArtifact");
	}

	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack makeHolyArtifact() 
	{
		if (this.s.tag == null) 
		{
			this.s.tag = new NBTTagCompound();
		}

		this.s.tag.setBoolean("HolyArtifact", true);

		if (!s.tag.hasKey("display")) 
		{
			s.tag.setCompound("display", new NBTTagCompound());
		}

		if (s.tag == null) 
		{
			s.tag = new NBTTagCompound();

			makeHolyArtifact();
		}

		List<String> list = new ArrayList<String>();

		Random random = new Random();

		switch (random.nextInt(10)) 
		{
			case 0: list.add("Very holy. And it's even warm to touch."); break;
			case 1: list.add("Be careful. This was meant for Gods, not humans"); break;
			case 2: list.add("Very Holy, very powerful!"); break;
			case 3: list.add("A Holy Artifact from beyond this world."); break;
			case 4: list.add("It has a strange otherworldly glow to it..."); break;
			case 5: list.add("A strange item from beyond this world..."); break;
			case 6: list.add("This is clearly not from this world..."); break;
			case 7: list.add("It has a strange feeling of power..."); break;
			case 8: list.add("This was never meant to be held by humans..."); break;
			case 9: list.add("A sense of ancient power flows through it...");
		}

		list.add(ChatColor.WHITE + "Kills: " + 0);
		setLore(list);
		
		return CraftItemStack.asCraftMirror(s);
	}

	public boolean hasName() 
	{
		return this.s.tag.getCompound("display").hasKey("Name");
	}

	public void setName(String name) 
	{
		this.s.tag.getCompound("display").setString("Name", name);
	}

	public String getName() 
	{
		return this.s.tag.getCompound("display").getString("Name");
	}

	public boolean hasKills() 
	{
		return this.s.tag.hasKey("PlayerKills");
	}

	public int getKills() 
	{
		return this.s.tag.getInt("PlayerKills");
	}

	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack setKills(int kills) 
	{
		this.s.tag.setInt("PlayerKills", kills);

		List<String> lore = getLore();

		lore.set(1, ChatColor.WHITE + "Kills: " + kills);

		setLore(lore);
		
		return CraftItemStack.asCraftMirror(s);
	}

	public List<String> getLore()
	{
		NBTTagList rawLore = this.s.tag.getCompound("display").getList("Lore");
		List<String> lore = new ArrayList<String>();

		for (int x = 0; x < rawLore.size(); x++) 
		{
			lore.add(((NBTTagString) rawLore.get(x)).data);
		}

		return lore;
	}

	public org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack setLore(List<String> loreList)
	{
		NBTTagList list = new NBTTagList();

		for (String lore : loreList) 
		{
			NBTTagString st = new NBTTagString(lore);
			st.data = lore;
			list.add(st);
		}

		this.s.tag.getCompound("display").set("Lore", list);
		
		return CraftItemStack.asCraftMirror(s);
	}
}