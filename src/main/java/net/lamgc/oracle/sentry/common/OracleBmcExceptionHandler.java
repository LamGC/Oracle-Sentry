package net.lamgc.oracle.sentry.common;

import com.oracle.bmc.model.BmcException;
import net.lamgc.oracle.sentry.common.retry.RetryExceptionHandler;

public class OracleBmcExceptionHandler implements RetryExceptionHandler {
    @Override
    public boolean handle(Exception e) {
        if (e instanceof BmcException bmc) {
            return bmc.getStatusCode() == -1;
        }
        return true;
    }
}
