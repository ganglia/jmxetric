package info.ganglia.jmxetric;


import info.ganglia.gmetric4j.GSampler;
import info.ganglia.gmetric4j.Publisher;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
// import java.util.logging.Level;
// import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

/**
 * A class that samples MBeans and publishes attributes to Ganglia.  This 
 * classes' run method will be called periodically to sample the mbeans.
 */
public class MBeanSampler extends GSampler {

    //private static Logger log =
    //        Logger.getLogger(JMXetricAgent.class.getName());
    /*
     * The internal data structure is a hashmap of key=mbean name
     */
    private Map<String, MBeanHolder> mbeanMap = new HashMap<String, MBeanHolder>();
    private MBeanServer mbs = null;

    /**
     * Creates an MBeanSampler
     * @param delay the sample interval in seconds
     * @param process the process name that is appended to metrics
     */
    public MBeanSampler(int initialDelay, int delay, String process ) {
    	super(initialDelay, delay, process);
    }

    /**
     * Adds a mbean name/attribute pair to be sampled
     * @param mbean the name of the mbean
     * @param attribute the name of the attribute
     * @param composite the name of the composite
     * @param publishName the name to publish this attribute on
     * @throws java.lang.Exception
     */
    public void addMBeanAttribute(String mbean, String attribute, String composite, 
                GMetricType type, String units, GMetricSlope slope,
                String publishName ) throws Exception {
        MBeanHolder mbeanHolder = mbeanMap.get(mbean);
        if (mbeanHolder == null) {
            mbeanHolder = new MBeanHolder(mbean);
            mbeanMap.put(mbean, mbeanHolder);
        }

        mbeanHolder.addAttribute(attribute, composite, type, slope, units, publishName);
    }
    /**
     * Adds a mbean name/attribute pair to be sampled
     * @param mbean the name of the mbean
     * @param attribute the name of the attribute
     * @param publishName the name to publish this attribute on
     * @throws java.lang.Exception
     */
    public void addMBeanAttribute(String mbean, String attribute, 
                GMetricType type, String units, GMetricSlope slope,
                String publishName ) throws Exception {
        addMBeanAttribute( mbean, attribute, null, type, units, slope, publishName );
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
            //log.warning("Exception thrown sampling Mbeans");
            //log.throwing( this.getClass().getName(), "Exception thrown sampling Mbeans:", ex) ;
        }
    }

    /**
     * Data structure used to sample one attribute
     */
    private class MBeanAttribute {

        private String attributeName;
        private String key;
        private String canonicalName;
        private String units ;
        private GMetricType type ;
        private GMetricSlope slope ;
        private String publishName ;

        public MBeanAttribute(String attributeName, String compositeKey, GMetricType type, 
                                String units, GMetricSlope slope, String publishName ) {
            this.key = compositeKey ;
            this.canonicalName = attributeName + "." + compositeKey ;
            this.attributeName = attributeName;
            this.units = units ;
            this.type = type ;
            this.slope = slope ;
            this.publishName = publishName ;
        }
        public MBeanAttribute(String attributeName, GMetricType type, GMetricSlope slope,  
                String units, String publishName ) {
			this(attributeName, null, type, units, slope, publishName);
        }

        public void publish(ObjectName objectName) {
            try {
                String value = null;
                if(mbs == null) {
                    mbs = ManagementFactory.getPlatformMBeanServer();
                }
                Object o = mbs.getAttribute(objectName, attributeName);
                if (o instanceof CompositeData) {
                    CompositeData cd = (CompositeData) o;
                    if (key != null) {
                        Object val = cd.get(key);
                        // log.fine("Sampling " + objectName +
                        //         " attribute " + canonicalName + ":" + val);
                        value = val.toString();
                    }
                } else {
                	if (null != o){
                		value = o.toString();
                		// log.fine("Sampling " + objectName +
                		// 		" attribute " + canonicalName + ":" + o);
                	}else{
                		// log.fine("Not sampling " + objectName + 
                		// 		" attribute " + canonicalName + 
                		// 		" as value is null");
                	}
                }
                
                if (null != value){
                	Publisher gm = getPublisher();
                	// log.finer("Announcing metric " + this.toString() + " value=" + value );
                	gm.publish(process, publishName, value, getType(), getSlope(), getDelay(), getUnits());
                }
                
            } catch ( javax.management.InstanceNotFoundException ex ) {
                // log.warning("Exception when getting " + objectName + " " + canonicalName);
            } catch (Exception ex) {
                // log.log(Level.WARNING,
                // 		"Exception when getting " + objectName + " " + canonicalName,
                // 		ex);
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
        
        public GMetricSlope getSlope() {
            return slope;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
        	StringBuilder buf = new StringBuilder() ;
        	buf.append("attributeName=").append(attributeName);
        	buf.append(" canonicalName=").append(canonicalName);
        	buf.append(" units=").append(units);
        	buf.append(" type=").append(type);
        	buf.append(" slope=").append(slope);
        	buf.append(" publishName=").append(publishName);
            return buf.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (this.getClass() != obj.getClass()) return false;
            MBeanAttribute attribute = (MBeanAttribute)obj ;
            return canonicalName.equals(attribute.getCanonicalName());
//            return attributeName.equals(attribute.getAttributeName());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.canonicalName != null ? this.canonicalName.hashCode() : 0);
//            hash = 79 * hash + (this.attributeName != null ? this.attributeName.hashCode() : 0);
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

        public void addAttribute(String attributeName, String compositeName, 
        		GMetricType type, GMetricSlope slope, String units, String publishName ) {
            attributes.add(new MBeanAttribute(attributeName, compositeName,
            		type, units, slope, publishName));
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


