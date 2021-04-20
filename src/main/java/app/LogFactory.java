package app;

import org.jgroups.logging.CustomLogFactory;
import org.jgroups.logging.Log;

public class LogFactory implements CustomLogFactory {
    @Override
    public Log getLog(Class<?> aClass) {
        return new app.Log();
    }

    @Override
    public Log getLog(String s) {
        return new app.Log();
    }
}
