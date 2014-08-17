package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.Publisher;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

/**
 * Data structure used to sample one attribute
 */
class MBeanAttribute {
	private static Logger log = Logger.getLogger(JMXetricAgent.class.getName());

	private String process;
	private String attributeName;
	private String key;
	private String canonicalName;
	private String units;
	private GMetricType type;
	private GMetricSlope slope;
	private String publishName;
	private int dmax;
	private MBeanServer mbs;
	private MBeanSampler sampler;

	public MBeanAttribute(MBeanSampler sampler, String process,
			String attributeName, String compositeKey, GMetricType type,
			String units, GMetricSlope slope, String publishName, int dmax) {
		this.sampler = sampler;
		this.process = process;
		this.key = compositeKey;
		this.canonicalName = attributeName + "." + compositeKey;
		this.attributeName = attributeName;
		this.units = units;
		this.type = type;
		this.slope = slope;
		this.publishName = publishName;
		this.dmax = dmax;
	}

	public MBeanAttribute(String process, String attributeName,
			String compositeKey, GMetricType type, String units,
			GMetricSlope slope, String publishName, int dmax) {
		this(null, process, attributeName, compositeKey, type, units, slope,
				publishName, dmax);
	}

	public MBeanAttribute(String process, String attributeName,
			GMetricType type, String units, GMetricSlope slope,
			String publishName, int dmax) {
		this(process, attributeName, null, type, units, slope, publishName,
				dmax);
	}

	public void publish(ObjectName objectName) {
		try {
			String value = null;
			if (mbs == null) {
				mbs = ManagementFactory.getPlatformMBeanServer();
			}
			Object o = mbs.getAttribute(objectName, attributeName);
			if (o instanceof CompositeData) {
				CompositeData cd = (CompositeData) o;
				if (key != null) {
					Object val = cd.get(key);
					log.fine("Sampling " + objectName + " attribute "
							+ canonicalName + ":" + val);
					value = val.toString();
				}
			} else {
				if (null != o) {
					value = o.toString();
					log.fine("Sampling " + objectName + " attribute "
							+ canonicalName + ":" + o);
				} else {
					log.fine("Not sampling " + objectName + " attribute "
							+ canonicalName + " as value is null");
				}
			}
			if (null != value) {
				Publisher gm = sampler.getPublisher();
				// log.finer("Announcing metric " + this.toString() + " value="
				// + value );
				gm.publish(process, publishName, value, getType(), getSlope(),
						sampler.getDelay(), getDMax(), getUnits());
			}

		} catch (javax.management.InstanceNotFoundException ex) {
			log.warning("Exception when getting " + objectName + " "
					+ canonicalName);
		} catch (Exception ex) {
			log.log(Level.WARNING, "Exception when getting " + objectName + " "
					+ canonicalName, ex);
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

	public int getDMax() {
		return dmax;
	}

	public MBeanSampler getSampler() {
		return sampler;
	}

	public void setSampler(MBeanSampler mBeanSampler) {
		sampler = mBeanSampler;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("attributeName=").append(attributeName);
		buf.append(" canonicalName=").append(canonicalName);
		buf.append(" units=").append(units);
		buf.append(" type=").append(type);
		buf.append(" slope=").append(slope);
		buf.append(" publishName=").append(publishName);
		buf.append(" dmax=").append(dmax);
		return buf.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (this.getClass() != obj.getClass())
			return false;
		MBeanAttribute attribute = (MBeanAttribute) obj;
		return canonicalName.equals(attribute.getCanonicalName());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79
				* hash
				+ (this.canonicalName != null ? this.canonicalName.hashCode()
						: 0);
		return hash;
	}
}