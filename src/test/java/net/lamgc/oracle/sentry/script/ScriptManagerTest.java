package net.lamgc.oracle.sentry.script;

import net.lamgc.oracle.sentry.oci.compute.ComputeInstanceManager;
import net.lamgc.oracle.sentry.oci.account.OracleAccountManager;
import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

class ScriptManagerTest {

    @Test
    public void loadScriptTest() {
        ScriptManager manager = new ScriptManager(new File("./run/scripts"),
                new ScriptComponents(new ScriptHttpClient(HttpClientBuilder.create().build()),
                        new ComputeInstanceManager(), new ScriptLoggerFactory(), new OracleAccountManager()));

        manager.loadScripts();

    }

}