package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Configures JMXetric using an XML file. The XML file is read and MBeanSamplers
 * are created based on what the file specifies. These MBeanSamplers are then
 * added to the JMXetricAgent.
 */
public class JMXetricXmlConfigurationService extends XMLConfigurationService {
  private static Logger log = Logger.getLogger(JMXetricAgent.class.getName());

  /**
   * agent that is configured using the XML file
   */
  private JMXetricAgent agent;

  /**
   * XML configuration source
   */
  private InputSource inputSource;

  /**
   * name that is associated with all declared metrics
   */
  private String processName;

  public JMXetricXmlConfigurationService(JMXetricAgent agent,
      InputSource inputSource, String processName) {
    this.agent = agent;
    this.inputSource = inputSource;
    this.processName = processName;
  }

  /**
   * Configures {@link info.ganglia.jmxetric.JMXetricAgent} using XML source
   * 
   * @throws Exception
   */
  void configure() throws Exception {
    configureProcessName();
    configureJMXetricAgent();
  }

  /**
   * The XML file may specify a processName to be used, which can be different
   * from the one used in the constructor. The name in XML takes priority.
   * 
   * @throws XPathExpressionException
   */
  private void configureProcessName() throws XPathExpressionException {
    if (processName != null) {
      return;
    }
    processName = "";
    Node jvm = getXmlNode("/jmxetric-config/jvm", inputSource);
    if (jvm != null) {
      processName = jvm.getAttributes().getNamedItem("process").getNodeValue();
    }
  }

  /**
   * Use the XML source to configure the agent. Source is read for <sample>
   * nodes, each node corresponds to a MBeanSampler to be added to the agent.
   * 
   * @throws XPathExpressionException
   * @throws Exception
   */
  private void configureJMXetricAgent() throws XPathExpressionException,
      Exception {
    // Gets the config for the samplers
    NodeList samples = getXmlNodeList("/jmxetric-config/sample", inputSource);
    for (int i = 0; i < samples.getLength(); i++) {
      Node sample = samples.item(i);
      MBeanSampler mbSampler = makeMBeanSampler(sample);
      agent.addSampler(mbSampler);
    }
  }

  /**
   * A <sample> node from the XML source is parsed to make an MBeanSampler.
   * 
   * @param sample
   * @return
   * @throws Exception
   */
  private MBeanSampler makeMBeanSampler(Node sample) throws Exception {
    String delayString = selectParameterFromNode(sample, "delay", "60");
    int delay = Integer.parseInt(delayString);

    String initialDelayString = selectParameterFromNode(sample, "initialdelay",
        "0");
    int initialDelay = Integer.parseInt(initialDelayString);

    String sampleDMax = selectParameterFromNode(sample, "dmax", "0");

    MBeanSampler mBeanSampler = new MBeanSampler(initialDelay, delay,
        processName);

    NodeList mBeanNodes = getXmlNodeSet("mbean", sample);
    addMBeansToSampler(mBeanNodes, mBeanSampler, sampleDMax);

    return mBeanSampler;
  }

  private void addMBeansToSampler(NodeList mBeanNodes,
      MBeanSampler mBeanSampler, String sampleDMax) throws Exception {
    for (int j = 0; j < mBeanNodes.getLength(); j++) {
      Node mBean = mBeanNodes.item(j);
      String _mBeanName = selectParameterFromNode(mBean, "name", null);
      Set<ObjectName> mbeanNames = expandMBeanName(_mBeanName);
      if (mbeanNames == null) {
        continue;
      }
      for (ObjectName objectName : mbeanNames) {
        String mBeanName = objectName.getCanonicalName();

        List<MBeanAttribute> attributes = getAttributesForMBean(mBeanName,
            mBean, sampleDMax);
        for (MBeanAttribute mBeanAttribute : attributes) {
          addMBeanAttributeToSampler(mBeanSampler, mBeanName, mBeanAttribute);
        }

      }
    }
  }

