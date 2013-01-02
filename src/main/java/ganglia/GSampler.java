package ganglia;

import ganglia.gmetric.GMetricSlope;
import ganglia.gmetric.GMetricType;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public abstract class GSampler implements Runnable {

    private static Logger log =
            Logger.getLogger(GSampler.class.getName());
    /*
     * The internal data structure is a hashmap of key=mbean name
     */
    private int delay;
    private int initialDelay;
    private Publisher publisher = null;
    protected String process = null ;

    /**
     * Creates a GSampler
     * @param delay the sample interval in seconds
     * @param process the process name that is appended to metrics
     */
    public GSampler(int initialDelay, int delay, String process ) {
    	this.initialDelay = initialDelay ;
        this.delay = delay;
        this.process = process; 
    }

    /**
     * Returns the sample interval
     * @return the sample interval in seconds
     */
    public int getDelay() {
        return delay;
    }
    /**
     * Returns the initial delay before sampling begins
     * @return the delay in seconds
     */
    public int getInitialDelay() {
        return initialDelay;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

}


