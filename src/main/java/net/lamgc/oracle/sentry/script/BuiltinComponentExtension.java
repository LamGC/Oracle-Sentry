package net.lamgc.oracle.sentry.script;

import net.lamgc.oracle.sentry.script.tools.http.ScriptHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class BuiltinComponentExtension implements ScriptComponentExtension{
    @Override
    public void configureScriptComponents(ScriptComponents components) {
        components.addComponentObject("HTTP", new ScriptHttpClient(HttpClientBuilder.create()
                .build()));
        components.addComponentFactory(new ScriptLoggerFactory());
    }
}
