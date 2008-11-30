
package ganglia.gmetric;


/**
 * Implements the Ganglia gmetric command in java
 */
public class GMetric {
	public enum UDPAddressingMode {
		MULTICAST,
		UNICAST
	};
	
    private Protocol protocol ;
    
    /**
     * Constructs a GMetric 
     * @param group the host/group to send the event to
     * @param port the port to send the event to
     * @param mode the mode
     */
    public GMetric( String group, int port, UDPAddressingMode mode) {
    	this( group, port, mode, false );
    }
    /**
     * Constructs a GMetric 
     * @param group the host/group to send the event to
     * @param port the port to send the event to
     */
    public GMetric( String group, int port, UDPAddressingMode mode, boolean ganglia311) {
    	if ( ! ganglia311 )
    		this.protocol = new v30xProtocol( group, port, mode );
    	else
    		this.protocol = new v311Protocol( group, port, mode, 5 );
    }
    /**
     * The Ganglia Metric Client (gmetric) announces a metric
     * @param name Name of the metric
     * @param value Value of the metric
     * @param type Type of the metric.  Either string|int8|uint8|int16|uint16|int32|uint32|float|double
     * @param units Unit of measure for the value
     * @param slope Either zero|positive|negative|both
     * @param tmax  The maximum time in seconds between gmetric calls 
     * @param dmax The lifetime in seconds of this metric
     * @param group Group Name of the metric
     * @throws java.lang.Exception
     */
    public void announce( String name, 
            String value, 
            GMetricType type,
            String units,
            GMetricSlope slope,
            int tmax,
            int dmax,
            String group ) throws GangliaException {
        try {
        	protocol.announce( name, value, type, 
        			units, slope, tmax, dmax, group);
        } catch ( Exception ex ) {
            throw new GangliaException( "Exception announcing metric", ex ) ;
        }
    }
    /**
     * Announces a metric
     * @param name Name of the metric 
     * @param value Value of the metric
     * @param group Group Name of the metric
     * @throws ganglia.GangliaException
     */
    public void announce( String name, 
            int value, String group ) throws GangliaException {
        this.announce(name, Integer.toString(value),GMetricType.INT32, "", GMetricSlope.BOTH, 60, 0, group);
    }
    /**
     * Announces a metric
     * @param name Name of the metric 
     * @param value Value of the metric
     * @param group Group Name of the metric
     * @throws ganglia.GangliaException
     */
    public void announce( String name, 
            long value, String group  ) throws GangliaException {
        this.announce(name, Long.toString(value),GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, group );
    }
    /**
     * Announces a metric
     * @param name Name of the metric 
     * @param value Value of the metric
     * @param group Group Name of the metric
     * @throws ganglia.GangliaException
     */
    public void announce( String name, 
            float value, String group ) throws GangliaException {
        this.announce(name, Float.toString(value),GMetricType.FLOAT, "", GMetricSlope.BOTH, 60, 0, group);
    }
    /**
     * Announces a metric
     * @param name Name of the metric 
     * @param value Value of the metric
     * @param group Group Name of the metric
     * @throws ganglia.GangliaException
     */
    public void announce( String name, 
            double value, String group ) throws GangliaException {
        this.announce(name, Double.toString(value),GMetricType.DOUBLE, "", GMetricSlope.BOTH, 60, 0, group);
    }
    /**
     * Main method that sends a test metric
     * @param args
     */
    public static void main( String[] args ) {
        try {
            GMetric gm = new GMetric("127.0.0.1", 8649, UDPAddressingMode.MULTICAST) ;
            gm.announce("BOILINGPOINT", "100", GMetricType.STRING, 
                    "CELSIUS", GMetricSlope.BOTH, 0,0, "TESTGROUP");
            gm.announce("INTTEST", (int)Integer.MAX_VALUE, "TESTGROUP" ) ;
            gm.announce("LONGTEST", (long)Long.MAX_VALUE, "TESTGROUP" );
            gm.announce("FLOATTEST", (float)Float.MAX_VALUE, "TESTGROUP" ) ;
            gm.announce("DOUBLETEST", (double)Double.MAX_VALUE, "TESTGROUP" ) ;
        } catch ( Exception ex ) {
            ex.printStackTrace() ;
        }
    }
}
