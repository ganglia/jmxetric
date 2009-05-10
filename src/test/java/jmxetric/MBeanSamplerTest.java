package jmxetric;

import static org.junit.Assert.*;
import ganglia.gmetric.GMetricType;
import ganglia.gmetric.GangliaException;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MBeanSamplerTest {

	public static String BEAN_NAME="jmxetric:type=Test.Example";
	private class MyPublisher implements Publisher 
	{
		Map<String, String> results = new HashMap<String,String>();

		public void publish(String processName, String attributeName,
				String value, GMetricType type, String units)
				throws GangliaException {
			results.put(attributeName, value);
		}
		
		public String getResult( String attributeName ) {
			return results.get(attributeName);
		}
		
	}
	@Before
	public void setUp() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
		ObjectName name = new ObjectName(BEAN_NAME); 
		Example mbean = new Example(); 
		mbs.registerMBean(mbean, name);
	}
	@After
	public void tearDown() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
		ObjectName name = new ObjectName(BEAN_NAME);
		mbs.unregisterMBean(name);
	}
    /**
     * Test of attribute sample, type Long
     */
    @Test
    public void sampleLong() throws Exception {
        MBeanSampler sampler = new MBeanSampler(30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Long", GMetricType.INT32, "bytes", "Longer");
        sampler.run() ;
        String value = publisher.getResult("Longer");
        assertEquals( Example.LONG_VALUE, Long.valueOf(value ));
    }
    /**
     * Test of attribute sample, type String
     */
    @Test
    public void sampleComposite() throws Exception {
        MBeanSampler sampler = new MBeanSampler(30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Composite","name", GMetricType.STRING, "bytes", "name");
        sampler.run() ;
        String value = publisher.getResult("name");
        assertEquals( ExampleComposite.STRING_VALUE, value );
    }
    
}
