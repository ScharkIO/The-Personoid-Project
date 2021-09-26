package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.utils.JavaUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NPCHandler {
    public static final NPCRegistry registry = CitizensAPI.getNPCRegistry();
    private static final Map<NPC, PersonoidNPC> NPCs = new HashMap<>();

    public static Map<NPC, PersonoidNPC> getNPCs() {
        return NPCs;
    }

    public static PersonoidNPC create(String name, Location loc) {
        return new PersonoidNPC(name).spawn(loc);
    }

    public static String getRandomName() {
        File nameFile = new File("names.txt");
        return JavaUtils.randomLineFromFile(nameFile);
    }
}