  /**
   * Gets a list of MBeanAttributes for a single <mbean>, they correspond to the
   * <attribute> tags in the XML file.
   * 
   * @param mBean
   *          the <mbean> node
   * @param sampleDMax
   *          value of dmax passed down from <sample>
   * @return a list of attributes associated to the mbean
   * @throws Exception
   */
  List<MBeanAttribute> getAttributesForMBean(String mBeanQueriedName,
      Node mBean, String sampleDMax) throws Exception {
    String mBeanName = selectParameterFromNode(mBean, "name", null);

    String mBeanPublishName = buildPname(mBeanQueriedName, mBeanName,
        selectParameterFromNode(mBean, "pname", ""));

    String mBeanDMax = selectParameterFromNode(mBean, "dmax", sampleDMax);
    log.finer("Mbean is " + mBeanName);

    NodeList attrs = getXmlNodeSet("attribute", mBean);
    List<MBeanAttribute> attributes = new Vector<MBeanAttribute>();

    for (int i = 0; i < attrs.getLength(); i++) {
      Node attr = attrs.item(i);
      if (isComposite(attr)) {
        attributes.addAll(makeCompositeAttributes(attr, mBeanName,
            mBeanPublishName, mBeanDMax));
      } else {
        attributes.add(makeSimpleAttribute(attr, mBeanName, mBeanPublishName,
            mBeanDMax));
      }
    }
    return attributes;
  }

  /**
   * Builds a unique pname for an mbean based on its fully qualified mbean,
   * retrieved from the MBean server, the name given in the config, and it's
   * pname from the config.
   * 
   * @param mBeanQueriedName
   * @param mBeanName
   * @param mBeanPublishName
   * @return
   */
  static String buildPname(String mBeanQueriedName, String mBeanName,
      String mBeanPublishName) {
    int indexOfWildcard = mBeanName.indexOf('*');
    if (mBeanQueriedName.isEmpty() || indexOfWildcard == -1) {
      return mBeanPublishName;
    }
    int endIndex = mBeanQueriedName.length()
        - (mBeanName.length() - mBeanName.indexOf('*') - 1);

    String fullPName = mBeanPublishName.concat(mBeanQueriedName.substring(
        indexOfWildcard, endIndex));
    return fullPName;
  }

  /**
   * Checks if the node is an attribute containing composites
   * 
   * @param node
   * @return true if the node is an attribute containing composites
   */
  private boolean isComposite(Node node) {
    return node.getNodeName().equals("attribute")
        && node.getChildNodes().getLength() > 0;
  }

  /**
   * Makes a {@link MBeanAttribute} that corresponds to a simple <attribute>
   * tag.
   * 
   * @param attr
   *          the <attribute> node
   * @param mBeanName
   *          name of the parent mbean
   * @param mBeanPublishName
   *          publish name of the parent mbean
   * @param mBeanDMax
   *          value of dmax specified by parent mbean
   * @return
   */
  private MBeanAttribute makeSimpleAttribute(Node attr, String mBeanName,
      String mBeanPublishName, String mBeanDMax) {
    MBeanAttribute mba = makeMBeanSimpleAttribute(attr, mBeanName,
        mBeanPublishName, mBeanDMax);
    return mba;
  }

  /**
   * Makes a list of {@link MBeanAttribute} that corresponds to an <attribute>
   * node that contains multiple <composite> nodes.
   * 
   * @param attr
   *          the <attribute> node
   * @param mBeanName
   *          name of the parent mbean
   * @param mBeanPublishName
   *          publish name of the parent mbean
   * @param mBeanDMax
   *          value of dmax specified by parent mbean
   * @return list of {@link MBeanAttribute}, one for each <composite>
   * @throws XPathExpressionException
   */
  private List<MBeanAttribute> makeCompositeAttributes(Node attr,
      String mBeanName, String mBeanPublishName, String mBeanDMax)
      throws XPathExpressionException {
    List<MBeanAttribute> mbas = new Vector<MBeanAttribute>();
    MBeanAttribute mba = null;
    NodeList composites = getXmlNodeSet("composite", attr);
    String name = selectParameterFromNode(attr, "name", "NULL");
    for (int l = 0; l < composites.getLength(); l++) {
      Node composite = composites.item(l);
      mba = makeMBeanCompositeAttribute(composite, mBeanName, mBeanPublishName,
          mBeanDMax, name);
      log.finer("Attr is " + name);
      mbas.add(mba);
    }
    return mbas;
  }

