package info.ganglia.jmxetric;

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class GangliaXmlConfigurationServiceTest {

	private final String ARGS1 = "host=localhost,port=8649,wireformat31x=true,config=etc/jmxetric.xml";
	private final String ARGS1_EXPECTED = "GMetric host=localhost port=8649 mode=MULTICAST v31x=true spoof=null";
	private final String ARGS2 = "host=info.ganglia.com,port=9999,wireformat31x=false,config=etc/config/jmxetric.xml";
	private final String ARGS2_EXPECTED = "GMetric host=info.ganglia.com port=9999 mode=MULTICAST v31x=false spoof=null";

	private final String BAD_PORT = "host=info.ganglia.com,port=abcd,wireformat31x=false,config=etc/config/jmxetric.xml";

	private final String BAD_WIRE = "host=localhost,port=8649,wireformat31x=123,config=etc/jmxetric.xml";
	private final String BAD_WIRE_EXPECTED = "GMetric host=localhost port=8649 mode=MULTICAST v31x=false spoof=null";

	private Document document;
	private CommandLineArgs args;
	private GangliaXmlConfigurationService g;

	@Before
	public void setUp() throws ParserConfigurationException, IOException, SAXException {
		InputSource inputSource = new InputSource("etc/jmxetric.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.parse(inputSource);
	}

	@Test
	public void testGetConfig_withNoCommandLineArguments() throws Exception {
		args = new CommandLineArgs(null);
		g = new GangliaXmlConfigurationService(document, args);
		try {
			assertEquals(ARGS1_EXPECTED, g.getConfigString());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		args = new CommandLineArgs("");
		g = new GangliaXmlConfigurationService(document, args);
		try {
			assertEquals(ARGS1_EXPECTED, g.getConfigString());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetConfig_withValidArguments() {
		args = new CommandLineArgs(ARGS1);
		g = new GangliaXmlConfigurationService(document, args);
		try {
			assertEquals(ARGS1_EXPECTED, g.getConfigString());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		args = new CommandLineArgs(ARGS2);
		g = new GangliaXmlConfigurationService(document, args);
		try {
			assertEquals(ARGS2_EXPECTED, g.getConfigString());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected=NumberFormatException.class)
	public void testGetConfig_withBadPort() throws Exception {
		args = new CommandLineArgs(BAD_PORT);
		g = new GangliaXmlConfigurationService(document, args);
		try {
			assertEquals(ARGS2_EXPECTED, g.getConfigString());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetConfig_withBadWireFormat() throws Exception {
		args = new CommandLineArgs(BAD_WIRE);
		g = new GangliaXmlConfigurationService(document, args);
		try {
			assertEquals(BAD_WIRE_EXPECTED, g.getConfigString());
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
