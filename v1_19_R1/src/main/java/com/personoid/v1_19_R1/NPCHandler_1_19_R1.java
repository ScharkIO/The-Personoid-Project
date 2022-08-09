package com.personoid.v1_19_R1;

import com.mojang.authlib.GameProfile;
import com.personoid.api.npc.NPC;
import com.personoid.api.npc.NPCHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCHandler_1_19_R1 implements NPCHandler {
    private static final List<NPC> npcs = new ArrayList<>();

    @Override
    public NPC createNPCInstance(World world, String name) {
        MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
        ServerLevel level = ((CraftWorld)world).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        NPC_1_19_R1 npc = new NPC_1_19_R1(server, level, profile);
        npc.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.CLIENTBOUND), npc) {
            @Override
            public void send(Packet<?> packet, @Nullable PacketSendListener packetsendlistener) { }
        };
        npcs.add(npc);
        return npc;
    }

    @Override
    public void spawnNPC(NPC npc, Location location) {
        NPC_1_19_R1 nmsNPC = (NPC_1_19_R1) npc;
        nmsNPC.setPos(location.getX(), location.getY(), location.getZ());
        new Packets_1_19_R1.AddPlayer(nmsNPC.getEntity()).send();
        nmsNPC.getLevel().addNewPlayer(nmsNPC);
    }

    @Override
    public void removeNPC(NPC npc) {
        despawnNPC(npc);
        npcs.remove(npc);
    }

    private void despawnNPC(NPC npc) {
        NPC_1_19_R1 nmsNPC = (NPC_1_19_R1) npc;
        new Packets_1_19_R1.RemovePlayer(nmsNPC.getEntity()).send();
        npc.remove();
        nmsNPC.remove(Entity.RemovalReason.DISCARDED);
    }

    public NPC getNPC(String name) {
        for (NPC npc : npcs) {
            NPC_1_19_R1 nmsNPC = (NPC_1_19_R1) npc;
            if (nmsNPC.getName().getString().equalsIgnoreCase(name.trim())) {
                return npc;
            }
        }
        return null;
    }

    public List<NPC> getNPCs() {
        return npcs;
    }

    public void purgeNPCs() {
        for (NPC npc : npcs) {
            despawnNPC(npc);
        }
        npcs.clear();
    }

    public boolean isNPC(org.bukkit.entity.Entity entity) {
        for (NPC npc : npcs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public NPC getNPC(org.bukkit.entity.Entity entity) {
        for (NPC npc : npcs) {
            if (npc.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return npc;
            }
        }
        return null;
    }
}
