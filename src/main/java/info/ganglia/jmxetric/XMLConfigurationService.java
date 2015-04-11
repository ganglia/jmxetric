package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetric;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Configures the JMXetricAgent based on the XML config file
 */
public class XMLConfigurationService {

	private static final Logger LOG = Logger.getLogger(XMLConfigurationService.class.getName());
	private final static XPath xpath = XPathFactory.newInstance().newXPath();

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

		InputSource inputSource;
		final InputStream is = ClassLoader.getSystemResourceAsStream(args.getConfig());
		if(is != null) {
			LOG.info("loading " + args.getConfig() + " from the classpath");
			inputSource = new InputSource(is);
		} else {
			LOG.info("loading " + args.getConfig() + " from the file system");
			inputSource = new InputSource(args.getConfig());
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(inputSource);

		configureGanglia(agent, document, args);

		configureJMXetric(agent, document, args);
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
