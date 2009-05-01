package jmxetric;

import ganglia.gmetric.GMetricType;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

/**
 * A class that samples MBeans and publishes attributes to Ganglia.  This 
 * classes' run method will be called periodically to sample the mbeans.
 */
public class MBeanSampler implements Runnable {

    private static Logger log =
            Logger.getLogger(JMXetricAgent.class.getName());
    /*
     * The internal data structure is a hashmap of key=mbean name
     */
    private Map<String, MBeanHolder> mbeanMap = new HashMap<String, MBeanHolder>();
    private int delay;
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private Publisher publisher = null;
    private String process = null ;

    /**
     * Creates an MBeanSampler
     * @param delay the sample interval in seconds
     * @param process the process name that is appended to metrics
     */
    public MBeanSampler(int delay, String process ) {
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
     * Adds a mbean name/attribute pair to be sampled
     * @param mbean the name of the mbean
     * @param attribute the name of the attribute
     * @param publishName the name to publish this attribute on
     * @throws java.lang.Exception
     */
    public void addMBeanAttribute(String mbean, String attribute, 
                GMetricType type, String units, String publishName ) throws Exception {
        MBeanHolder mbeanHolder = mbeanMap.get(mbean);
        if (mbeanHolder == null) {
            mbeanHolder = new MBeanHolder(mbean);
            mbeanMap.put(mbean, mbeanHolder);
        }

        mbeanHolder.addAttribute(attribute, type, units, publishName);
    }

    /**
     * Called by the JMXAgent periodically to sample the mbeans
     */
    public void run() {
        try {
            for (String mbean : mbeanMap.keySet()) {
                MBeanHolder h = mbeanMap.get(mbean);
                h.publish();
            }
        } catch ( Exception ex ) {
            // Robust exception to prevent thread death
            log.warning("Exception thrown sampling Mbeans");
            log.throwing( this.getClass().getName(), "Exception thrown sampling Mbeans:", ex) ;
        }
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Data structure used to sample one attribute
     */
    private class MBeanAttribute {

        private String attributeName;
        private String key;
        private String canonicalName;
        private String units ;
        GMetricType type ;
        private String publishName ;

        public MBeanAttribute(String attributeName, GMetricType type, 
                                String units, String publishName ) {
            String[] tokens = attributeName.split("\\.");
            if (tokens.length == 1) {
                this.attributeName = attributeName;
            } else {
                this.attributeName = tokens[0];
                key = tokens[1];
            }
            this.canonicalName = attributeName;
            this.units = units ;
            this.type = type ;
            this.publishName = publishName ;
        }

        public void publish(ObjectName objectName) {
            try {
                String value = null;
                Object o = mbs.getAttribute(objectName, attributeName);
                if (o instanceof CompositeData) {
                    CompositeData cd = (CompositeData) o;
                    if (key != null) {
                        Object val = cd.get(key);
                        log.fine("Sampling " + objectName +
                                " attribute " + canonicalName + ":" + val);
                        value = val.toString();
                    }
                } else {
                    value = o.toString();
                    log.fine("Sampling " + objectName +
                            " attribute " + canonicalName + ":" + o);
                }
                Publisher gm = getPublisher();
                log.finer("Announcing metric " + publishName + "=" + value +"("+ getUnits() +")" );
                gm.publish(process, publishName, value, getType(), getUnits());
            } catch (Exception ex) {
                log.warning("Exception when getting " + canonicalName);
                ex.printStackTrace();
            }

        }

        public String getAttributeName() {
            return attributeName;
        }

        public String getCanonicalName() {
            return canonicalName;
        }

        public String getUnits() {
            return units;
        }

        public GMetricType getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return canonicalName;
        }

        @Override
        public boolean equals(Object obj) {
            return attributeName.equals(obj);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.attributeName != null ? this.attributeName.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Data structure to hold and query mbean
     */
    private class MBeanHolder {
        private ObjectName objectName;
        private Set<MBeanAttribute> attributes = new HashSet<MBeanAttribute>();

        public MBeanHolder(String name) throws Exception {
            objectName = new ObjectName(name);
        }

        public void addAttribute(String attributeName, GMetricType type, String units, String publishName ) {
            attributes.add(new MBeanAttribute(attributeName, type, units, publishName));
        }

        public void publish() {
            for (MBeanAttribute attr : attributes) {
                try {
                    attr.publish(objectName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}


