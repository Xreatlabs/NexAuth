/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.paper.protocol;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import org.bukkit.entity.Player;
import xyz.xreatlabs.nexauth.paper.PaperNexAuth;

import java.util.ArrayList;
import java.util.List;

import static xyz.xreatlabs.nexauth.common.config.ConfigurationKeys.HIDE_PLAYER_INVENTORY;

/**
 * Packet listener to hide player inventory for unauthenticated players
 * Only works on Paper/Spigot servers
 */
public class InventoryPacketListener extends PacketListenerAbstract {
    
    private final PaperNexAuth plugin;
    private final ItemStack emptyItem;
    private final InventoryHider inventoryHider;
    
    public InventoryPacketListener(PaperNexAuth plugin) {
        super(PacketListenerPriority.HIGH);
        this.plugin = plugin;
        this.emptyItem = ItemStack.builder().type(ItemTypes.AIR).amount(0).build();
        this.inventoryHider = new InventoryHider(plugin);
    }
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        
        // Check if inventory hiding is enabled
        if (!plugin.getConfiguration().get(HIDE_PLAYER_INVENTORY)) return;
        
        Player player = (Player) event.getPlayer();
        if (player == null) return;
        
        // Check if player is authenticated
        if (plugin.getAuthorizationProvider().isAuthorized(player)) return;
        
        // Block inventory-related incoming packets for unauthenticated players
        if (isInventoryInteractionPacket(event.getPacketType())) {
            event.setCancelled(true);
            plugin.getLogger().debug("Blocked inventory interaction packet for unauthenticated player: " + player.getName());
        }
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        
        // Check if inventory hiding is enabled
        if (!plugin.getConfiguration().get(HIDE_PLAYER_INVENTORY)) return;
        
        Player player = (Player) event.getPlayer();
        if (player == null) return;
        
        // Check if player is authenticated
        if (plugin.getAuthorizationProvider().isAuthorized(player)) return;
        
        // Handle outgoing inventory packets for unauthenticated players
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
            handleWindowItemsPacket(event);
        } else if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            handleSetSlotPacket(event);
        }
    }
    
    /**
     * Handle WINDOW_ITEMS packet - replace all items with empty items
     */
    private void handleWindowItemsPacket(PacketSendEvent event) {
        try {
            WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);
            
            // Get the original items list
            List<ItemStack> originalItems = windowItems.getItems();
            
            // Create empty items list with same size
            List<ItemStack> emptyItems = new ArrayList<>();
            for (int i = 0; i < originalItems.size(); i++) {
                emptyItems.add(emptyItem);
            }
            
            // Replace with empty items
            windowItems.setItems(emptyItems);
            
            plugin.getLogger().debug("Hid window items for unauthenticated player: " + ((Player) event.getPlayer()).getName());
            
        } catch (Exception e) {
            plugin.getLogger().warn("Failed to hide window items: " + e.getMessage());
        }
    }
    
    /**
     * Handle SET_SLOT packet - replace item with empty item
     */
    private void handleSetSlotPacket(PacketSendEvent event) {
        try {
            WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);
            
            // Only hide player inventory slots (window ID 0)
            if (setSlot.getWindowId() == 0) {
                setSlot.setItem(emptyItem);
                plugin.getLogger().debug("Hid inventory slot for unauthenticated player: " + ((Player) event.getPlayer()).getName());
            }
            
        } catch (Exception e) {
            plugin.getLogger().warn("Failed to hide inventory slot: " + e.getMessage());
        }
    }
    
    /**
     * Check if the packet is related to inventory interaction
     */
    private boolean isInventoryInteractionPacket(Object packetType) {
        return packetType == PacketType.Play.Client.CLICK_WINDOW ||
               packetType == PacketType.Play.Client.CLOSE_WINDOW ||
               packetType == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION ||
               packetType == PacketType.Play.Client.HELD_ITEM_CHANGE ||
               packetType == PacketType.Play.Client.PLAYER_DIGGING ||
               packetType == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT ||
               packetType == PacketType.Play.Client.USE_ITEM ||
               packetType == PacketType.Play.Client.INTERACT_ENTITY;
    }
    
    /**
     * Hide inventory by sending empty inventory packets
     */
    public void hideInventory(Player player) {
        if (!plugin.getConfiguration().get(HIDE_PLAYER_INVENTORY)) return;
        
        plugin.getLogger().debug("Hiding inventory for unauthenticated player: " + player.getName());
        
        // Schedule inventory hiding on next tick
        plugin.getBootstrap().getServer().getScheduler().runTask(plugin.getBootstrap(), () -> {
            try {
                // Send empty inventory to client
                inventoryHider.sendEmptyInventory(player);
                
                // Also clear individual slots to be thorough
                inventoryHider.clearAllInventorySlots(player);
                
            } catch (Exception e) {
                plugin.getLogger().warn("Failed to hide inventory for player " + player.getName() + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Reveal inventory by updating the player's inventory
     */
    public void revealInventory(Player player) {
        if (!plugin.getConfiguration().get(HIDE_PLAYER_INVENTORY)) return;
        
        plugin.getLogger().debug("Revealing inventory for authenticated player: " + player.getName());
        
        // Schedule inventory reveal on next tick
        plugin.getBootstrap().getServer().getScheduler().runTask(plugin.getBootstrap(), () -> {
            try {
                // Force client to refresh inventory with real items
                player.updateInventory();
                
            } catch (Exception e) {
                plugin.getLogger().warn("Failed to reveal inventory for player " + player.getName() + ": " + e.getMessage());
            }
        });
    }
}