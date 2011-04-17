package jmxetric;

import static org.junit.Assert.*;

import ganglia.gmetric.GMetricResult;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class JMXetricAgentIT {

	@Before
	public void setUp() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
		ObjectName name = new ObjectName("jmxetric:type=TestExample"); 
		Example mbean = new Example(); 
		mbs.registerMBean(mbean, name);
	}

	@Test
	public void testRun() {
        JMXetricAgent a = null ;
        try {
            a = new JMXetricAgent();
            XMLConfigurationService.configure(a, "host=localhost,port=8649,wireformat31x=true,config=src/test/resources/jmxetric_test.xml");
            a.start();
            Thread.sleep(5000);
            GMetricResult.GMetricDetail floatResult = GMetricResult.getGMetric("ProcessName_TestExample_Float");
            assertEquals( Float.toString( Example.FLOAT_VALUE) , floatResult.value);
            assertEquals( "both", floatResult.slope);
            // TODO Add asserts
        } catch ( Exception ex ) {
            ex.printStackTrace();
            fail("Exception thrown");
        }
	}

}
