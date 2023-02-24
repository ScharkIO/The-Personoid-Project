package com.personoid.api.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.personoid.api.utils.cache.Cache;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class Skin implements Serializable {

    private static final String SKIN_TEXTURE_STEVE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMTY1NjI4MTU2NywKICAicHJvZmlsZUlkIiA6ICJjMDZmODkwNjRjOGE0OTExOWMyOWVhMWRiZDFhYWI4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU3RldmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZjg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiIKICAgIH0KICB9Cn0=";
    private static final String SKIN_SIGNATURE_STEVE = "fHqXH1hHnbobsNWYLx+jkJKd8OV6RrLMMHNTrJv/z2L9YQlFJ9Ki7Gq/REaogdgL7vy70QlxF1dezy9Cdnu/Dw6oh38DZuu4hCOJCJKsO5neEW7Cgsbkq/Yfc+jd2k5w/JLbZeJeaLF7SgTGfnISfpkj7TSSAXmi2g5pD10HTeX53GUEVbhFw51Guf7nZyZbWVJaPPF01MstyIbqQ8RZgotwJaYkrc90aWZF7iClVG8hI2c7tt3MMCUpjfFWfuFBno/OzBsCOelo7LdQ6yaR0xMtgRqtiKXWZxkECKjHCLKFIMyRH5P00WsgEv7nJ5biwbJCTN09KmgJre2FX0lHrXflV0AeNqT05foLv0+7cGktoy2Mm00sva5fg300F0CllYnsCS+WxbjtTkOOqv4wEFLrcxd/N6Jhwr7ubKgPUXb5CoB9riSj9n82g6mVs+E3unLkiPL1BSt6Cf0Ts+BVUcRYvfXwLcwRBXtwswqYLgOopBWgRrZ/B5iGH2J3+NvJVVBOyNzKZXxmAu3KxxuzU3/10WdTHUhSSDMr+sZLkgIqIiCx3Fib1+LGKS1xJZr7rp3gNDm7GcIzct4sekVogRcuU8buUjBs0MnFzQA+w/xijUUDpqyPfzMiN7bzNkUM4LLS7YaGmC6NMLBTVTiOd7wAzmLmwf+hP2dKsk/hetg=";
    private static final String SKIN_TEXTURE_ALEX = "ewogICJ0aW1lc3RhbXAiIDogMTYyMTY1NjMwOTUyMSwKICAicHJvZmlsZUlkIiA6ICI2YWI0MzE3ODg5ZmQ0OTA1OTdmNjBmNjdkOWQ3NmZkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQWxleCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84M2NlZTVjYTZhZmNkYjE3MTI4NWFhMDBlODA0OWMyOTdiMmRiZWJhMGVmYjhmZjk3MGE1Njc3YTFiNjQ0MDMyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
    private static final String SKIN_SIGNATURE_ALEX = "ljxArZT9SaqOaB8qRoEax7nuCvex3Fvpg/ZAm4whTxZ3SI+6iwGSI8cfMxDy4Ugf58oMB2J3kWk7ZrK+vcyRgBIE30Rnb+vFHLGKP/xbipilebhwDc1xl9Gvv1kDRNysWDy4u3vWvPB+Yr6LJjyETU6XzJqBxpu1v+6WmZ80rZ5JJ/avLaWTygotWobrAVsHEiHAqinqZeDU4zEWutOyu+PG1i5mo5pu0I1U1o8ZIiMcjilEftKFYuLPrtJ4rENLdZQpkFrOHR5SGiYCRYl48ha3cGqeK1MfVJ+KdCIpYWkxUxr7dvOPHZPlfipdhsVYUbsoYx4+eSsD3KtJ59COvAenVscwY2/Qef4ssVWeZYyq/jLBe+uoORuqLqYx1xLHUGdOHsWqCkgeQEYKaJGZcJidEjBckWDrDuAvXDawBmWFIuHQXDMiy3HWUIhmDAf9eojfOTYVCNTuwt4RzTNnrAME2lkq8LeKUpNwHt1d/5SoJPQE8AjDr1URiLrBRgFUY39uicqyeDrYr8ogONAHlhFm+xOe3mfHK2oWE8l1dKGIeL5Kocpn9Rwyje75xP0SEdCXhrrHgUU9Krh56RB6WC6oyX/GoDBpBg/Q79aflruT13KFeDKBdlfELkgVT3kA2GxBceXyT/FVe4tGhU46Td7badi6NThI9Nbq82TdveM=";
    private static final Cache CACHE = new Cache("skins");
    private final String texture;
    private final String signature;

    static {
        CACHE.load("skins");
    }

    private Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
        cache();
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }

    public static Skin get(String name) {
        return CACHE.getOrPut(name, () -> {
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                InputStreamReader reader = new InputStreamReader(url.openStream());
                String uuid = JsonParser.parseReader(reader).getAsJsonObject().get("id").getAsString();
                return getFromUUID(uuid);
            } catch (IOException e) {
                throw new RuntimeException("Error ", e);
            }
        });
    }

    public static Skin get(UUID uuid) {
        return getFromUUID(uuid.toString());
    }

    private static Skin getFromUUID(String uuid) {
        return CACHE.getOrPut(uuid, () -> {
            try {
                URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                InputStreamReader reader2 = new InputStreamReader(url2.openStream());
                JsonObject property = JsonParser.parseReader(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
                String texture = property.get("value").getAsString();
                String signature = property.get("signature").getAsString();
                return new Skin(texture, signature);
            } catch (Exception e) {
                throw new RuntimeException("Error while getting skin from uuid", e);
            }
        });
    }

    public static Skin get(File file) {
        String fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (!fileType.equals("png")) throw new RuntimeException("Error while grabbing file: type must be .png");
        try {
            return get(ImageIO.read(file));
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file as image", e);
        }
    }

    public static Skin get(BufferedImage image) {
        final byte[] buffer;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", stream);
            stream.flush();
            buffer = stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing PNG data", e);
        }
        return CACHE.getOrPut(Arrays.toString(buffer), () -> {
            try {
                CloseableHttpClient client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost("https://api.mineskin.org/generate/upload?visibility=1");
                post.setEntity(
                        MultipartEntityBuilder.create()
                                .addBinaryBody("file", buffer, ContentType.IMAGE_PNG, "skin.png")
                                .build()
                );
                JSONObject json = (JSONObject) new JSONParser().parse(EntityUtils.toString(client.execute(post).getEntity()));
                client.close();
                JSONObject texture = (JSONObject) ((JSONObject) json.get("data")).get("texture");
                String value = (String) texture.get("value");
                String signature = (String) texture.get("signature");
                return new Skin(value, signature);
            } catch (IOException | NullPointerException | org.json.simple.parser.ParseException e) {
                throw new RuntimeException("Error while parsing JSON response", e);
            }
        });
    }

    public static Skin get(URL url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(30000);
            DataOutputStream stream = new DataOutputStream(conn.getOutputStream());
            stream.writeBytes("url=" + URLEncoder.encode(url.toString(), "UTF-8"));
            stream.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONObject json = (JSONObject) new JSONParser().parse(reader);
            Bukkit.broadcastMessage(json.toJSONString());
            JSONObject texture = (JSONObject) ((JSONObject) json.get("data")).get("texture");
            String value = (String) texture.get("value");
            String signature = (String) texture.get("signature");
            return new Skin(value, signature);
        } catch (ParseException | IOException | NullPointerException e) {
            throw new RuntimeException("Error while parsing JSON response", e);
        }
    }

    public static Skin randomDefault() {
        return new Random().nextBoolean() ? steve() : alex();
    }

    public static Skin steve() {
        return new Skin(SKIN_TEXTURE_STEVE, SKIN_SIGNATURE_STEVE);
    }

    public static Skin alex() {
        return new Skin(SKIN_TEXTURE_ALEX, SKIN_SIGNATURE_ALEX);
    }

    public static void cache() {
        CACHE.save("skins");
    }
}
