package com.personoid.api.utils.packet;

import com.personoid.api.utils.CacheManager;
import com.personoid.api.utils.Parameter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReflectionUtils {
    private static final CacheManager CACHE = new CacheManager("reflection_utils");
    private static String version;

    public static Class<?> findClass(Packages packageType, String className) {
        return findClass(packageType.getPackageName(), className);
    }

    public static Class<?> findClass(String packageName, String className) {
        try {
            if (CACHE.contains(packageName + "." + className)) {
                return CACHE.getClass(packageName + "." + className);
            }
            Class<?> clazz = Class.forName(packageName + "." + className);
            CACHE.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getNMSClass(String packageName, String className) throws ClassNotFoundException {
        if (packageName == null) {
            return Class.forName("net.minecraft.server." + getVersion() + "." + className);
        } else {
            return Class.forName("net.minecraft.server." + getVersion() + "." + packageName + "." + className);
        }
    }

    public static Class<?> getNMSClassNoVer(String packageName, String className) throws ClassNotFoundException {
        if (packageName == null) {
            return Class.forName("net.minecraft.server." + className);
        } else {
            return Class.forName("net.minecraft.server." + packageName + "." + className);
        }
    }

    public static Class<?> getNMSClass(String oldPackageName, String newPackageName, String className, int minVerPackageChange) {
        try {
            if (Integer.parseInt(Objects.requireNonNull(getVersion()).split("_")[1]) >= minVerPackageChange) {
                if (newPackageName == null) {
                    return Class.forName("net.minecraft.server." + className);
                } else {
                    return Class.forName("net.minecraft.server." + newPackageName + "." + className);
                }
            } else {
                if (oldPackageName == null) {
                    return Class.forName("net.minecraft.server." + getVersion() + "." + className);
                } else {
                    return Class.forName("net.minecraft.server." + getVersion() + "." + oldPackageName + "." + className);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getCraftClass(String packageName, String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + packageName + "." + className);
    }

    public static Object invoke(Object obj, String methodName, Object... args) {
        try {
            return obj.getClass().getMethod(methodName).invoke(obj, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        throw new RuntimeException("Failed to invoke method for class " + obj.getClass().getName() + " ( " + methodName + ")");
    }

    public static Packet createPacket(String className, Parameter... parameters) throws ClassNotFoundException {
        Class<?> packetClass = null;
        for (String subPackage : getPacketSubPackages()) {
            try {
                packetClass = Class.forName(subPackage + "." + className);
            } catch (ClassNotFoundException ignored) {}
        }
        if (packetClass == null) {
            throw new ClassNotFoundException("Could not find packet " + className + " in sub packages");
        }
        try {
            Class<?>[] types = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                types[i] = parameters[i].getType();
            }
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                args[i] = parameters[i].getValue();
            }
            Object packetInstance = packetClass.getConstructor(types).newInstance(args);
            return new Packet() {
                @Override
                public void send(Player to) {
                    try {
                        Object craftPlayer = to.getClass().getMethod("getHandle").invoke(to);
                        Field connField = craftPlayer.getClass().getField("b");
                        connField.setAccessible(true);
                        Object connection = connField.get(craftPlayer);
                        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.Packet");
                        connection.getClass().getMethod("a", packetClass).invoke(connection, packetInstance);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
                        throw new RuntimeException("Could not get handle of player", e);
                    }
                }
            };
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create packet " + className, e);
        }
    }

    public static Object getEntityPlayer(Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get handle of player", e);
        }
    }

    public static Object getNMSEntity(Entity player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get handle of entity", e);
        }
    }

    public static Object getItemStack(ItemStack itemStack) {
        try {
            return itemStack.getClass().getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS item stack", e);
        }
    }

    public static Object getEquipmentSlot(EquipmentSlot slot) {
        try {
            return findClass(Packages.SERVER_WORLD, "EnumItemSlot").getMethod("valueOf", String.class).invoke(null, slot.name());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS equipment slot", e);
        }
    }

    public static Object getField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getEnum(Class<?> clazz, String enumName) {
        try {
            return clazz.getMethod("valueOf", String.class).invoke(null, enumName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get NMS enum", e);
        }
    }

    private static List<String> getPacketSubPackages() {
        return Arrays.asList(
                Packages.PACKETS.plus("game"),
                Packages.PACKETS.plus("handshake"),
                Packages.PACKETS.plus("login"),
                Packages.PACKETS.plus("status")
        );
    }

    public static String getVersion() {
        if (version != null) return version;
        try {
            return version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static int getVersionInt() {
        if (version != null) return Integer.parseInt(version.split("_")[1]);
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            return Integer.parseInt(version.split("_")[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
