package app;

public class Log implements org.jgroups.logging.Log {
    @Override
    public boolean isFatalEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void fatal(String s) {

    }

    @Override
    public void fatal(String s, Object... objects) {

    }

    @Override
    public void fatal(String s, Throwable throwable) {

    }

    @Override
    public void error(String s) {

    }

    @Override
    public void error(String s, Object... objects) {

    }

    @Override
    public void error(String s, Throwable throwable) {

    }

    @Override
    public void warn(String s) {

    }

    @Override
    public void warn(String s, Object... objects) {

    }

    @Override
    public void warn(String s, Throwable throwable) {

    }

    @Override
    public void info(String s) {

    }

    @Override
    public void info(String s, Object... objects) {

    }

    @Override
    public void debug(String s) {

    }

    @Override
    public void debug(String s, Object... objects) {

    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public void trace(Object o) {

    }

    @Override
    public void trace(String s) {

    }

    @Override
    public void trace(String s, Object... objects) {

    }

    @Override
    public void trace(String s, Throwable throwable) {

    }

    @Override
    public void setLevel(String s) {

    }

    @Override
    public String getLevel() {
        return null;
    }
}
