package net.lamgc.oracle.sentry.oci.compute.ssh;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntryDecoder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @see SshAuthInfoSerializer
 */
class SshAuthInfoSerializerTest {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(SshAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .registerTypeAdapter(PasswordAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .registerTypeAdapter(PublicKeyAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .registerTypeAdapter(UnsupportedSshAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .serializeNulls()
            .create();

    private JsonObject getTestsInfo(String name) {
        InputStream resource = this.getClass().getResourceAsStream("/ssh-auth/" + name + ".json");
        if (resource == null) {
            throw new NoSuchElementException("Required resource not found: " + name);
        }
        return gson.fromJson(new InputStreamReader(resource, StandardCharsets.UTF_8), JsonObject.class);
    }

    @Test
    public void deserializePasswordTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("StandardPassword"), SshAuthInfo.class);

        assertTrue(info instanceof PasswordAuthInfo);
        assertEquals("opc", info.getUsername());
        assertEquals("123456", ((PasswordAuthInfo) info).getPassword());
        assertEquals("SHA256:qBu2jRXM6Wog/jWUJJ0WLTMb3UdDGAmYEVZQNZdFZNM", KeyUtils.getFingerPrint(info.getServerKey()));
    }

    @Test
    public void deserializePublicKeyTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("StandardPublicKey"), SshAuthInfo.class);
        assertEquals("SHA256:qBu2jRXM6Wog/jWUJJ0WLTMb3UdDGAmYEVZQNZdFZNM", KeyUtils.getFingerPrint(info.getServerKey()));
        assertEquals("opc", info.getUsername());
        if (info instanceof PublicKeyAuthInfo pkInfo) {
            assertEquals(new File("~/.ssh/id_rsa"), pkInfo.getPrivateKeyPath());
            assertEquals("123456", pkInfo.getKeyPassword());
        } else {
            fail("The type of the parsing result does not match: " + info.getClass());
        }
    }

