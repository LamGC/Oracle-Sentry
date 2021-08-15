package net.lamgc.oracle.sentry.oci.compute.ssh;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SshAuthInfoSerializerTest {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(SshAuthInfo.class, SshAuthInfoSerializer.INSTANCE)
            .create();

    private JsonObject getPasswordAuthObject() {
        return gson.fromJson("""
                {
                    "username": "opc",
                    "authType": "password",
                    "serverKey": "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC/NGFFKkchNdE8HDE9WHGIcw97ZVOP5edY7drtRQn0xSSG6uLu08T36B8IWT+XJdg45/YMmcuVSzsG1QZs/R3s0URVUhsWjwdezWDeWeBHt8/6TGl2AsgA0iXSAOeRNldhZlITFvWoBEv2wElNjCTsEGo5bBp3rVPqqZNJFUs+FR9s/uVgmFqe7HGhuKhhk7BrRThJ/NcgDRicMQ4yXU3Hl++pG54TVLH+0HmgWg312XNAWtzw2iRmKBAuu2I4pP1TRp93K/lbD7QU8k8W7QcyGSAc73nZrhyzYVMko5wQGt4/vGpchOw7ehkotSejTB1GSyhzBTZobA23For76YLzuVFOjF3lEvSh1QV30ysu0PREKLtY83ad0WHVFqVgJrFHkkXQrglN335BhGwhFzwyMpRxbD8HCDtz6VjpqwoKtd/ExQkcfaj/g10o28vRzHGyzUbCTe433V61fjSsC4Bikw15vTnQ3ZuyOzfyoCYUNpFcf1Wv+mkoWqn9xU8lGvk= Test-Server",
                    "password": "123456"
                }
                """, JsonObject.class);
    }

    private JsonObject getPublicKeyAuthObject() {
        return gson.fromJson("""
                {
                    "username": "opc",
                    "authType": "Public_Key",
                    "serverKey": "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC/NGFFKkchNdE8HDE9WHGIcw97ZVOP5edY7drtRQn0xSSG6uLu08T36B8IWT+XJdg45/YMmcuVSzsG1QZs/R3s0URVUhsWjwdezWDeWeBHt8/6TGl2AsgA0iXSAOeRNldhZlITFvWoBEv2wElNjCTsEGo5bBp3rVPqqZNJFUs+FR9s/uVgmFqe7HGhuKhhk7BrRThJ/NcgDRicMQ4yXU3Hl++pG54TVLH+0HmgWg312XNAWtzw2iRmKBAuu2I4pP1TRp93K/lbD7QU8k8W7QcyGSAc73nZrhyzYVMko5wQGt4/vGpchOw7ehkotSejTB1GSyhzBTZobA23For76YLzuVFOjF3lEvSh1QV30ysu0PREKLtY83ad0WHVFqVgJrFHkkXQrglN335BhGwhFzwyMpRxbD8HCDtz6VjpqwoKtd/ExQkcfaj/g10o28vRzHGyzUbCTe433V61fjSsC4Bikw15vTnQ3ZuyOzfyoCYUNpFcf1Wv+mkoWqn9xU8lGvk= Test-Server",
                    "privateKeyPath": "",
                    "keyPassword": ""
                }
                """, JsonObject.class);
    }

    @Test
    public void deserializeTest() {
        SshAuthInfo info = gson.fromJson(getPasswordAuthObject(), SshAuthInfo.class);

        assertTrue(info instanceof PasswordAuthInfo);
        assertEquals("opc", info.getUsername());
        assertEquals("123456", ((PasswordAuthInfo) info).getPassword());
        assertEquals("SHA256:qBu2jRXM6Wog/jWUJJ0WLTMb3UdDGAmYEVZQNZdFZNM", KeyUtils.getFingerPrint(info.getServerKey()));
    }

}