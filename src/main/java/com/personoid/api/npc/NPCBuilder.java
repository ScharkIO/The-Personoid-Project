package com.personoid.api.npc;

import com.personoid.api.utils.cache.Cache;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.ReflectionUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public class NPCBuilder {
    private static final Cache CACHE = new Cache("npc_builder");
    private static DynamicType.Builder<?> builder;

    static {
        CACHE.put("entity_player", ReflectionUtils.findClass(Packages.SERVER_LEVEL, "EntityPlayer"));
        CACHE.put("minecraft_server", ReflectionUtils.findClass(Packages.SERVER, "MinecraftServer"));
        CACHE.put("world_server", ReflectionUtils.findClass(Packages.SERVER_LEVEL, "WorldServer"));
        CACHE.put("game_profile", ReflectionUtils.findClass(Packages.AUTH_LIB, "GameProfile"));
        if (ReflectionUtils.getVersionInt() >= 19 && ReflectionUtils.getSubVersionInt() <= 2) {
            CACHE.put("profile_public_key", ReflectionUtils.findClass(Packages.WORLD.plus("entity.player"), "ProfilePublicKey"));
        }
    }

    public static NPC create(GameProfile profile) {
        //Mappings mappings = Mappings.get(MinecraftVersion.get());
        NPC npc = new NPC(profile);
        if (builder == null) {
            builder = new ByteBuddy().subclass(CACHE.getClass("entity_player"), ConstructorStrategy.Default.IMITATE_SUPER_CLASS_PUBLIC);
        }
        try {
            for (String method : npc.getOverrides().getMethods()) {
                builder = builder.method(ElementMatchers.isMethod()
                        .and(ElementMatchers.named(method))
                        .and(ElementMatchers.returns(TypeDescription.VOID))
                        .and(ElementMatchers.takesNoArguments())
                ).intercept(MethodCall.invokeSuper().andThen(MethodCall.invoke(NPCOverrides.class.getMethod(method)).on(npc.getOverrides())));
            }
            Class<?> loaded = builder.make().load(NPCBuilder.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
            Object gameProfile = ReflectionUtils.construct(CACHE.getClass("game_profile"), profile.getId(), profile.getName());
            Object base;
            if (ReflectionUtils.getVersionInt() >= 19 && ReflectionUtils.getSubVersionInt() <= 2) {
                base = loaded.getConstructor(
                        CACHE.getClass("minecraft_server"),
                        CACHE.getClass("world_server"),
                        CACHE.getClass("game_profile"),
                        CACHE.getClass("profile_public_key"))
                        .newInstance(
                                ReflectionUtils.invoke(Bukkit.getServer(), "getServer"),
                                ReflectionUtils.invoke(Bukkit.getWorlds().get(0), "getHandle"),
                                gameProfile, null
                        );
            } else {
                base = loaded.getConstructor(
                                CACHE.getClass("minecraft_server"),
                                CACHE.getClass("world_server"),
                                CACHE.getClass("game_profile"))
                        .newInstance(
                                ReflectionUtils.invoke(Bukkit.getServer(), "getServer"),
                                ReflectionUtils.invoke(Bukkit.getWorlds().get(0), "getHandle"),
                                gameProfile
                        );
            }
            npc.getOverrides().setBase(base);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return npc;
    }
}
