package info.ganglia.jmxetric;

import static org.junit.Assert.*;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JMXetricXmlConfigurationServiceTest {
	private JMXetricXmlConfigurationService jmxetric;
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private Document document;

	@Before
	public void setUp() throws ParserConfigurationException, IOException, SAXException {
		InputSource inputSource = new InputSource(
				"src/test/resources/jmxetric_test.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.parse(inputSource);
		jmxetric = new JMXetricXmlConfigurationService(null, document,
				"TestProcessName");
	}

	@Test
	public void testAll() throws XPathExpressionException {
		NodeList samples = (NodeList) xpath.evaluate("/jmxetric-config/sample",
				document, XPathConstants.NODESET);
		Node firstSample = samples.item(0);
		Node secondSample = samples.item(1);

		NodeList firstMBeans = (NodeList) xpath.evaluate("mbean", firstSample,
				XPathConstants.NODESET);
		NodeList secondMBeans = (NodeList) xpath.evaluate("mbean",
				secondSample, XPathConstants.NODESET);

		Node mBeanWithCompositeAttributes = firstMBeans.item(0);
		Node mBeanWithSimpleAttributes = secondMBeans.item(1);
		Node mBeanWithBoth = secondMBeans.item(0);

		try {
			testMBeanWithSimpleAttributes(mBeanWithSimpleAttributes);
			testMBeanWithCompositeAttributes(mBeanWithCompositeAttributes);
			testMBeanWithBothKinds(mBeanWithBoth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void testMBeanWithBothKinds(Node mBeanWithBoth) throws Exception {
		List<MBeanAttribute> attributes = jmxetric.getAttributesForMBean(
				mBeanWithBoth, "1");

		assertEquals(9, attributes.size());
		assertAttribute(attributes.get(0), "Int", "Int.null", 0,
				GMetricType.INT32, "");
		assertAttribute(attributes.get(1), "Long", "Long.null", 0,
				GMetricType.INT32, "");
		assertAttribute(attributes.get(6), "Composite", "Composite.date", 0,
				GMetricType.STRING, "bytes");
		assertAttribute(attributes.get(7), "Composite", "Composite.integer", 4,
				GMetricType.INT32, "bytes");
		assertAttribute(attributes.get(8), "Composite", "Composite.name", 0,
				GMetricType.INT32, "bytes");

	}

	private void assertAttribute(MBeanAttribute mBeanAttribute, String name,
			String canonicalName, int dMax, GMetricType type, String units) {
		assertEquals(name, mBeanAttribute.getAttributeName());
		assertEquals(canonicalName, mBeanAttribute.getCanonicalName());
		assertEquals(dMax, mBeanAttribute.getDMax());
		assertEquals(type, mBeanAttribute.getType());
		assertEquals(units, mBeanAttribute.getUnits());
	}

	private void testMBeanWithSimpleAttributes(Node mBeanWithSimpleAttributes)
			throws XPathExpressionException, Exception {
		List<MBeanAttribute> mbas = jmxetric.getAttributesForMBean(
				mBeanWithSimpleAttributes, "");

		assertEquals(2, mbas.size());
		assertAttribute(mbas.get(0), "ThreadCount", "ThreadCount.null", 0,
				GMetricType.INT16, "");
		assertAttribute(mbas.get(1), "DaemonThreadCount",
				"DaemonThreadCount.null", 0, GMetricType.INT16, "");
	}

	private void testMBeanWithCompositeAttributes(
			Node mBeanWithCompositeAttributes) throws XPathExpressionException,
			Exception {
		List<MBeanAttribute> mbas = jmxetric.getAttributesForMBean(
				mBeanWithCompositeAttributes, "");
		assertEquals(8, mbas.size());

		assertAttribute(mbas.get(0), "HeapMemoryUsage", "HeapMemoryUsage.init",
				0, GMetricType.INT32, "bytes");

		assertAttribute(mbas.get(0), "HeapMemoryUsage", "HeapMemoryUsage.init",
				0, GMetricType.INT32, "bytes");

		assertAttribute(mbas.get(1), "HeapMemoryUsage",
				"HeapMemoryUsage.committed", 0, GMetricType.INT32, "bytes");

		assertAttribute(mbas.get(2), "HeapMemoryUsage", "HeapMemoryUsage.used",
				0, GMetricType.INT32, "bytes");

		assertAttribute(mbas.get(3), "HeapMemoryUsage", "HeapMemoryUsage.max",
				0, GMetricType.INT32, "bytes");
	}
}
