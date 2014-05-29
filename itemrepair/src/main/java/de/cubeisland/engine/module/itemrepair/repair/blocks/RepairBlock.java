/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.module.itemrepair.repair.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.cubeisland.engine.core.module.service.Economy;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.module.itemrepair.Itemrepair;
import de.cubeisland.engine.module.itemrepair.material.BaseMaterial;
import de.cubeisland.engine.module.itemrepair.material.BaseMaterialContainer;
import de.cubeisland.engine.module.itemrepair.material.RepairItem;
import de.cubeisland.engine.module.itemrepair.material.RepairItemContainer;
import de.cubeisland.engine.module.itemrepair.repair.RepairBlockManager;
import de.cubeisland.engine.module.itemrepair.repair.RepairRequest;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static org.bukkit.Effect.GHAST_SHRIEK;
import static org.bukkit.Sound.ANVIL_BREAK;
import static org.bukkit.Sound.BURP;

public class RepairBlock
{
    private final BaseMaterialContainer priceProvider;
    protected final RepairItemContainer itemProvider;
    private final RepairBlockManager repairBlockManager;
    private final Permission permission;

    private final Itemrepair module;

    private final Map<String, RepairBlockInventory> inventoryMap;

    private final RepairBlockConfig config;

    private final Random rand;
    private final String name;

    private final Economy economy;

    public RepairBlock(Itemrepair module, RepairBlockManager manager, String name, RepairBlockConfig config)
    {
        this.module = module;
        this.name = name;
        this.repairBlockManager = manager;
        this.itemProvider = repairBlockManager.getItemProvider();
        this.priceProvider = itemProvider.getPriceProvider();
        this.permission = this.module.getBasePermission().childWildcard("block").child(name);
        this.inventoryMap = new HashMap<>();
        this.rand = new Random(System.currentTimeMillis());
        this.config = config;
        this.economy = module.getCore().getModuleManager().getServiceManager().getServiceImplementation(Economy.class);
    }

    public final String getName()
    {
        return this.name;
    }

    public final String getTitle()
    {
        return this.config.title;
    }

    public final Permission getPermission()
    {
        return this.permission;
    }

    public final Material getMaterial()
    {
        return this.config.block;
    }

    public double calculatePrice(Iterable<ItemStack> items)
    {
        return this.calculatePrice(items, this.module.getConfig().price.enchantMultiplier.factor,
           this.module.getConfig().price.enchantMultiplier.base, this.config.costPercentage);
    }

    private double calculatePrice(Iterable<ItemStack> items, double enchantmentFactor, double enchantmentBase, float percentage)
    {
        double price = 0.0;

        Material type;
        RepairItem item;
        double currentPrice;
        for (ItemStack itemStack : items)
        {
            type = itemStack.getType();
            item = itemProvider.of(type);
            currentPrice = 0;
            for (Entry<BaseMaterial, Integer> entry : item.getBaseMaterials().entrySet())
            {
                currentPrice += entry.getKey().getPrice() * entry.getValue();
            }
            currentPrice *= (double)Math.min(itemStack.getDurability(), type.getMaxDurability()) / (double)type.getMaxDurability();
            currentPrice *= getEnchantmentMultiplier(itemStack, enchantmentFactor, enchantmentBase);

            price += currentPrice;
        }
        price *= percentage/100;
        return price;
    }

    public RepairBlockInventory removeInventory(final Player player)
    {
        return this.inventoryMap.remove(player.getName());
    }

    public RepairBlockInventory getInventory(final Player player)
    {
        if (player == null)
        {
            return null;
        }
        RepairBlockInventory inventory = this.inventoryMap.get(player.getName());
        if (inventory == null)
        {
            inventory = new RepairBlockInventory(Bukkit.createInventory(player, 9 * 4, this.config.title), player);
            this.inventoryMap.put(player.getName(), inventory);
        }
        return inventory;
    }

    public class RepairBlockInventory
    {
        public final Inventory inventory;
        public final Player player;

        public RepairBlockInventory(Inventory inventory, Player player)
        {
            this.inventory = inventory;
            this.player = player;
        }
    }

    public boolean withdrawPlayer(User user, double price)
    {
        economy.createAccount(user.getUniqueId()); // Make sure account exists
        if (economy.has(user.getUniqueId(), price) && economy.withdraw(user.getUniqueId(), price))
        {
            // TODO bankAccounts
            /*
            String account = this.plugin.getServerBank();
            if (eco.hasBankSupport() && !("".equals(account)))
            {
                eco.bankDeposit(account, amount);
            }
            else
            {
                account = this.plugin.getServerPlayer();
                if (!("".equals(account)) && eco.hasAccount(account))
                {
                    eco.depositPlayer(account, amount);
                }
            }
            */
            return true;
        }
        return false;

    }

