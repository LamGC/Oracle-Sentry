package net.lamgc.oracle.sentry.oci.compute.ssh;

import com.google.common.base.Strings;
import com.google.gson.*;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver;
import org.apache.sshd.common.config.keys.PublicKeyEntryDecoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

/**
 * @author LamGC
 */
public final class SshAuthInfoSerializer implements JsonSerializer<SshAuthInfo>, JsonDeserializer<SshAuthInfo> {

    public final static SshAuthInfoSerializer INSTANCE = new SshAuthInfoSerializer();

    @Override
    public SshAuthInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("It should be a JsonObject");
        }
        JsonObject infoObject = json.getAsJsonObject();
        String type = getFieldToStringOrFail(infoObject, "authType");
        SshAuthInfo.AuthType authType = SshAuthInfo.AuthType.valueOf(type.toUpperCase());
        SshAuthInfo info;
        if (authType == SshAuthInfo.AuthType.PASSWORD) {
            PasswordAuthInfo pswAuthInfo = new PasswordAuthInfo();
            pswAuthInfo.setPassword(getFieldToStringOrFail(infoObject, "password"));
            info = pswAuthInfo;
        } else if (authType == SshAuthInfo.AuthType.PUBLIC_KEY) {
            PublicKeyAuthInfo publicKeyInfo = new PublicKeyAuthInfo();
            String privateKeyPath = getFieldToStringOrFail(infoObject, "privateKeyPath");
            File privateKeyFile = new File(privateKeyPath);
            publicKeyInfo.setPrivateKeyPath(privateKeyFile);
            info = publicKeyInfo;
        } else {
            throw new JsonParseException("Unsupported authentication type: " + authType);
        }
        info.setUsername(getFieldToStringOrFail(infoObject, "username"));
        try {
            info.setServerKey(decodeSshPublicKey(
                    infoObject.has("serverKey") && infoObject.get("serverKey").isJsonPrimitive() ?
                            infoObject.get("serverKey").getAsString() :
                            null));
        } catch (GeneralSecurityException | IOException e) {
            throw new JsonParseException(e);
        }
        return info;
    }

    @Override
    public JsonElement serialize(SshAuthInfo src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("authType", src.getType().toString());
        json.addProperty("username", src.getUsername());
        try {
            json.addProperty("serverKey", encodeSshPublicKey(src.getServerKey()));
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
        if (src instanceof PasswordAuthInfo info) {
            json.addProperty("password", info.getPassword());
        } else if (src instanceof PublicKeyAuthInfo info) {
            try {
                json.addProperty("privateKeyPath", info.getPrivateKeyPath().getCanonicalPath());
            } catch (IOException e) {
                throw new JsonParseException(e);
            }
            json.addProperty("keyPassword", info.getKeyPassword());
        } else {
            throw new JsonParseException("Unsupported type");
        }
        return json;
    }

    private String getFieldToStringOrFail(JsonObject object, String field) {
        if (!object.has(field)) {
            throw new JsonParseException("Missing field: " + field);
        }
        return object.get(field).getAsString();
    }

    private PublicKey decodeSshPublicKey(String publicKeyString) throws GeneralSecurityException, IOException {
        if (Strings.isNullOrEmpty(publicKeyString)) {
            return null;
        }

        String[] strings = publicKeyString.split(" ", 3);

        @SuppressWarnings("unchecked") PublicKeyEntryDecoder<PublicKey, ?> decoder =
                (PublicKeyEntryDecoder<PublicKey, ?>) KeyUtils.getPublicKeyEntryDecoder(strings[0]);
        return decoder.decodePublicKey(null, strings[0], Base64.getDecoder().decode(strings[1]), Collections.emptyMap());
    }

    private String encodeSshPublicKey(PublicKey key) throws IOException {
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        PublicKeyEntry.appendPublicKeyEntry(builder, key);
        return builder.toString();
        /*
        // 以下代码改写自 KnownHosts 的那个认证器, 说实话翻一下官方代码还可以找到不错的东西.
        @SuppressWarnings("unchecked") PublicKeyEntryDecoder<PublicKey, ?> decoder
                = (PublicKeyEntryDecoder<PublicKey, ?>) KeyUtils.getPublicKeyEntryDecoder(key);
        if (decoder == null) {
            throw new JsonParseException("Cannot retrieve decoder for key=" + key.getAlgorithm());
        }

        try (ByteArrayOutputStream s = new ByteArrayOutputStream(Byte.MAX_VALUE)) {
            String keyType = decoder.encodePublicKey(s, key);
            byte[] bytes = s.toByteArray();
            PublicKeyEntryDataResolver encoder = PublicKeyEntry.resolveKeyDataEntryResolver(keyType);
            return encoder.encodeEntryKeyData(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

}
