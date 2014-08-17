package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

/**
 * Data structure to hold and query mbean
 */
class MBeanHolder {
	/**
	 * 
	 */
	private final MBeanSampler mBeanSampler;
	private String process;
	private ObjectName objectName;
	private Set<MBeanAttribute> attributes = new HashSet<MBeanAttribute>();

	public MBeanHolder(MBeanSampler mBeanSampler, String process, String name)
			throws Exception {
		this.mBeanSampler = mBeanSampler;
		this.process = process;
		objectName = new ObjectName(name);
	}

	public void addAttribute(String attributeName, String compositeName,
			GMetricType type, GMetricSlope slope, String units,
			String publishName, int dmax) {
		attributes.add(new MBeanAttribute(mBeanSampler, process, attributeName,
				compositeName, type, units, slope, publishName, dmax));
	}

	public void addAttribute(MBeanAttribute attr) {
		attributes.add(attr);
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