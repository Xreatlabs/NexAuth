/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.paper.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import org.bukkit.entity.Player;
import xyz.xreatlabs.nexauth.paper.PaperNexAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to hide player inventory by sending empty inventory packets
 */
public class InventoryHider {
    
    private final PaperNexAuth plugin;
    private final ItemStack emptyItem;
    
    public InventoryHider(PaperNexAuth plugin) {
        this.plugin = plugin;
        this.emptyItem = ItemStack.builder().type(ItemTypes.AIR).amount(0).build();
    }
    
    /**
     * Send empty inventory to player
     */
    public void sendEmptyInventory(Player player) {
        try {
            // Create empty items for player inventory (46 slots: 36 inventory + 4 armor + 1 offhand + 5 crafting)
            List<ItemStack> emptyItems = new ArrayList<>();
            for (int i = 0; i < 46; i++) {
                emptyItems.add(emptyItem);
            }
            
            // Send empty inventory packet
            WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(
                0, // Player inventory window ID
                0, // State ID
                emptyItems,
                emptyItem // Carried item
            );
            
            // Send packet to player
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, windowItems);
            
            plugin.getLogger().debug("Sent empty inventory to player: " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().warn("Failed to send empty inventory to player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Send empty slot to player
     */
    public void sendEmptySlot(Player player, int slot) {
        try {
            WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(
                0, // Player inventory window ID
                0, // State ID
                slot, // Slot number
                emptyItem // Empty item
            );
            
            // Send packet to player
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, setSlot);
            
            plugin.getLogger().debug("Sent empty slot " + slot + " to player: " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().warn("Failed to send empty slot to player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Clear all player inventory slots
     */
    public void clearAllInventorySlots(Player player) {
        // Clear main inventory (slots 9-35)
        for (int i = 9; i <= 35; i++) {
            sendEmptySlot(player, i);
        }
        
        // Clear hotbar (slots 0-8)
        for (int i = 0; i <= 8; i++) {
            sendEmptySlot(player, i);
        }
        
        // Clear armor slots (slots 36-39)
        for (int i = 36; i <= 39; i++) {
            sendEmptySlot(player, i);
        }
        
        // Clear offhand slot (slot 40)
        sendEmptySlot(player, 40);
        
        plugin.getLogger().debug("Cleared all inventory slots for player: " + player.getName());
    }
}