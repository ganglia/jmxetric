package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.GSampler;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class that samples MBeans and publishes attributes to Ganglia. This
 * classes' run method will be called periodically to sample the mbeans.
 */
public class MBeanSampler extends GSampler {

	private static Logger log = Logger.getLogger(JMXetricAgent.class.getName());

	/*
	 * The internal data structure is a hashmap of key=mbean name
	 */
	private Map<String, MBeanHolder> mbeanMap = new HashMap<String, MBeanHolder>();

	/**
	 * Creates an MBeanSampler
	 * 
	 * @param delay
	 *            the sample interval in seconds
	 * @param process
	 *            the process name that is appended to metrics
	 */
	public MBeanSampler(int initialDelay, int delay, String process) {
		super(initialDelay, delay, process);
	}

	/**
	 * Adds a mbean name/attribute pair to be sampled
	 * 
	 * @param mbean
	 *            the name of the mbean
	 * @param attribute
	 *            the name of the attribute
	 * @param composite
	 *            the name of the composite
	 * @param publishName
	 *            the name to publish this attribute on
	 * @param tmax
	 *            maximum time (in seconds) between gmetric calls
	 * @param dmax
	 *            lifetime (in seconds) of this metric (use 0 for always alive)
	 * @throws java.lang.Exception
	 */
	public void addMBeanAttribute(String mbean, String attribute,
			String composite, GMetricType type, String units,
			GMetricSlope slope, String publishName, int dmax) throws Exception {
		MBeanAttribute mba = new MBeanAttribute(this, process, attribute,
				composite, type, units, slope, publishName, dmax);
		addMBeanAttribute(mbean, mba);
	}

	/**
	 * Adds a mbean name/attribute pair to be sampled
	 * 
	 * @param mbean
	 *            the name of the mbean
	 * @param attribute
	 *            the name of the attribute
	 * @param composite
	 *            the name of the composite
	 * @param publishName
	 *            the name to publish this attribute on
	 * @throws java.lang.Exception
	 */
	public void addMBeanAttribute(String mbean, String attribute,
			String composite, GMetricType type, String units,
			GMetricSlope slope, String publishName) throws Exception {
		addMBeanAttribute(mbean, attribute, composite, type, units, slope,
				publishName, 0);
	}

	/**
	 * Adds a mbean name/attribute pair to be sampled
	 * 
	 * @param mbean
	 *            the name of the mbean
	 * @param attribute
	 *            the name of the attribute
	 * @param publishName
	 *            the name to publish this attribute on
	 * @throws java.lang.Exception
	 */
	public void addMBeanAttribute(String mbean, String attribute,
			GMetricType type, String units, GMetricSlope slope,
			String publishName) throws Exception {
		addMBeanAttribute(mbean, attribute, null, type, units, slope,
				publishName);
	}

	/**
	 * Adds an {@link info.ganglia.jmxetric.MBeanAttribute} to be sampled.
	 * 
	 * @param mbean
	 *            name of the mbean
	 * @param attr
	 *            attribute to be sample
	 * @throws Exception
	 */
	public void addMBeanAttribute(String mbean, MBeanAttribute attr)
			throws Exception {
		MBeanHolder mbeanHolder = mbeanMap.get(mbean);
		if (mbeanHolder == null) {
			mbeanHolder = new MBeanHolder(this, process, mbean);
			mbeanMap.put(mbean, mbeanHolder);
		}
		mbeanHolder.addAttribute(attr);
		log.info("Added attribute " + attr + " to " + mbean);
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
		} catch (Exception ex) {
			// Robust exception to prevent thread death
			log.warning("Exception thrown sampling Mbeans");
			log.throwing(this.getClass().getName(),
					"Exception thrown sampling Mbeans:", ex);
		}
	}
}
