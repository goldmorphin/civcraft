/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.config;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.LibraryEnchantment;
import com.avrgaming.civcraft.object.StoreMaterial;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Bank;
import com.avrgaming.civcraft.structure.FishHatchery;
import com.avrgaming.civcraft.structure.Grocer;
import com.avrgaming.civcraft.structure.Library;
import com.avrgaming.civcraft.structure.Quarry;
import com.avrgaming.civcraft.structure.Store;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.TradeShip;
import com.avrgaming.civcraft.structure.Trommel;

public class ConfigTownUpgrade {
	public String id;
	public String name;
	public double cost;
	public String action;
	public String require_upgrade = null;
	public String require_tech = null;
	public String require_structure = null;
	public String category = null;
	
	public static HashMap<String, Integer> categories = new HashMap<String, Integer>();
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigTownUpgrade> upgrades) {
		upgrades.clear();
		List<Map<?, ?>> culture_levels = cfg.getMapList("upgrades");
		for (Map<?, ?> level : culture_levels) {
			ConfigTownUpgrade town_upgrade = new ConfigTownUpgrade();
			
			town_upgrade.id = (String)level.get("id");
			town_upgrade.name = (String)level.get("name");
			town_upgrade.cost = (Double)level.get("cost");
			town_upgrade.action = (String)level.get("action");
			town_upgrade.require_upgrade = (String)level.get("require_upgrade");
			town_upgrade.require_tech = (String)level.get("require_tech");
			town_upgrade.require_structure = (String)level.get("require_structure");
			town_upgrade.category = (String)level.get("category");
		
			Integer categoryCount = categories.get(town_upgrade.category);
			if (categoryCount == null) {
				categories.put(town_upgrade.category.toLowerCase(), 1);
			} else {
				categories.put(town_upgrade.category.toLowerCase(), categoryCount+1);
			}
			
			upgrades.put(town_upgrade.id, town_upgrade);
		}
		CivLog.info("Loaded "+upgrades.size()+" town upgrades.");		
	}
	
	public void processAction(Town town) throws CivException {
		if (this.action == null) 
			return;
		String[] args = this.action.split(",");

		Structure struct;
		
		switch (args[0]) {
		case "set_town_level":
			if (town.getLevel() < Integer.valueOf(args[1].trim())) {
				town.setLevel(Integer.valueOf(args[1].trim()));
				CivMessage.global(CivSettings.localize.localizedString("var_townUpgrade_town",town.getName(),town.getLevelTitle()));
			}
			break;
		
		case "set_bank_level":
			struct = town.getStructureByType("s_bank");
			if (struct != null && (struct instanceof Bank)) {
				Bank bank = (Bank)struct;
				if (bank.getLevel() < Integer.valueOf(args[1].trim())) {
					bank.setLevel(Integer.valueOf(args[1].trim()));
					bank.updateSignText();
					town.saved_bank_level = bank.getLevel();
					CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townUpgrade_bank",bank.getLevel()));
				}
			}
			break;
		case "set_bank_interest":
			struct = town.getStructureByType("s_bank");
			if (struct != null && (struct instanceof Bank)) {
				Bank bank = (Bank)struct;
				if (bank.getInterestRate() < Double.valueOf(args[1].trim())) {
					bank.setInterestRate(Double.valueOf(args[1].trim()));
					town.saved_bank_interest_amount = bank.getInterestRate();
					DecimalFormat df = new DecimalFormat();
					CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_interest",df.format(bank.getInterestRate()*100)));
				}
			}
			break;
		case "set_store_level":
			struct = town.getStructureByType("s_store");
			if (struct != null && (struct instanceof Store)) {
				Store store = (Store)struct;
				if (store.getLevel() < Integer.valueOf(args[1].trim())) {
					store.setLevel(Integer.valueOf(args[1].trim()));
					store.updateSignText();
					CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_store",store.getLevel()));
				}
			}
			break;
		case "set_store_material":
			struct = town.getStructureByType("s_store");
			if (struct != null && (struct instanceof Store)) {
				Store store = (Store)struct;
				StoreMaterial mat = new StoreMaterial(args[1].trim(), args[2].trim(), args[3].trim(), args[4].trim());
				store.addStoreMaterial(mat);
				store.updateSignText();
			}
			break;
		case "set_library_level":
			struct = town.getStructureByType("s_library");
			if (struct != null && (struct instanceof Library)) {
				Library library = (Library)struct;
				if (library.getLevel() < Integer.valueOf(args[1].trim())) {
					library.setLevel(Integer.valueOf(args[1].trim()));
					library.updateSignText();
					CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_library",library.getLevel()));
				}
			}
			break;
		case "enable_library_enchantment":
			struct = town.getStructureByType("s_library");
			if (struct != null && (struct instanceof Library)) {
				Library library = (Library)struct;
				LibraryEnchantment enchant = new LibraryEnchantment(args[1].trim(), Integer.valueOf(args[2].trim()), Double.valueOf(args[3].trim()));
				library.addEnchant(enchant);
				library.updateSignText();
				CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_enchantment",args[1].trim()+" "+args[2]));
			}
			break;
		case "set_fish_hatchery_level":
			boolean didUpgradeFishery = false;
			int fisheryLevel = 1;
			for (Structure structure : town.getStructures()) {
				if (structure.getConfigId().equalsIgnoreCase("ti_fish_hatchery")) {

					if (structure != null && (structure instanceof FishHatchery)) {
						FishHatchery fishery = (FishHatchery)structure;
						if (fishery.getLevel() < Integer.valueOf(args[1].trim())) {
							didUpgradeFishery = true;
							fishery.setLevel(Integer.valueOf(args[1].trim()));
							fishery.updateSignText();
							town.saved_fish_hatchery_level = fishery.getLevel();
							fisheryLevel = fishery.getLevel();
						}
					}
				}
			}
			if (didUpgradeFishery)
			{
				CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_fish_hatchery",fisheryLevel));
			}
			break;
		case "set_tradeship_upgrade_level":
			boolean didUpgradeTradeShip = false;
			int tradeshipLevel = 1;
			for (Structure structure : town.getStructures()) {
				if (structure.getConfigId().equalsIgnoreCase("ti_trade_ship")) {

					if (structure != null && (structure instanceof TradeShip)) {
						TradeShip tradeShip = (TradeShip)structure;
						if (tradeShip.getUpgradeLvl() < Integer.valueOf(args[1].trim())) {
							didUpgradeTradeShip = true;
							tradeShip.setUpgradeLvl(Integer.valueOf(args[1].trim()));
							tradeShip.reprocessCommandSigns();
							town.saved_tradeship_upgrade_levels = tradeShip.getUpgradeLvl();
							tradeshipLevel = tradeShip.getUpgradeLvl();
						}
					}
				}
			}
			if (didUpgradeTradeShip)
			{
				CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_tradeship",tradeshipLevel));
			}
			break;
		case "set_grocer_level":
			struct = town.getStructureByType("s_grocer");
			if (struct != null && (struct instanceof Grocer)) {
				Grocer grocer = (Grocer)struct;
				if (grocer.getLevel() < Integer.valueOf(args[1].trim())) {
					grocer.setLevel(Integer.valueOf(args[1].trim()));
					grocer.updateSignText();
					CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_grocer",grocer.getLevel()));
				}
			}
			break;
		case "set_trommel_level":
			boolean didUpgrade = false;
			int trommelLevel = 1;
			for (Structure structure : town.getStructures()) {
				if (structure.getConfigId().equalsIgnoreCase("s_trommel")) {

					if (structure != null && (structure instanceof Trommel)) {
						Trommel trommel = (Trommel)structure;
						if (trommel.getLevel() < Integer.valueOf(args[1].trim())) {
							didUpgrade = true;
							trommel.setLevel(Integer.valueOf(args[1].trim()));
							town.saved_trommel_level = trommel.getLevel();
							trommelLevel = trommel.getLevel();
						}
					}
				}
			}
			if (didUpgrade)
			{
				CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_trommels",trommelLevel));
			}
			break;
		case "set_quarry_level":
			boolean didUpgradeQuarry = false;
			int quarryLevel = 1;
			for (Structure structure : town.getStructures()) {
				if (structure.getConfigId().equalsIgnoreCase("ti_quarry")) {

					if (structure != null && (structure instanceof Quarry)) {
						Quarry quarry = (Quarry)structure;
						if (quarry.getLevel() < Integer.valueOf(args[1].trim())) {
							didUpgradeQuarry = true;
							quarry.setLevel(Integer.valueOf(args[1].trim()));
							town.saved_quarry_level = quarry.getLevel();
							quarryLevel = quarry.getLevel();
						}
					}
				}
			}
			if (didUpgradeQuarry)
			{
				CivMessage.sendTown(town, CivSettings.localize.localizedString("var_townupgrade_quarries",quarryLevel));
			}
			break;
		}
	}

	public boolean isAvailable(Town town) {
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
		
		if (town.hasUpgrade(this.require_upgrade)) {
			if (town.getCiv().hasTechnology(this.require_tech)) {
				if (town.hasStructure(require_structure)) {
					if (!town.hasUpgrade(this.id)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static int getAvailableCategoryCount(String category, Town town) {
		int count = 0;
		
		for (ConfigTownUpgrade upgrade : CivSettings.townUpgrades.values()) {
			if (upgrade.category.equalsIgnoreCase(category) || category.equalsIgnoreCase("all")) {
				if (upgrade.isAvailable(town)) {
					count++;
				}
			}
		}
		
		return count;
	}
		
}
