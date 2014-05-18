package info.ganglia.jmxetric;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

/**
 * A utility class that scans the platform MBeanServer for registered
 * MBeans/MXBeans, and generates an XML configuration file that JMXetric can
 * use.
 * 
 * @author Ng Zhi An
 * 
 */
public class MBeanScanner {
	private static class XmlTag {
		boolean isSelfClosing = false;
		String _tag;
		String _value = "";
		List<XmlTag> _innerTags = new Vector<XmlTag>();
		Map<String, String> _attributes = new HashMap<String, String>();

		public XmlTag(String tag) {
			if (tag == null) tag = "mbean";
			this._tag = tag;
		}

		public void setSelfClosing(boolean self) {
			isSelfClosing = self;
		}

		public void setValue(String value) {
			if (value == null) return;
			_value = value;
		}

		public void addInnerTag(XmlTag tag) {
			if (tag == null) return;
			this._innerTags.add(tag);
		}

		public void addAttribute(String name, String value) {
			_attributes.put(name, value);
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<" + _tag);
			for (String key : _attributes.keySet()) {
				sb.append(" " + key + "=" + "\"" + _attributes.get(key) + "\"");
			}
			if (isSelfClosing) {
				sb.append("/>\n");
			} else {
				sb.append(">\n" + valueToString() + "</" + _tag + ">\n");
			}
			return sb.toString();
		}

		public String valueToString() {
			if (_innerTags.size() == 0) {
				return _value;
			}
			StringBuilder sb = new StringBuilder();
			for (XmlTag tag : _innerTags) {
				sb.append(tag.toString());
			}
			return sb.toString();
		}
	}
	
	private static class ConfigWriter {
		private static ConfigWriter configWriter;
		private OutputStream out;
		public static ConfigWriter getInstance() {
			if (configWriter == null) return new ConfigWriter();
			else return configWriter;
		}
		public void setOutputStream(OutputStream output) {
			this.out = output;
		}
		public void writeTag(XmlTag tag) {
			PrintWriter pw = new PrintWriter(out);
			pw.print(tag);
		}
	}

	private MBeanServer mBeanServer = ManagementFactory
			.getPlatformMBeanServer();

	/**
	 * Makes an XML <attribute> tag from the attributes of an MBean
	 * 
	 * @param oa
	 *            ObjectName representing the MBean
	 * @param info
	 *            information on an exposed MBean attribute
	 * @return
	 */
	public XmlTag makeAttributeTag(ObjectName oa, MBeanAttributeInfo info) {
		// type determines if this should be composite
		// need to convert types to proper
		// @see info.ganglia.gmetric4j.gmetric.GMetricType
		try {
			Object attr = mBeanServer.getAttribute(oa, info.getName());
			XmlTag attributeTag = new XmlTag("attribute");
			attributeTag.addAttribute("name", info.getName());
			if (attr == null) {
				return null;
			} else if (attr instanceof CompositeData) {
				List<XmlTag> compositeTags = makeCompositeDataTags((CompositeData) attr);
				attributeTag._innerTags = compositeTags;
//				attributeTag.setValue(compositeTags + System.lineSeparator());
			} else {
				attributeTag.setSelfClosing(true);
				attributeTag.addAttribute("type",
						translateDataType(info.getType()));
			}
			return attributeTag;
		} catch (AttributeNotFoundException | InstanceNotFoundException
				| MBeanException | ReflectionException | RuntimeMBeanException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	/**
	 * Builds the <composite> tags within an <attribute>
	 * 
	 * @param compositeData
	 * @return
	 */
	public List<XmlTag> makeCompositeDataTags(CompositeData compositeData) {
		List<XmlTag> tags = new Vector<XmlTag>();
		CompositeType compositeType = compositeData.getCompositeType();
		StringBuffer sb = new StringBuffer();
		for (String key : compositeType.keySet()) {
			tags.add(makeCompositeTag(compositeType, key));
		}
		return tags;
	}

	/**
	 * Builds a single <composite> tag containing the name and type of the data
	 * 
	 * @param compositeType
	 * @param name
	 * @return
	 */
	public XmlTag makeCompositeTag(CompositeType compositeType, String name) {
		XmlTag compositeXmlTag = new XmlTag("composite");
		compositeXmlTag.setSelfClosing(true);
		compositeXmlTag.addAttribute("name", name);
		String recognizedDataType = translateDataType(compositeType.getType(
				name).toString());
		compositeXmlTag.addAttribute("type", recognizedDataType);
		return compositeXmlTag;
//		return compositeXmlTag.toString();
	}
	
	public class AttributeValue {
		public String attribute;
		public String value;
		public AttributeValue(String attr, String val) {
			this.attribute = attr;
			this.value = val;
		}
	}
	
	private class ConfigMBean {
		private List<AttributeValue> mBeanAttributes;
		private List<ConfigMBeanAttribute> childAttributes;
	}
	
	private class ConfigMBeanAttribute {
		public String name;
		public boolean isComposite;
		private List<ConfigMBeanComposite> composites;
	}
	
	private class ConfigMBeanComposite {
		private List<AttributeValue> attributes;
	}

	public void run() {
		XmlTag jmxetricTag = new XmlTag("jmxetric-config");

		XmlTag jvmTag = new XmlTag("jvm");
		jvmTag.setSelfClosing(true);
		jvmTag.addAttribute("process", "ProcessName"); // default process name

		XmlTag sampleTag = new XmlTag("sample");

		jmxetricTag.addInnerTag(jvmTag);
		jmxetricTag.addInnerTag(sampleTag);

		Set<ObjectInstance> mBeanObjects = mBeanServer.queryMBeans(null, null);
		Map<String, XmlTag> mBeansTags = new HashMap<String, XmlTag>();
		for (ObjectInstance objectInstance : mBeanObjects) {
			XmlTag mBeanTag = new XmlTag("mbean");
			mBeanTag.addAttribute("name", objectInstance.getObjectName().getCanonicalName());
			mBeansTags.put(objectInstance.getObjectName().getCanonicalName(), mBeanTag);
			try {
				ObjectName objectName = objectInstance.getObjectName();
				MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objectName);
				MBeanAttributeInfo[] info = mBeanInfo.getAttributes();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < info.length; i++) {
					mBeanTag.addInnerTag(makeAttributeTag(objectName, info[i]));
				}
				sampleTag.addInnerTag(mBeanTag);
			} catch (IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(jmxetricTag);
	}

	private String translateDataType(String possibleData) {
		if (possibleData.equals("string") | possibleData.equals("int8")
				| possibleData.equals("uint8") | possibleData.equals("int16")
				| possibleData.equals("unit16") | possibleData.equals("int32")
				| possibleData.equals("uint32") | possibleData.equals("float")
				| possibleData.equals("double")) {
			return possibleData;
		}
		if (possibleData.contains("java.lang.Long")) {
			return "int8";
		} else if (possibleData.contains("java.lang.Integer")) {
			return "int32";
		} else if (possibleData.contains("java.lang.Float")) {
			return "int32";
		}
		return "string";
	}

	public static void main(String[] args) {
		MBeanScanner mBeanScanner = new MBeanScanner();
		mBeanScanner.run();
	}
}
