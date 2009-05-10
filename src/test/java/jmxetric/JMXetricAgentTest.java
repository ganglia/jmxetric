package jmxetric;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class JMXetricAgentTest {

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
            Thread.sleep(1000);
            // TODO Add asserts
        } catch ( Exception ex ) {
            ex.printStackTrace();
            fail("Exception thrown");
        }
	}

}
