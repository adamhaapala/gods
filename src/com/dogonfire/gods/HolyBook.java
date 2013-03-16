package com.dogonfire.gods;

import java.util.ArrayList;
import java.util.List;

//import net.minecraft..ItemStack;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.BookMeta;

public class HolyBook 
{
	private final ItemStack s;

	public HolyBook(org.bukkit.inventory.ItemStack itemStack) throws Exception 
	{
		if (itemStack.getType() != Material.WRITTEN_BOOK && itemStack.getType() != Material.BOOK_AND_QUILL) 
		{
			throw new Exception("HolyBook: CraftItemStack is not Material.WRITTEN_BOOK or Material.BOOK_AND_QUILL");
		}
		
		s = itemStack;

		/*
		this.s = CraftItemStack.asNMSCopy(itemStack);
		
		if (this.s.tag == null) 
		{
			this.s.tag = new NBTTagCompound();
		}
		*/
	}
	
	public org.bukkit.inventory.ItemStack getItem()
	{
		return s;//CraftItemStack.asCraftMirror(s);
	}

	public boolean hasTitle() 
	{		
		return s.getItemMeta().hasDisplayName();
		//ItemMeta itemMeta = item.getItemMeta();
		//return s.tag.hasKey("title");
	}

	public boolean hasAuthor() 
	{
		return ((BookMeta)s.getItemMeta()).hasAuthor();
		//return this.s.tag.hasKey("author");
	}

	public boolean hasPages() 
	{
		return ((BookMeta)s.getItemMeta()).hasPages();		
		//return this.s.tag.hasKey("pages");
	}

	public String getTitle() 
	{
		return ((BookMeta)s.getItemMeta()).getTitle();		
		//return this.s.tag.getString("title");
	}

	public String getAuthor() 
	{
		return ((BookMeta)s.getItemMeta()).getAuthor();		
		//return this.s.tag.getString("author");
	}

	public List<String> getPages() 
	{
		return ((BookMeta)s.getItemMeta()).getPages();		
/*
		NBTTagList list = (NBTTagList) this.s.tag.get("pages");
		String[] pages = new String[list.size()];

		for (int i = 0; i < list.size(); i++) 
		{
			pages[i] = ((NBTTagString) list.get(i)).data;
		}

		return pages;
		*/
	}

	/*
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
	*/
	public void setTitle(String name) 
	{
		 ItemMeta meta = s.getItemMeta();
         BookMeta bookMeta = (BookMeta)meta;
         bookMeta.setTitle(ChatColor.GOLD + name);
         s.setItemMeta(bookMeta);
	}

	public void setAuthor(String author) 
	{
		 ItemMeta meta = s.getItemMeta();
         BookMeta bookMeta = (BookMeta)meta;
         bookMeta.setAuthor(ChatColor.GOLD + author);
         s.setItemMeta(bookMeta);
	}
/*
	public void setPages(String[] pages) 
	{
		NBTTagList list = new NBTTagList();

		int size = pages.length;

		for (int i = 0; i < size; i++) 
		{
			String page = pages[i];

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
*/
	public void setPages(List<String> pages) 
	{
		 ItemMeta meta = s.getItemMeta();
         BookMeta bookMeta = (BookMeta)meta;
         bookMeta.setPages(pages);
         s.setItemMeta(bookMeta);
		
		/*
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
		*/
	}


	public ItemStack getItemStack() 
	{
		return this.s;
	}
}