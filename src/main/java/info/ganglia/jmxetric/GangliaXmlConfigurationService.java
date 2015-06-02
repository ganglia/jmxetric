package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Configures JMXetricAgent based on command line arguments and parameters
 * specified in the XML configuration source.
 * 
 */
class GangliaXmlConfigurationService extends XMLConfigurationService {
	private static Logger log = Logger.getLogger(JMXetricAgent.class.getName());

	/**
	 * default host name gmond is running on
	 */
	private static final String DEFAULT_HOSTNAME = "localhost";

	/**
	 * default port gmond listens to
	 */
	private static final String DEFAULT_PORT = "8649";

	/**
	 * default transport mode
	 */
	private static final String DEFAULT_MODE = "multicast";

	/**
	 * default multicast TTL = 5 (same site)
	 */
	private static final int DEFAULT_TTL = 5;

	/**
	 * the XML configuration file source
	 */
	private final InputSource inputSource;

	/**
	 * command line arguments that was passed in
	 */
	private final CommandLineArgs args;

	private Node ganglia;

	public GangliaXmlConfigurationService(InputSource inputSource,
			CommandLineArgs args) {
		this.inputSource = inputSource;
		this.args = args;
	}

	/**
	 * Creates a GMetric attribute on the JMXetricAgent from the XML config
	 * 
	 * @return
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public GMetric getConfig() throws IOException, XPathExpressionException {
		// TODO what happens when the node cannot be found? do we use all
		// default values?
		ganglia = getXmlNode("/jmxetric-config/ganglia", inputSource);
		// Gets the config for ganglia
		// Note that the ganglia config needs to be found before the samplers
		// are created.
		GMetric gmetric = makeGMetricFromXml();
		return gmetric;
	}

	/**
	 * Makes a GMetric object that can be use to define configuration for an
	 * agent.
	 * 
	 * @return GMetric object with the configuration
	 * @throws IOException
	 */
	GMetric makeGMetricFromXml() throws IOException {
		String hostname = getHostName();
		int port = getPort();
		UDPAddressingMode addressingMode = getAddressingMode();
		boolean v31x = getV31();
		String spoof = getSpoof();

		StringBuilder buf = new StringBuilder();
		buf.append("GMetric host=").append(hostname);
		buf.append(" port=").append(port);
		buf.append(" mode=").append(addressingMode);
		buf.append(" v31x=").append(v31x);
		buf.append(" spoof=").append(spoof);
		log.fine(buf.toString());
		System.out.println(buf.toString());
		return new GMetric(hostname, port, addressingMode, DEFAULT_TTL, v31x,
				null, spoof);
	}

	/**
	 * Gets the name of the host to be reported to.
	 * 
	 * @return name of host, defaults to "localhost"
	 */
	private String getHostName() {
		return getGangliaConfig(args.getHost(), ganglia, "hostname",
				DEFAULT_HOSTNAME);
	}

	/**
	 * Gets the port that JMXetric will announce to, this is usually the port
	 * gmond is running on
	 * 
	 * @return port number, defaults to 8649
	 */
	private int getPort() {
		String port = getGangliaConfig(args.getPort(), ganglia, "port",
				DEFAULT_PORT);
		return Integer.parseInt(port);
	}

	/**
	 * UDPAddressingMode to use for reporting
	 * 
	 * @return {@link info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode.UNICAST}
	 *         or
	 *         {@link info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode.MULTICAST}
	 */
	private UDPAddressingMode getAddressingMode() {
		String mode = getGangliaConfig(args.getMode(), ganglia, "mode",
				DEFAULT_MODE);
		if (mode.toLowerCase().equals("unicast")) {
			return UDPAddressingMode.UNICAST;
		} else {
			return UDPAddressingMode.MULTICAST;
		}
	}

	/**
	 * Whether the reporting be done on the new wire format 31.
	 * 
	 * @return true if new format is to be used
	 */
	private boolean getV31() {
		String stringv31x = getGangliaConfig(args.getWireformat(), ganglia,
				"wireformat31x", "false");
		return Boolean.parseBoolean(stringv31x);
	}

	/**
	 * Gets the value of the spoof parameter.
	 * 
	 * @return value of spoof
	 */
	private String getSpoof() {
		return getGangliaConfig(args.getSpoof(), ganglia, "spoof", null);
	}

	/**
	 * Gets a configuration parameter for Ganglia. First checks if it was given
	 * on the command line arguments. If it is not available, it looks for the
	 * value in the XML node.
	 * 
	 * @param cmdLine
	 *            command line value for this attribute
	 * @param ganglia
	 *            the XML node
	 * @param attributeName
	 *            name of the attribute
	 * @param defaultValue
	 *            default value if this attribute cannot be found
	 * @return the string value of the specified attribute
	 */
	private String getGangliaConfig(String cmdLine, Node ganglia,
			String attributeName, String defaultValue) {
		if (cmdLine == null) {
			return selectParameterFromNode(ganglia, attributeName, defaultValue);
		} else {
			return cmdLine;
		}
	}

	/**
	 * Method used by tests to print out the read in configuration.
	 * 
	 * @return string representation of configuration
	 * @throws XPathExpressionException
	 */
	String getConfigString() throws XPathExpressionException {
		ganglia = getXmlNode("/jmxetric-config/ganglia", inputSource);
		String hostname = getHostName();
		int port = getPort();
		UDPAddressingMode addressingMode = getAddressingMode();
		boolean v31x = getV31();
		String spoof = getSpoof();

		StringBuilder buf = new StringBuilder();
		buf.append("GMetric host=").append(hostname);
		buf.append(" port=").append(port);
		buf.append(" mode=").append(addressingMode);
		buf.append(" v31x=").append(v31x);
		buf.append(" spoof=").append(spoof);
		return buf.toString();
	}
}
