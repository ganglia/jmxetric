package info.ganglia.jmxetric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
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
 * MBeans/MXBeans. The MBeans are queried and represented as private objects
 * These objects are then written using {@link ConfigWriter} to a
 * {@link java.io.PrintStream}.
 * 
 * @see Config
 * @see MBeanConfig
 * @see MBeanAttributeConfig
 * @see MBeanCompositeConfig
 * @see ConfigWriter
 */
public class MBeanScanner {
	private static final String ERR_FILE = "%s can not be written to, using System.out instead.\n";
	/* Platform MBean Server */
	private final MBeanServer mBeanServer = ManagementFactory
			.getPlatformMBeanServer();

	/**
	 * Used mainly for testing purposed to output a test configuration file to
	 * System.out. Also shows how to use this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PrintStream out = System.out;
		if (args.length > 0) {
			try {
				out = new PrintStream(new File(args[0]));
			} catch (FileNotFoundException e) {
				System.out.printf(ERR_FILE, args[0]);
			}
		}
		MBeanScanner mBeanScanner = new MBeanScanner();
		List<Config> configs = mBeanScanner.scan();
		ConfigWriter cw;
		cw = new ConfigWriter(out, configs);
		cw.write();
	}

	/**
	 * Scans the platform MBean server for registered MBeans, creating
	 * 
	 * @see Config objects to represent these MBeans.
	 */
	public List<Config> scan() {
		Set<ObjectInstance> mBeanObjects = mBeanServer.queryMBeans(null, null);
		List<Config> configs = getConfigForAllMBeans(mBeanObjects);
		return configs;
	}

	/**
	 * Constructs a configuration for all MBeans.
	 * 
	 * @param mBeanObjects
	 * @return
	 */
	private List<Config> getConfigForAllMBeans(Set<ObjectInstance> mBeanObjects) {
		List<Config> configs = new Vector<Config>();
		for (ObjectInstance objectInstance : mBeanObjects) {
			Config configMB = scanOneMBeanObject(objectInstance);
			configs.add(configMB);
		}
		return configs;
	}

	/**
	 * Constructs the configuration for a single MBean. The configuration
	 * includes the name, e.g. "java.util.loggin:type=Logging", and the
	 * attributes belonging to that MBean.
	 * 
	 * @param objectInstance
	 *            MBean object instance
	 * @return configuration representing this MBean
	 */
	private Config scanOneMBeanObject(ObjectInstance objectInstance) {
		MBeanConfig mBeanConfig = new MBeanConfig();
		ObjectName objectName = objectInstance.getObjectName();
		mBeanConfig.addField("name", objectName.getCanonicalName());
		scanMBeanAttributes(mBeanConfig, objectName);
		return mBeanConfig;
	}

