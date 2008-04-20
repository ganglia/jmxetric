package ganglia.gmetric;

/**
 *
 * @author humphrej
 */
public class GangliaException extends Exception {

    /**
     * Creates a new instance of <code>GangliaException</code> without detail message.
     */
    public GangliaException() {
    }


    /**
     * Constructs an instance of <code>GangliaException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GangliaException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>GangliaException</code> with the specified detail message and cause.
     * @param msg the detail message
     * @param cause the cause
     */
    public GangliaException( String msg, Throwable cause ) {
        super( msg, cause );
    }
}
