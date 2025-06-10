package me.plugin.goldtomoney;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class GoldToMoney extends JavaPlugin implements Listener {

    private static Economy econ = null;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault bulunamadı. Eklenti devre dışı!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("GoldToMoney GUI aktif!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("kese")) {
            openKeseGUI(player);
            return true;
        }
        return false;
    }

    private void openKeseGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§8Kese");

        ItemStack altinPara = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta1 = altinPara.getItemMeta();
        meta1.setDisplayName("§6Altını Paraya Çevir");
        meta1.setLore(Collections.singletonList("§7Her 1 altın = 5 para"));
        altinPara.setItemMeta(meta1);

        ItemStack paraAltin = new ItemStack(Material.EMERALD);
        ItemMeta meta2 = paraAltin.getItemMeta();
        meta2.setDisplayName("§aParayı Altına Çevir");
        meta2.setLore(Collections.singletonList("§75 para = 1 altın"));
        paraAltin.setItemMeta(meta2);

        gui.setItem(2, altinPara); // sol
        gui.setItem(6, paraAltin); // sağ

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (!e.getView().getTitle().equals("§8Kese")) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.GOLD_INGOT) {
            // ALTINI PARAYA ÇEVİR
            int goldCount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                    player.getInventory().remove(item);
                }
            }

            if (goldCount == 0) {
                player.sendMessage("§cHiç altın yok!");
                return;
            }

            double money = goldCount * 5.0;
            econ.depositPlayer(player, money);
            player.sendMessage("§a" + goldCount + " altın bozduruldu, +" + money + " para kazandın!");

        } else if (clicked.getType() == Material.EMERALD) {
            // PARAYI ALTINA ÇEVİR
            double balance = econ.getBalance(player);
            int possibleGold = (int) (balance / 5);

            if (possibleGold <= 0) {
                player.sendMessage("§cYeterli paran yok!");
                return;
            }

            econ.withdrawPlayer(player, possibleGold * 5);
            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, possibleGold));
            player.sendMessage("§a" + (possibleGold * 5) + " para karşılığında " + possibleGold + " altın aldın!");
        }
    }
}