	/**
	 * Stores all attributes of an MBean into its MBeanConfig object
	 * 
	 * @param mBeanConfig
	 *            the configuration object to store the attributes into
	 * @param mBeanName
	 *            the name of the MBean object we are getting the attributes
	 *            from
	 */
	private void scanMBeanAttributes(MBeanConfig mBeanConfig,
			ObjectName mBeanName) {
		MBeanInfo mBeanInfo;
		try {
			mBeanInfo = mBeanServer.getMBeanInfo(mBeanName);
			MBeanAttributeInfo[] infos = mBeanInfo.getAttributes();
			for (int i = 0; i < infos.length; i++) {
				MBeanAttributeConfig cMBAttr = makeConfigMBeanAttribute(
						mBeanName, infos[i]);
				mBeanConfig.addChild(cMBAttr);
			}
		} catch (IntrospectionException e) {
			System.err.println(e.getMessage());
		} catch (InstanceNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (ReflectionException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Creates an object to represent a single attribute of an MBean. An
	 * attribute can be a simple attribute, or made up composites.
	 * 
	 * @param mBeanName
	 * @param attributeInfo
	 * @return
	 */
	private MBeanAttributeConfig makeConfigMBeanAttribute(ObjectName mBeanName,
			MBeanAttributeInfo attributeInfo) {
		// type determines if this should be composite
		try {
			Object attr = mBeanServer.getAttribute(mBeanName,
					attributeInfo.getName());
			MBeanAttributeConfig config = new MBeanAttributeConfig();
			config.addField("name", attributeInfo.getName());

			if (attr == null) {
				return null;
			} else if (attr instanceof CompositeData) {
				addComposites(config, (CompositeData) attr);
			} else {
				config.addField("type",
						translateDataType(attributeInfo.getType()));
			}
			return config;
		} catch (AttributeNotFoundException | InstanceNotFoundException
				| MBeanException | ReflectionException | RuntimeMBeanException e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	/**
	 * Adds the composite data of an MBean's attribute to an
	 * MBeanAttributeConfig
	 * 
	 * @param config
	 *            configuration which the composite belongs to
	 * @param compositeData
	 *            object representing the composite data
	 */
	private void addComposites(MBeanAttributeConfig config,
			CompositeData compositeData) {
		CompositeType compositeType = compositeData.getCompositeType();
		for (String key : compositeType.keySet()) {
			config.addChild(makeComposite(compositeType, key));
		}
	}

	/**
	 * Makes a configuration for JMXetric that represents the composite tag
	 * 
	 * @param compositeType
	 * @param name
	 * @return
	 */
	private MBeanCompositeConfig makeComposite(CompositeType compositeType,
			String name) {
		MBeanCompositeConfig config = new MBeanCompositeConfig();
		config.addField("name", name);
		String rawType = compositeType.getType(name).toString();
		config.addField("type", translateDataType(rawType));
		return config;
	}

	/**
	 * The date types returned by JMX calls are no the same as those accepted by
	 * JMXetric and Ganglia. This methods provides the translation. e.g.
	 * java.lang.Long -> int8
	 * 
	 * @param possibleData
	 *            the data type string returned by Java JMX methods
	 * @return a data type string that Ganglia recognizes
	 */
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

	/* Data types that represent the MBean configuration */

	/**
	 * Config is a super class that represents a type of configuration that is
	 * fed into JMXetric.
	 */
	private class Config {
		/* name of this Config, this is the name of the XML tag */
		String name;
		/* used to determine the XML tag will be self-closing */
		boolean hasChildren = false;
		/* a map of the parameters in the tag, e.g. delay, name, pname */
		Map<String, String> fields = new HashMap<>();
		/* a list of inner/children Config */
		List<Config> children = new Vector<Config>();

		/* Users are not supposed to instantiate this class */
		private Config() {
		};

		/**
		 * Adds a new field to this Config. This is written as a parameter in
		 * this XML tag.
		 * 
		 * @param key
		 *            the name of this key, e.g. "delay", "name"
		 * @param value
		 *            value of this key, e.g. "0", "60", "java.lang:type=Memory"
		 */
		void addField(String key, String value) {
			fields.put(key, value);
		}

		/**
		 * Adds a Config to the list of children this Config has
		 * 
		 * @param config
		 *            child Config to be added to this
		 */
		void addChild(Config config) {
			if (config == null) {
				return; // ensure that the children contains no null values
			}
			if (hasChildren == false) {
				hasChildren = true;
			}
			children.add(config);
		}

		/**
		 * A String representation of this Config, includes the name and the
		 * fields.
		 */
		@Override
		public String toString() {
			return name + " " + fieldsToString();
		}

		/**
		 * Printable representation of all the fields of this Config
		 * 
		 * @return A String with all the fields in this format <key>="<value>"
		 */
		public String fieldsToString() {
			String result = "";
			for (String key : fields.keySet()) {
				result += key + "=\"" + fields.get(key) + "\" ";
			}
			// remove the trailing whitespace
			return result.substring(0, result.length() - 1);
		}
	}

	/**
	 * Represents a configuration with the name "mbean". This is the "<mbean>"
	 * tag in the XML configuration file.
	 */
	private class MBeanConfig extends Config {
		public MBeanConfig() {
			this.name = "mbean";
		}
	}

	/**
	 * Represents a configuration with the name "attribute". This is the
	 * "<attribute>" tag in the XML configuration file.
	 */
	private class MBeanAttributeConfig extends Config {
		public MBeanAttributeConfig() {
			this.name = "attribute";
		}
	}

	/**
	 * Represents a configuration with the name "composite". This is the
	 * "<composite>" tag in the XML configuration file.
	 */
	private class MBeanCompositeConfig extends Config {
		public MBeanCompositeConfig() {
			this.name = "composite";
		}
	}

	/**
	 * Writes the configuration to a {@link java.io.PrintStream}. The output is
	 * in the XML format, which is what JMXetric reads in.
	 */
	private static class ConfigWriter {
		/* The output stream that the configuration will be written to. */
		private final PrintStream out;
		/* The list of configurations to be written. */
		private final List<Config> configs;
		/* System-specific new-line separator. */
		private static final String NL = System.lineSeparator();
		/* XML Declaration used by JMXetric */
		private static final String XML_DECL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>";
		/* XML Doctype used by JMXetric */
		private static final String XML_DOCTYPE = "<!DOCTYPE jmxetric-config ["
				+ NL + "  <!ELEMENT jmxetric-config (sample|ganglia|jvm)*>"
				+ NL + "  <!ELEMENT sample (mbean)*>" + NL
				+ "    <!ATTLIST sample delay CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST sample initialdelay CDATA \"0\">" + NL
				+ "    <!ATTLIST sample dmax CDATA \"0\" >" + NL
				+ "  <!ELEMENT mbean (attribute)*>" + NL
				+ "    <!ATTLIST mbean name CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST mbean pname CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST mbean dmax CDATA \"0\" >"
				+ "  <!ELEMENT attribute (composite*)>" + NL
				+ "    <!ATTLIST attribute name CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST attribute type CDATA \"\" >" + NL
				+ "    <!ATTLIST attribute units CDATA \"\" >" + NL
				+ "    <!ATTLIST attribute pname CDATA \"\" >" + NL
				+ "    <!ATTLIST attribute slope CDATA \"both\" >" + NL
				+ "    <!ATTLIST attribute dmax CDATA \"0\" >" + NL
				+ "  <!ELEMENT composite EMPTY>" + NL
				+ "    <!ATTLIST composite name CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST composite type CDATA \"\" >" + NL
				+ "    <!ATTLIST composite units CDATA \"\" >" + NL
				+ "    <!ATTLIST composite pname CDATA \"\" >" + NL
				+ "    <!ATTLIST composite slope CDATA \"both\" >" + NL
				+ "    <!ATTLIST composite dmax CDATA \"0\" >" + NL
				+ "  <!ELEMENT ganglia EMPTY>" + NL
				+ "    <!ATTLIST ganglia hostname CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST ganglia port CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST ganglia mode CDATA #REQUIRED>" + NL
				+ "    <!ATTLIST ganglia wireformat31x CDATA #REQUIRED>" + NL
				+ "  <!ELEMENT jvm EMPTY>" + NL
				+ "    <!ATTLIST jvm process CDATA \"\">" + NL + "]>";

		public ConfigWriter(PrintStream outputStream, List<Config> config) {
			this.out = outputStream;
			this.configs = config;
		}

		/**
		 * Write the configuration out to the PrintStream out.
		 */
		public void write() {
			if (configs == null)
				return;
			StringBuilder sb = new StringBuilder();
			sb.append(XML_DECL + NL);
			sb.append(XML_DOCTYPE + NL);
			sb.append("<jmxetric-config>" + NL);
			sb.append("  <jvm process=\"ProcessName\"/>" + NL);
			sb.append("  <sample initialdelay=\"20\" delay=\"60\">" + NL);
			String output = buildXmlTagsFromList(configs, "    ");
			sb.append(output);
			sb.append("  </sample>" + NL);
			sb.append("</jmxetric-config>" + NL);
			out.print(sb.toString());
		}

		/**
		 * Builds a String of XML tags out of a list of Config. Each Config is
		 * built by {@link buildConfig} and the result is interspersed with new
		 * lines for readability.
		 * 
		 * @param configs
		 *            list of Config to be converted to XML tags
		 * @param indent
		 *            string that is prefixed to each XML tag
		 * @return string of XML tags for list
		 */
		public String buildXmlTagsFromList(List<Config> configs, String indent) {
			StringBuilder sb = new StringBuilder();
			for (Config config : configs) {
				sb.append(buildXmlTag(config, indent) + NL);
			}
			return sb.toString();
		}

		/**
		 * Builds a XML tag for config. The tag with have indent prefixed.
		 * 
		 * @param config
		 *            to be converted to an XML tag
		 * @param indent
		 *            indentation string that is prefixed to the tag
		 * @return XML tag for this config
		 */
		private String buildXmlTag(Config config, String indent) {
			StringBuffer sb = new StringBuffer();
			sb.append(indent + "<" + config.name + " "
					+ config.fieldsToString());
			if (!config.hasChildren) {
				// self-closing XML tag
				sb.append("/>");
			} else {
				sb.append(">" + NL);
				sb.append(buildXmlTagsFromList(config.children, "  " + indent));
				sb.append(indent + "</" + config.name + ">");
			}
			return sb.toString();
		}
	}
}