    @Test
    public void deserializeBadPortNumberTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("BadPortValue-NonNumber"), SshAuthInfo.class);

        assertTrue(info instanceof PasswordAuthInfo);
        assertEquals("opc", info.getUsername());
        assertEquals("123456", ((PasswordAuthInfo) info).getPassword());
        assertEquals("SHA256:qBu2jRXM6Wog/jWUJJ0WLTMb3UdDGAmYEVZQNZdFZNM", KeyUtils.getFingerPrint(info.getServerKey()));
        assertEquals(22, info.getPort());
    }

    @Test
    public void deserializePortNumberOutOfBoundTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("BadPortValue-OutOfBound"), SshAuthInfo.class);

        assertTrue(info instanceof PasswordAuthInfo);
        assertEquals("opc", info.getUsername());
        assertEquals("123456", ((PasswordAuthInfo) info).getPassword());
        assertEquals("SHA256:qBu2jRXM6Wog/jWUJJ0WLTMb3UdDGAmYEVZQNZdFZNM", KeyUtils.getFingerPrint(info.getServerKey()));
        assertEquals(22, info.getPort());
    }

    @Test
    public void deserializePortNumberOutOfBoundMinusTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("BadPortValue-OutOfBound-minus"), SshAuthInfo.class);

        assertTrue(info instanceof PasswordAuthInfo);
        assertEquals("opc", info.getUsername());
        assertEquals("123456", ((PasswordAuthInfo) info).getPassword());
        assertEquals("SHA256:qBu2jRXM6Wog/jWUJJ0WLTMb3UdDGAmYEVZQNZdFZNM", KeyUtils.getFingerPrint(info.getServerKey()));
        assertEquals(22, info.getPort());
    }

    @Test
    public void deserializeUnsupportedTest() {
        assertThrows(JsonParseException.class, () ->
                gson.fromJson(getTestsInfo("UnsupportedAuthType"), SshAuthInfo.class));
    }

    @Test
    public void deserializeNoExistTypeTest() {
        assertThrows(JsonParseException.class, () ->
                gson.fromJson(getTestsInfo("NoExistType"), SshAuthInfo.class));
    }

    @Test
    public void deserializeBadServerKeyFieldTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("BadServerKeyField"), SshAuthInfo.class);
        assertNull(info.getServerKey());
    }

    @Test
    public void deserializeBadServerKeyDecodeTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("BadServerKey-decode"), SshAuthInfo.class);
        assertNull(info.getServerKey());
    }

    @Test
    public void deserializeNoExistServerKeyTest() {
        SshAuthInfo info = gson.fromJson(getTestsInfo("ServerKeyNoExist"), SshAuthInfo.class);
        assertNull(info.getServerKey());
    }

    @Test
    public void deserializeUnsupportedJsonTypeTest() {
        assertThrows(JsonParseException.class, () ->
                gson.fromJson(getTestsInfo("UnsupportedJsonType"), SshAuthInfo.class));
    }

    @Test
    public void deserializeBadRequiredFieldJsonTypeTest() {
        assertThrows(JsonParseException.class, () ->
                gson.fromJson(getTestsInfo("BadRequiredFieldType"), SshAuthInfo.class));
    }

    private void initialSshAuthInfo(SshAuthInfo info) {
        try {
            KeyPair pair = KeyUtils.generateKeyPair("ssh-rsa", 3072);
            info.setServerKey(pair.getPublic());
            info.setPort(new Random().nextInt(65536));
            info.setUsername("linux");
            if (info instanceof PasswordAuthInfo psw) {
                psw.setPassword(String.valueOf(new Random().nextLong()));
            } else if (info instanceof PublicKeyAuthInfo pk) {
                pk.setKeyPassword(String.valueOf(new Random().nextLong()));
                pk.setPrivateKeyPath(new File("./" + new Random().nextLong() + "/key"));
            }
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private String getOrFailField(JsonObject json, String field) {
        if (json.has(field) && json.get(field).isJsonPrimitive()) {
            return json.get(field).getAsString();
        } else {
            fail("The JSON field '" + field + "' does not exist or is not a primitive.");
            throw new RuntimeException();
        }
    }

    private PublicKey decodeSshPublicKey(String publicKeyString) throws GeneralSecurityException, IOException {
        String[] strings = publicKeyString.split(" ", 3);

        @SuppressWarnings("unchecked") PublicKeyEntryDecoder<PublicKey, ?> decoder =
                (PublicKeyEntryDecoder<PublicKey, ?>) KeyUtils.getPublicKeyEntryDecoder(strings[0]);
        return decoder.decodePublicKey(null, strings[0], Base64.getDecoder().decode(strings[1]), Collections.emptyMap());
    }

    @Test
    public void serializePasswordTest() throws GeneralSecurityException, IOException {
        PasswordAuthInfo info = new PasswordAuthInfo();
        initialSshAuthInfo(info);

        JsonObject json = gson.fromJson(gson.toJson(info), JsonObject.class);
        assertEquals(SshAuthInfo.AuthType.PASSWORD.name(), getOrFailField(json, "authType"));
        assertEquals(KeyUtils.getFingerPrint(info.getServerKey()),
                KeyUtils.getFingerPrint(decodeSshPublicKey(getOrFailField(json, "serverKey"))));
        assertEquals(info.getPort(), Integer.parseInt(getOrFailField(json, "port")));
        assertEquals(info.getUsername(), getOrFailField(json, "username"));

        assertEquals(info.getPassword(), getOrFailField(json, "password"));
    }

    @Test
    public void serializePublicKeyTest() throws GeneralSecurityException, IOException {
        PublicKeyAuthInfo info = new PublicKeyAuthInfo();
        initialSshAuthInfo(info);

        JsonObject json = gson.fromJson(gson.toJson(info), JsonObject.class);
        assertEquals(SshAuthInfo.AuthType.PUBLIC_KEY.name(), getOrFailField(json, "authType"));
        assertEquals(KeyUtils.getFingerPrint(info.getServerKey()),
                KeyUtils.getFingerPrint(decodeSshPublicKey(getOrFailField(json, "serverKey"))));
        assertEquals(info.getUsername(), getOrFailField(json, "username"));
        assertEquals(info.getPort(), Integer.parseInt(getOrFailField(json, "port")));

        assertEquals(info.getPrivateKeyPath().getCanonicalFile(), new File(getOrFailField(json, "privateKeyPath")));
        assertEquals(info.getKeyPassword(), getOrFailField(json, "keyPassword"));
    }

    @Test
    public void serializeNoExistServerKeyTest() {
        PasswordAuthInfo info = new PasswordAuthInfo();
        initialSshAuthInfo(info);

        info.setServerKey(null);

        JsonObject json = gson.fromJson(gson.toJson(info), JsonObject.class);
        assertEquals(SshAuthInfo.AuthType.PASSWORD.name(), getOrFailField(json, "authType"));
        assertTrue(json.get("serverKey").isJsonNull());
        assertEquals(info.getUsername(), getOrFailField(json, "username"));
        assertEquals(info.getPort(), Integer.parseInt(getOrFailField(json, "port")));

        assertEquals(info.getPassword(), getOrFailField(json, "password"));
    }

    @Test
    public void serializeUnsupportedTest() {
        assertThrows(JsonParseException.class, () ->
                gson.toJson(new UnsupportedSshAuthInfo(false)));
    }

    @Test
    public void serializeBadPrivateKeyPathTest() {
        PublicKeyAuthInfo info = new PublicKeyAuthInfo();
        initialSshAuthInfo(info);

        info.setPrivateKeyPath(new File("@#$*%&&96137:()*/key"));

        assertThrows(JsonParseException.class, () ->
                gson.toJson(info));
    }

    @Test
    public void serializeBadServerKeyTest() {
        PasswordAuthInfo info = new PasswordAuthInfo();
        initialSshAuthInfo(info);

        info.setServerKey(new BadPublicKey());

        JsonObject json = gson.fromJson(gson.toJson(info), JsonObject.class);
        assertEquals(SshAuthInfo.AuthType.PASSWORD.name(), getOrFailField(json, "authType"));
        assertTrue(json.get("serverKey").isJsonNull());
        assertEquals(info.getUsername(), getOrFailField(json, "username"));
        assertEquals(info.getPort(), Integer.parseInt(getOrFailField(json, "port")));

        assertEquals(info.getPassword(), getOrFailField(json, "password"));
    }

}