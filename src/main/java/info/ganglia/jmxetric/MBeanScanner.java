package info.ganglia.jmxetric;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class MBeanScanner {
	private static class XmlTag {
		String _tag;
		String _value = "";
		Map<String, String> attributes = new HashMap<String, String>();

		public XmlTag(String tag) {
			this._tag = tag;
		}

		public void setTag(String tag) {
			_tag = tag;
		}

		public void setValue(String value) {
			_value = value;
		}

		public void addAttribute(String name, String value) {
			attributes.put(name, value);
		}

		public String toString() {
			Set<String> keys = attributes.keySet();
			StringBuilder sb = new StringBuilder();
			sb.append("<" + _tag);
			for (String key : keys) {
				sb.append(" ");
				sb.append(key);
				sb.append("=");
				sb.append("\"" + attributes.get(key) + "\"");
			}
			sb.append(">");
			sb.append(_value);
			sb.append("</" + _tag + ">");
			return sb.toString();
		}
	}

	public static void main(String[] args) {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectInstance> mBeanObjects = mBeanServer.queryMBeans(null, null);
		Map<String, XmlTag> mBeansTag = new HashMap();
		for (ObjectInstance oi : mBeanObjects) {
			// System.out.println(oi.getObjectName().getCanonicalName());
			XmlTag xmlTag = new XmlTag("mbean");
			xmlTag.addAttribute("name", oi.getObjectName().getCanonicalName());
			mBeansTag.put(oi.getObjectName().getCanonicalName(), xmlTag);
			// this goes into <mbean name="???">
			try {
				ObjectName oa = oi.getObjectName();
				MBeanInfo mbi = mBeanServer.getMBeanInfo(oa);
				MBeanAttributeInfo[] info = mbi.getAttributes();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < info.length; i++) {
					XmlTag attributeTag = new XmlTag("attribute");
					attributeTag.addAttribute("name", info[i].getName());
					attributeTag.addAttribute("type", info[i].getType());
					sb.append(attributeTag.toString());
					// for each attribute in each MBean
					// make an attribute xml and add it to the value
//					 System.out.println(info[i].getName());
//					 System.out.println(info[i].getType());
					// name is the <attribute name="???">
					 System.out.println(info[i].getType());
					// type determines if this should be composite
					 // need to convert types to proper
					 // @see info.ganglia.gmetric4j.gmetric.GMetricType
				}
				xmlTag.setValue(sb.toString());
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
		for (String key : mBeansTag.keySet()) {
//			System.out.println(key);
//			System.out.println(mBeansTag.get(key));
//			System.out.println("=====");
		}
	}
}