    public RepairRequest requestRepair(RepairBlockInventory inventory)
    {
        User user = this.module.getCore().getUserManager().getExactUser(inventory.player.getUniqueId());
        Map<Integer, ItemStack> items = this.itemProvider.getRepairableItems(inventory.inventory);
        if (items.size() > 0)
        {
            Double price = calculatePrice(items.values());
            String format = economy.format(price);
            if (this.config.breakPercentage > 0)
            {
                user.sendTranslated(NEGATIVE, "Items will break with a chance of {decimal:2}%", this.config.breakPercentage);
            }
            if (this.config.failPercentage > 0)
            {
                user.sendTranslated(NEGATIVE, "Items will not repair with a chance of {decimal:2}%", this.config.failPercentage);
            }
            if (this.config.looseEnchantmentsPercentage > 0)
            {
                user.sendTranslated(NEGATIVE, "Items will loose all enchantments with a chance of {decimal:2}%", this.config.looseEnchantmentsPercentage);
            }
            if (this.config.costPercentage > 100)
            {
                user.sendTranslated(NEUTRAL, "The repair would cost {input#amount} (+{decimal:2}%)", format, this.config.costPercentage - 100);
            }
            else if (this.config.costPercentage < 100)
            {
               user.sendTranslated(NEUTRAL, "The repair would cost {input#amount} (-{decimal:2}%)", format, 100 - this.config.costPercentage);
            }
            else
            {
                user.sendTranslated(NEUTRAL, "The repair would cost {input#amount}", format);
            }
            economy.createAccount(user.getUniqueId());
            user.sendTranslated(NEUTRAL, "You currently have {input#balance}", economy.format(user.getLocale(), economy.getBalance(user.getUniqueId())));
            user.sendTranslated(POSITIVE, "{text:Leftclick} again to repair all your damaged items.");
            return new RepairRequest(this, inventory, items, price);
        }
        else
        {
            user.sendTranslated(NEGATIVE, "There are no items to repair!");
        }
        return null;
    }

    public void repair(RepairRequest request)
    {
        double price = request.getPrice();
        RepairBlockInventory inventory = request.getInventory();
        User user = this.module.getCore().getUserManager().getExactUser(inventory.player.getUniqueId());
        if (withdrawPlayer(user, price))
        {
            boolean itemsBroken = false;
            boolean repairFail = false;
            boolean looseEnch = false;
            ItemStack item;
            int amount;
            for (Map.Entry<Integer, ItemStack> entry : request.getItems().entrySet())
            {
                item = entry.getValue();
                if (this.rand.nextInt(100) >= this.config.breakPercentage)
                {
                    if (this.rand.nextInt(100) >= this.config.failPercentage)
                    {
                        repairItem(entry.getValue());
                    }
                    else
                    {
                        repairFail = true;
                    }
                    if (!entry.getValue().getEnchantments().isEmpty())
                    {
                        if (this.rand.nextInt(100) < this.config.looseEnchantmentsPercentage)
                        {
                            looseEnch = true;
                            for (Enchantment enchantment : entry.getValue().getEnchantments().keySet())
                            {
                                entry.getValue().removeEnchantment(enchantment);
                            }
                        }
                    }
                }
                else
                {
                    itemsBroken = true;
                    amount = item.getAmount();
                    if (amount == 1)
                    {
                        inventory.inventory.clear(entry.getKey());
                    }
                    else
                    {
                        item.setAmount(amount - 1);
                        repairItem(item);
                    }
                }
            }
            if (itemsBroken)
            {
                user.sendTranslated(NEGATIVE, "You broke some of your items when repairing!");
                user.playSound(user.getLocation(), ANVIL_BREAK,1,0);
            }
            if (repairFail)
            {
                user.sendTranslated(NEGATIVE, "You failed to repair some of your items!");
                user.playSound(user.getLocation(), BURP,1,0);
            }
            if (looseEnch)
            {
                user.sendTranslated(NEGATIVE, "Oh no! Some of your items lost their magical power.");
                user.playEffect(user.getLocation(), GHAST_SHRIEK, 0);
            }
            user.sendTranslated(POSITIVE, "You paid {input#amount} to repair your items!", economy.format(price));
            if (this.config.costPercentage > 100)
            {
                user.sendTranslated(POSITIVE, "Thats {decimal#percent:2}% of the normal price!", this.config.costPercentage);
            }
            else if (this.config.costPercentage < 100)
            {
                user.sendTranslated(POSITIVE, "Thats {decimal#percent:2}% less then the normal price!", 100 - this.config.costPercentage);
            }
        }
        else
        {
           user.sendTranslated(NEGATIVE, "You don't have enough money to repair these items!");
        }
    }

    /*
     * Utilities
     */

    public static double getEnchantmentMultiplier(ItemStack item, double factor, double base)
    {
        double enchantmentLevel = 0;
        for (Integer level : item.getEnchantments().values())
        {
            enchantmentLevel += level;
        }

        if (enchantmentLevel > 0)
        {
            double enchantmentMultiplier = factor * Math.pow(base, enchantmentLevel);

            enchantmentMultiplier = enchantmentMultiplier / 100.0 + 1.0;

            return enchantmentMultiplier;
        }
        else
        {
            return 1.0;
        }
    }

    public static void repairItems(RepairRequest request)
    {
        repairItems(request.getItems().values());
    }

    public static void repairItems(Iterable<ItemStack> items)
    {
        repairItems(items, (short)0);
    }

    public static void repairItems(Iterable<ItemStack> items, short durability)
    {
        for (ItemStack item : items)
        {
            repairItem(item, durability);
        }
    }

    public static void repairItem(ItemStack item)
    {
        repairItem(item, (short)0);
    }

    public static void repairItem(ItemStack item, short durability)
    {
        if (item != null)
        {
            item.setDurability(durability);
        }
    }

    public static void removeHeldItem(Player player)
    {
        PlayerInventory inventory = player.getInventory();
        inventory.clear(inventory.getHeldItemSlot());
    }
}