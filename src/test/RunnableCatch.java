package test;

/**
 * Catches exceptions thrown by a Runnable, so you can check/view them later.
 */
public class RunnableCatch implements Runnable {

    /** Proxy we will run */
    private final Runnable _proxy;

    private Throwable _exception;

    public RunnableCatch(final Runnable proxy) {
        _proxy = proxy;
    }

    public void run() {
        try {
            _proxy.run();
        } catch (Throwable e) {
            synchronized (this) {
                _exception = e;
            }
        }
    }

    /** @return any exception that occurred, or NULL */
    public synchronized Throwable getException() {
        return _exception;
    }
}