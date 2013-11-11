package info.ganglia.jmxetric;


import info.ganglia.gmetric4j.GSampler;
import info.ganglia.gmetric4j.Publisher;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;
import info.ganglia.gmetric4j.gmetric.GangliaException;

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
	private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

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
	 * @param interval the name of the interval
	 * @param publishName the name to publish this attribute on
	 * @throws java.lang.Exception
	 */
	public void addMBeanAttribute(String mbean, String attribute, String composite, 
			GMetricType type, String units, GMetricSlope slope, String publishName,
			int interval) throws Exception {
		MBeanHolder mbeanHolder = mbeanMap.get(mbean);
		if (mbeanHolder == null) {
			mbeanHolder = new MBeanHolder(mbean);
			mbeanMap.put(mbean, mbeanHolder);
		}

		mbeanHolder.addAttribute(attribute, composite, type, slope, units, publishName, interval);
	}
	/**
	 * Adds a mbean name/attribute pair to be sampled
	 * @param mbean the name of the mbean
	 * @param attribute the name of the attribute
	 * @param interval the name of the interval
	 * @param publishName the name to publish this attribute on
	 * @throws java.lang.Exception
	 */
	public void addMBeanAttribute(String mbean, String attribute, 
			GMetricType type, String units, GMetricSlope slope,
			String publishName, int interval) throws Exception {
		addMBeanAttribute( mbean, attribute, null, type, units, slope, publishName, interval);
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
		private int interval;
		private long time;

		public MBeanAttribute(String attributeName, String compositeKey, GMetricType type, 
				String units, GMetricSlope slope, String publishName, int interval) {
			this.key = compositeKey ;
			this.canonicalName = attributeName + "." + compositeKey ;
			this.attributeName = attributeName;
			this.units = units ;
			this.type = type ;
			this.slope = slope ;
			this.publishName = publishName ;
			this.interval = interval;
		}
		@SuppressWarnings("unused")
		public MBeanAttribute(String attributeName, GMetricType type, GMetricSlope slope,  
				String units, String publishName, int interval, long time) {
			this(attributeName, null, type, units, slope, publishName, interval);
		}

		/** 
		 * MY copy to modify at will
		 * @author Juliana
		 */
		public void publish(String value) {
			if (value != null){
				Publisher gm = getPublisher();
				//log.finer("Announcing metric " + this.toString() + " value=" + value );
				try {
					gm.publish(process, publishName, value, getType(), getSlope(), getUnits());
				} catch (GangliaException e) {
					e.printStackTrace();
				}
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

		public int getInterval() {
			return interval;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
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
			buf.append(" interval=").append(interval);
			buf.append(" time=").append(time);
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
	 * Data structure to keep rate and time of read.
	 */
	private class MBeanRate {
		private Object value;
		private long time;

		public MBeanRate(Object value, long time) {
			this.value = value;
			this.time = time;
		}
		public Object getValue() {
			return value;
		}
		public long getTime() {
			return time;
		}
	}

	/**
	 * Data structure to hold and query mbean
	 */
	private class MBeanHolder {
		private ObjectName objectName;
		private Set<MBeanAttribute> attributes = new HashSet<MBeanAttribute>();
		private Map<String, MBeanRate> readRates = new HashMap<String, MBeanRate>();

		public MBeanHolder(String name) throws Exception {
			objectName = new ObjectName(name);
		}

		public void addAttribute(String attributeName, String compositeName, 
				GMetricType type, GMetricSlope slope, String units, String publishName, int interval) {
			attributes.add(new MBeanAttribute(attributeName, compositeName,
					type, units, slope, publishName, interval));
		}

		public Object getRate(Object value1, Object value0, long time) {
			if(value1 instanceof String) {
				return value1;
			}
			if(time==0) {
				return value1;
			}
			if(value0.equals(value1)) {
				return 0;
			}
			Object value = ObjectUtils.subtractValues(value1, value0);
			value = ObjectUtils.divideValueByTime(value, time);
			return value;
		}

		public void publish() {
			long t;
			for (MBeanAttribute attr : attributes) {  
				try {
					String value = null;
					Object val = mbs.getAttribute(objectName, attr.getAttributeName());
					attr.setTime(System.currentTimeMillis());
					if (val instanceof CompositeData) {
						CompositeData cd = (CompositeData) val;
						if (attr.getKey() != null) {
							val = cd.get(attr.getKey());
						}
					} 
					if(attr.getInterval() > 0) {
						String rateName = attr.getCanonicalName();
						MBeanRate mba = readRates.get(rateName);
						if (mba == null) {
							readRates.put(rateName, new MBeanRate(val, System.currentTimeMillis()));
						}
						else {
							t = attr.getTime() - mba.getTime();
							val = getRate(val, mba.getValue(), t);
							readRates.put(rateName, new MBeanRate(val, attr.getTime()));
						}
					} 
					value = val.toString();
					if (value != null){
						attr.publish(value);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
	}
	
}


