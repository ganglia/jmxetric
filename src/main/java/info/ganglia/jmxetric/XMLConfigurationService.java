package info.ganglia.jmxetric;


import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.io.IOException;
//import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
//    private static Logger log =
//      Logger.getLogger(JMXetricAgent.class.getName());

    private final static XPath xpath = XPathFactory.newInstance().newXPath();
    
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "8649";
    private static final String DEFAULT_CONFIG="jmxetric.xml";
    private static final String DEFAULT_MODE="multicast";
    private static final String DEFAULT_USE_WIREFORMAT31 = "false";
    
    /**
     * Configures the JMXetricAgent based on the supplied agentArgs
     * @param agent the agent to configure
     * @param agentArgs the agent arguments list
     * @throws java.lang.Exception
     */
    public static void configure(JMXetricAgent agent, String agentArgs) throws Exception {
    	CommandLineArgs args = new CommandLineArgs(agentArgs);

    	// log.config("Command line argument found: host=" + args.getHost());
    	// log.config("Command line args: port=" + args.getPort());
    	// log.config("Command line args: config=" + args.getConfig());
    	// log.config("Command line args: mode=" + args.getMode());
    	// log.config("Command line args: wireformat31x=" + args.getWireformat());
    	// log.config("Command line args: process=" + args.getProcessName());
    	// log.config("Command line args: spoof=" + args.getSpoof());

    	InputSource inputSource = new InputSource(args.getConfig());

    	configureGangliaFromXML(agent, inputSource, args.getHost(), args.getPort(), args.getMode(), 
    			args.getWireformat(), args.getSpoof());
    	configureJMXetricFromXML(agent, inputSource, args.getConfig(), args.getProcessName());
    }

    private final static Pattern argPattern = Pattern.compile( "(\\S+?)\\=(\\S*)" );

    // Default multicast TTL = 5 (same site)
	private static final int DEFAULT_TTL = 5;
    
    /**
     * Parses the string array, input, looking for a pattern tag=value
     * @param tag the tag to search for
     * @param input the array list 
     * @param defaultValue the default value if tag is not found
     * @return tha value
     */
    private static String getTagValue(String tag, String[] input, String defaultValue) {
        for( String arg: input) {
            Matcher matcher = argPattern.matcher(arg);
            // Get tagname and contents of tag
            if ( matcher.find() ) {
                String tagname = matcher.group(1);     
                if ( tag.equals( tagname )) {
                    return matcher.group(2);
                }
            }
        }
        return defaultValue ;
    }
    
    private static String selectParameterFromNode( Node ganglia, 
    		String attributeName, String defaultValue ) {
    	return selectParameterFromNode( (String)null, ganglia, attributeName, defaultValue);
    }

    private static String selectParameterFromNode(String cmdLine, 
    		Node ganglia, String attributeName, 
    		String defaultValue) {
    	String ret = defaultValue ;
    	if ( cmdLine != null )
    		ret = cmdLine ;
    	else if ( ganglia != null ) {
    		Node node = ganglia.getAttributes().getNamedItem(attributeName);
    		if ( node != null ) {
	    		String value = node.getNodeValue();
	    		if ( value != null )
	    			ret = value ;
    		}
    	}
    	return ret ;
    }
    /**
     * Creates a GMetric attribute on the JMXetricAgent from the XML config
     * @param agent the agent to configure
     * @param inputSource the input xml
     * @param cmdLineHost the host found on the agent arg list
     * @param cmdLinePort the port found on the agent arg list
     * @param cmdLineMode the mode found on the agent arg list
     * @param v31x true if the ganglia v31x wire format should be used
     * @param cmdLineSpoof the spoof value found on the agent arg list
     * @throws IOException
     * @throws XPathExpressionException
     * @throws java.lang.Exception
     */
    private static void configureGangliaFromXML(JMXetricAgent agent, 
            InputSource inputSource, String cmdLineHost, 
            String cmdLinePort, String cmdLineMode, String cmdLinev31x, String cmdLineSpoof)
            		throws XPathExpressionException, IOException
                    {
        // Gets the config for ganglia
        // Note that the ganglia config needs to be found before the samplers 
        // are created.
        GMetric gmetric = makeGMetricFromXml(inputSource, cmdLineHost, cmdLinePort, cmdLineMode, cmdLinev31x, cmdLineSpoof);
        agent.setGmetric(gmetric);
    }
    
    private static GMetric makeGMetricFromXml(InputSource inputSource, String cmdLineHost,
            String cmdLinePort, String cmdLineMode, String cmdLinev31x, String cmdLineSpoof)
            		throws XPathExpressionException, IOException {
        String gangliaExpr = "/jmxetric-config/ganglia";
        Node g = (Node) xpath.evaluate(gangliaExpr, inputSource,
                XPathConstants.NODE);
        String hostname = selectParameterFromNode(cmdLineHost,
        		g, "hostname", DEFAULT_HOST);
        String port = selectParameterFromNode(cmdLinePort,
        		g, "port", DEFAULT_PORT);
        int iport = Integer.parseInt(port);
        String mode = selectParameterFromNode(cmdLineMode,
        		g, "mode",DEFAULT_MODE);
        UDPAddressingMode addressingMode = UDPAddressingMode.MULTICAST;
        if (mode.toLowerCase().equals("unicast")) {
        	addressingMode = UDPAddressingMode.UNICAST;
        }
        String stringv31x = selectParameterFromNode(cmdLinev31x,
        		g, "wireformat31x", DEFAULT_USE_WIREFORMAT31);
        boolean v31x = Boolean.parseBoolean(stringv31x);
        String spoof = selectParameterFromNode(cmdLineSpoof,
                g, "spoof", null);
        
        StringBuilder buf = new StringBuilder();
        buf.append("GMetric host=").append(hostname);
        buf.append(" port=").append(port);
        buf.append(" mode=").append(mode);
        buf.append(" v31x=").append(v31x);
        buf.append(" spoof=").append(spoof);
        // log.fine(buf.toString());
        return new GMetric(hostname, iport, addressingMode, DEFAULT_TTL, v31x, null, spoof);
    }

    /**
     * Creates managed attributed on the JMXetricAgent from the XML config
     * @param agent the agent to configure
     * @param inputSource the input xml
     * @param file the config file from trom the agent arg list
     * @throws java.lang.Exception
     */
    private static void configureJMXetricFromXML(JMXetricAgent agent,
            InputSource inputSource, String file, String processName) throws Exception {

        // if the processName supplied from the agent arg list is null, see if 
        // it's in the xml
        if ( processName == null ) {
            Node jvm = (Node) xpath.evaluate("/jmxetric-config/jvm", inputSource,
                XPathConstants.NODE);
            if ( jvm != null )
                processName = jvm.getAttributes().getNamedItem("process").getNodeValue();
        }
        
        // Gets the config for the samplers
        //
        String expression = "/jmxetric-config/sample";
        NodeList samples = (NodeList) xpath.evaluate(expression, inputSource,
                XPathConstants.NODESET);
        // for every sample
        for (int i = 0; i < samples.getLength(); i++) {
            Node sample = samples.item(i);

            //log.finer("Sample is " + sample);
            /*
             *         String port = selectParameterFromNode( cmdLinePort, 
        		g, "port", "8649");
        int iport = Integer.parseInt(port);

             */
            String delayString = selectParameterFromNode( sample, "delay", "60");
            int delay = Integer.parseInt(delayString);
            String initialDelayString = selectParameterFromNode( sample, "initialdelay", "0");
            int initialDelay = Integer.parseInt(initialDelayString);
            String sampleDMax = selectParameterFromNode(sample, "dmax", "0");
            MBeanSampler mbSampler = new MBeanSampler(initialDelay, delay, processName);
            NodeList mbeans = (NodeList) xpath.evaluate("mbean", sample,
                    XPathConstants.NODESET);
            // for every mbean
            for (int j = 0; j < mbeans.getLength(); j++) {
                Node mbean = mbeans.item(j);
                String mbeanName = selectParameterFromNode( mbean, "name", null );
                String mbeanPublishName = selectParameterFromNode( mbean, "pname", "NULL" );
                // log.finer("Mbean is " + mbeanName);
                NodeList attrs = (NodeList) xpath.evaluate("attribute", mbean,
                        XPathConstants.NODESET);
                //for every attribute
                for (int k = 0; k < attrs.getLength(); k++) {
                    Node attr = attrs.item(k);
                    String attrName = selectParameterFromNode(attr, "name","NULL");
                    String type = selectParameterFromNode(attr, "type", "");
                    String units = selectParameterFromNode(attr, "units", "" );
                    String pname = selectParameterFromNode(attr, "pname", "");
                    String slope = selectParameterFromNode(attr, "slope", "" );
                    String dMax = selectParameterFromNode(attr, "dmax",
                            selectParameterFromNode(mbean, "dmax", sampleDMax));
                    
                    if ( "".equals(type)) {
                    	//assume that there is a composite attribute to follow
                        NodeList composites = (NodeList) xpath.evaluate("composite", attr,
                                XPathConstants.NODESET);
                        //for every composite
                        for (int l = 0; l < composites.getLength(); l++) {
                            Node composite = composites.item(l);
                            String compositeName = selectParameterFromNode(composite, "name", "NULL");
                            String compositeType = selectParameterFromNode(composite, "type", "");
                            String compositeUnits = selectParameterFromNode(composite, "units", "");
                            String compositePname = selectParameterFromNode(composite, "pname", "");
                            String compositeSlope = selectParameterFromNode(composite, "slope", "");
                            String compositeDMax = selectParameterFromNode(composite, "dmax", dMax);
                            String metricName = buildMetricName( processName, mbeanName, 
                                    mbeanPublishName, compositeName, compositePname );
    	                    //log.finer("Attr is " + compositeName);
    	                    mbSampler.addMBeanAttribute(mbeanName, attrName, compositeName, 
    	                    		GMetricType.valueOf(compositeType.toUpperCase()), compositeUnits,
    	                    		GMetricSlope.valueOf(compositeSlope.toUpperCase()), metricName,
    	                    		parseDMax(compositeDMax));
                        }
                    } else {
                    	// It's a non composite attribute
	                    //log.finer("Attr is " + attrName);
	                    String metricName = buildMetricName( processName, mbeanName, 
	                            mbeanPublishName, attrName, pname );
	                    mbSampler.addMBeanAttribute(mbeanName, attrName, null, 
	                    		GMetricType.valueOf(type.toUpperCase()), units,
	                    		GMetricSlope.valueOf(slope.toUpperCase()), metricName,
	                    		parseDMax(dMax));
                    }
                }
            }
            agent.addSampler(mbSampler);
        }
    }


    /**
     * Parses dMaxString, which is the value of dmax read in from a configuration file.
     * @param dMaxString value read in from configuration
     * @return int value of dMaxString if parse is successful, 0 (default value) otherwise
     */
    private static int parseDMax(String dMaxString) {
        int dMax;
        try {
            dMax = Integer.parseInt(dMaxString);
        } catch (NumberFormatException e) {
            dMax = 0;
        }
        return dMax;
    }

    /**
     * Builds the metric name in ganglia
     * @param process the process name, or null if not used
     * @param mbeanName the mbean name
     * @param mbeanPublishName the mbean publish name, or null if not used
     * @param attribute the mbean attribute name
     * @param attrPublishName the mbean attribute publish name
     * @return the metric name
     */
    private static String buildMetricName( String process, 
            String mbeanName, String mbeanPublishName, 
            String attribute, String attrPublishName ) {
        StringBuilder buf = new StringBuilder() ;
        if ( process != null ) {
            buf.append( process );
            buf.append("_");
        }
        if ( mbeanPublishName != null ) {
            buf.append( mbeanPublishName);
        } else {
            buf.append( mbeanName );
        }
        buf.append("_");
        if ( ! "".equals( attrPublishName ) ) {
            buf.append( attrPublishName );
        } else {
            buf.append( attribute );
        }
        return buf.toString() ;
    }
}
