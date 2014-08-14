package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetric;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Configures the JMXetricAgent based on the XML config file
 */
public class XMLConfigurationService {

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
		InputSource inputSource = new InputSource(args.getConfig());

		configureGanglia(agent, inputSource, args);

		configureJMXetric(agent, inputSource, args);
	}

	private static void configureGanglia(JMXetricAgent agent,
			InputSource inputSource, CommandLineArgs args) throws IOException,
			XPathExpressionException {
		GangliaXmlConfigurationService gangliaConfigService = new GangliaXmlConfigurationService(
				inputSource, args);
		GMetric gmetric = gangliaConfigService.getConfig();
		agent.setGmetric(gmetric);
	}

	private static void configureJMXetric(JMXetricAgent agent,
			InputSource inputSource, CommandLineArgs args) throws Exception {
		JMXetricXmlConfigurationService jmxetricConfigService = new JMXetricXmlConfigurationService(
				agent, inputSource, args.getProcessName());
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

	protected static Node getXmlNode(String expr, InputSource inputSource)
			throws XPathExpressionException {
		return (Node) xpath.evaluate(expr, inputSource, XPathConstants.NODE);
	}

	NodeList getXmlNodeList(String expression, InputSource inputSource)
			throws XPathExpressionException {
		NodeList samples = (NodeList) xpath.evaluate(expression, inputSource,
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
