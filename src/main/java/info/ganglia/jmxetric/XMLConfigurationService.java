package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetric;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Configures the JMXetricAgent based on the XML config file
 */
public class XMLConfigurationService {

	/**
	 * XML Configuration files could go in /etc/ganglia.
	 */
	public static final String GANGLIA_CONFIG_HOME = "/etc/ganglia/";
	private static final Logger LOG = Logger.getLogger(XMLConfigurationService.class.getName());
	private final static XPath xpath = XPathFactory.newInstance().newXPath();
	private final static String DEFAULT_CONFIGURATION = "default_jmxetric.xml";

	/**
	 * Configures the JMXetricAgent based on the supplied agentArgs Command line
	 * arguments overwrites XML arguments. Any arguments that is required but no
	 * supplied will be defaulted.
	 * 
	 * @param agent
	 *            the agent to configure
	 * @param agentArgs
	 *            the agent arguments list
	 * @throws java.lang.Exception
	 */
	public static void configure(JMXetricAgent agent, String agentArgs)
			throws Exception {
		CommandLineArgs args = new CommandLineArgs(agentArgs);

		InputSource inputSource = maketInputSource(args.getConfig());

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(inputSource);

		configureGanglia(agent, document, args);

		configureJMXetric(agent, document, args);
	}

	private static InputSource maketInputSource(String configFilename) {
		final InputStream is = ClassLoader.getSystemResourceAsStream(configFilename);
		if(is != null) {
			LOG.info("loading " + configFilename + " from the classpath");
			return new InputSource(is);
		}

		final File fis = new File(configFilename);
		if( fis.exists() && fis.isFile()) {
			LOG.info("loading " + configFilename + " from the file system");
			return new InputSource(configFilename);
		}

//		File fisExt = new File(System.getProperty("java.home") + "/lib/ext/" + configFilename);
		File fisExt = new File(GANGLIA_CONFIG_HOME + configFilename);
		if( fisExt.exists() && fisExt.isFile()) {
			LOG.info("loading " + fisExt.toString() + " from the ext directory");
			try {
				return new InputSource(new FileInputStream(fisExt));
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Cannot find " + configFilename, e);
			}
		}
		final InputStream defaultIs = ClassLoader.getSystemResourceAsStream(DEFAULT_CONFIGURATION);
		if(is != null) {
			LOG.info("loading " + DEFAULT_CONFIGURATION + " from the classpath");
			return new InputSource(is);
		} else {
			throw new IllegalStateException("Cannot find " + DEFAULT_CONFIGURATION);
		}
	}

	private static void configureGanglia(JMXetricAgent agent,
			Document document, CommandLineArgs args) throws IOException,
			XPathExpressionException {
		GangliaXmlConfigurationService gangliaConfigService = new GangliaXmlConfigurationService(
				document, args);
		GMetric gmetric = gangliaConfigService.getConfig();
		agent.setGmetric(gmetric);
	}

	private static void configureJMXetric(JMXetricAgent agent,
			Document document, CommandLineArgs args) throws Exception {
		JMXetricXmlConfigurationService jmxetricConfigService = new JMXetricXmlConfigurationService(
				agent, document, args.getProcessName());
		jmxetricConfigService.configure();
	}

	String selectParameterFromNode(Node ganglia, String attributeName,
			String defaultValue) {
		if (ganglia == null) {
			return defaultValue;
		}
		Node node = ganglia.getAttributes().getNamedItem(attributeName);
		if (node == null) {
			return defaultValue;
		}
		String value = node.getNodeValue();
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	protected static Node getXmlNode(String expr, Document document)
			throws XPathExpressionException {
		return (Node) xpath.evaluate(expr, document, XPathConstants.NODE);
	}

	NodeList getXmlNodeList(String expression, Document document)
			throws XPathExpressionException {
		NodeList samples = (NodeList) xpath.evaluate(expression, document,
				XPathConstants.NODESET);
		return samples;
	}

	NodeList getXmlNodeSet(String expression, Node node)
			throws XPathExpressionException {
		NodeList samples = (NodeList) xpath.evaluate(expression, node,
				XPathConstants.NODESET);
		return samples;
	}
}