  private MBeanAttribute makeMBeanSimpleAttribute(Node attr, String mBeanName,
      String mBeanPublishName, String mBeanDMax) {
    return makeMBeanAttribute(attr, mBeanName, mBeanPublishName, mBeanDMax,
        null);
  }

  private MBeanAttribute makeMBeanCompositeAttribute(Node composite,
      String mBeanName, String mBeanPublishName, String mBeanDMax,
      String attrName) {
    return makeMBeanAttribute(composite, mBeanName, mBeanPublishName,
        mBeanDMax, attrName);
  }

  private MBeanAttribute makeMBeanAttribute(Node attr, String mBeanName,
      String mBeanPublishName, String mBeanDMax, String attrName) {
    String name = selectParameterFromNode(attr, "name", "NULL");
    String units = selectParameterFromNode(attr, "units", "");
    String pname = selectParameterFromNode(attr, "pname", "");
    String slope = selectParameterFromNode(attr, "slope", "");
    String dMax = selectParameterFromNode(attr, "dmax", mBeanDMax);
    String type = selectParameterFromNode(attr, "type", "");
    GMetricType gType = GMetricType.valueOf(type.toUpperCase());
    GMetricSlope gSlope = GMetricSlope.valueOf(slope.toUpperCase());
    int dMaxInt = parseDMax(dMax);
    String metricName = buildMetricName(processName, mBeanName,
        mBeanPublishName, name, pname);

    if (attrName == null) {
      return new MBeanAttribute(processName, name, null, gType, units, gSlope,
          metricName, dMaxInt);
    } else {
      return new MBeanAttribute(processName, attrName, name, gType, units,
          gSlope, metricName, dMaxInt);
    }
  }

  /**
   * Parses dMaxString, which is the value of dmax read in from a configuration
   * file.
   * 
   * @param dMaxString
   *          value read in from configuration
   * @return int value of dMaxString if parse is successful, 0 (default value)
   *         otherwise
   */
  private int parseDMax(String dMaxString) {
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
   * 
   * @param process
   *          the process name, or null if not used
   * @param mbeanName
   *          the mbean name
   * @param mbeanPublishName
   *          the mbean publish name, or null if not used
   * @param attribute
   *          the mbean attribute name
   * @param attrPublishName
   *          the mbean attribute publish name
   * @return the metric name
   */
  private String buildMetricName(String process, String mbeanName,
      String mbeanPublishName, String attribute, String attrPublishName) {
    StringBuilder buf = new StringBuilder();
    if (process != null) {
      buf.append(process);
      buf.append("_");
    }
    if (mbeanPublishName != null) {
      buf.append(mbeanPublishName);
    } else {
      buf.append(mbeanName);
    }
    buf.append("_");
    if (!"".equals(attrPublishName)) {
      buf.append(attrPublishName);
    } else {
      buf.append(attribute);
    }
    return buf.toString();
  }

  private static Set<ObjectName> expandMBeanName(String mbeanName) {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      Set<ObjectName> names = mbs.queryNames(new ObjectName(mbeanName), null);
      return names;
    } catch (MalformedObjectNameException e) {
      log.info("MBean name given in XML is malformed: " + mbeanName);
    }
    return null;
  }

  /**
   * Adds a MBeanAttribute to an MBeanSampler. This also checks if the
   * MBeanAttribute to be added already has a MBeanSampler set, if not it will
   * set it.
   * 
   * @param mBeanSampler
   * @param mBeanName
   * @param mBeanAttribute
   * @throws Exception
   */
  private void addMBeanAttributeToSampler(MBeanSampler mBeanSampler,
      String mBeanName, MBeanAttribute mBeanAttribute) throws Exception {
    if (mBeanAttribute.getSampler() == null) {
      mBeanAttribute.setSampler(mBeanSampler);
    }
    mBeanSampler.addMBeanAttribute(mBeanName, mBeanAttribute);
  }

}