package info.ganglia.jmxetric;

import static org.junit.Assert.*;

import info.ganglia.gmetric4j.Publisher;
import info.ganglia.gmetric4j.gmetric.GMetricSlope;
import info.ganglia.gmetric4j.gmetric.GMetricType;
import info.ganglia.gmetric4j.gmetric.GangliaException;
import info.ganglia.jmxetric.MBeanSampler;

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
				String value, GMetricType type, GMetricSlope slope, String units)
				throws GangliaException {
			results.put(attributeName, value);
		}
		
		@Override
		public void publish(String processName, String attributeName,
				String value, GMetricType type, GMetricSlope slope, int delay,
				String units) throws GangliaException {
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
        MBeanSampler sampler = new MBeanSampler(0, 30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Long", GMetricType.INT32, 
        		"bytes", GMetricSlope.BOTH, "Longer");
        sampler.run() ;
        String value = publisher.getResult("Longer");
        assertEquals( Example.LONG_VALUE, Long.valueOf(value ));
    }
    /**
     * Test of attribute sample, type String
     */
    @Test
    public void sampleComposite() throws Exception {
        MBeanSampler sampler = new MBeanSampler(0, 30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        
        String compNamePublishName = "compName";
        sampler.addMBeanAttribute(BEAN_NAME, 
        						"Composite",
        						"name", 
        						GMetricType.STRING, 
        						"bytes", 
        						GMetricSlope.BOTH, 
        						compNamePublishName);
        
        String compIntPublishName = "compInt";
        sampler.addMBeanAttribute(BEAN_NAME, 
        							"Composite",
        							"integer", 
        							GMetricType.STRING, 
        							"bytes", 
        							GMetricSlope.BOTH, 
        							compIntPublishName);
        
        String compDatePublishName = "compDate";
        sampler.addMBeanAttribute(BEAN_NAME, 
									"Composite",
									"date", 
									GMetricType.STRING, 
									"bytes", 
									GMetricSlope.BOTH, 
									compDatePublishName);
        
        sampler.run();
        
        assertEquals( ExampleComposite.STRING_VALUE, 
        				publisher.getResult(compNamePublishName) );
        
        assertEquals( "" + ExampleComposite.INT_VALUE, 
        				publisher.getResult(compIntPublishName) );
        
        assertEquals( "" + ExampleComposite.DATE_VALUE, 
        				publisher.getResult(compDatePublishName) );
    }
    /**
     * Test of attribute sample, type int, slope positive
     */
    @Test
    public void sampleCounter() throws Exception {
        MBeanSampler sampler = new MBeanSampler(0, 1, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Counter", GMetricType.INT32,
        		"units", GMetricSlope.POSITIVE, "counter");
        sampler.run() ;
        String value = publisher.getResult("counter");
        assertTrue( Integer.valueOf(value) >= 0);
    }
    
}
