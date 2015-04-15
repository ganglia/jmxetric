package info.ganglia.jmxetric;

import javax.management.ObjectName;
import java.util.HashSet;
import java.util.Set;

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