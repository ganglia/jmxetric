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

		public void publish(String processName, String attributeName, String value, 
				GMetricType type, GMetricSlope slope, String units)
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
        MBeanSampler sampler = new MBeanSampler(0, 30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Long", GMetricType.INT32, 
        		"bytes", GMetricSlope.BOTH, "Longer", 0);
        sampler.run() ;
        String value = publisher.getResult("Longer");
        assertEquals(Example.LONG_VALUE, Long.valueOf(value));
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
        						compNamePublishName,
        						0);
        
        String compIntPublishName = "compInt";
        sampler.addMBeanAttribute(BEAN_NAME, 
        							"Composite",
        							"integer", 
        							GMetricType.STRING, 
        							"bytes", 
        							GMetricSlope.BOTH,
        							compIntPublishName,
        							0);
        
        String compDatePublishName = "compDate";
        sampler.addMBeanAttribute(BEAN_NAME, 
									"Composite",
									"date", 
									GMetricType.STRING, 
									"bytes", 
									GMetricSlope.BOTH, 
									compDatePublishName,
									0);
       
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
        		"units", GMetricSlope.POSITIVE, "counter", 0);
        sampler.run() ;
        String value = publisher.getResult("counter");
        assertTrue( Integer.valueOf(value) >= 0);
    }
    
    /**
     * Test of String metric incorrectly marked as rate
     */
    @Test
    public void stringRateTest() throws Exception {
        MBeanSampler sampler = new MBeanSampler(0, 30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "String", GMetricType.STRING, 
        		"bytes", GMetricSlope.BOTH, "String_Rate", 1);
        sampler.run() ;
        sampler.run() ;
        assertEquals(Example.STRING_VALUE, publisher.getResult("String_Rate"));
    }
    
    /**
     * Test of int rate that remains unchanged
     */
    @Test
    public void unchangedIntRateTest() throws Exception {
        MBeanSampler sampler = new MBeanSampler(0, 30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Int", GMetricType.INT16, 
        		"bytes", GMetricSlope.BOTH, "Unchanged_Int_Rate", 1);
        sampler.run() ;
        sampler.addMBeanAttribute(BEAN_NAME, "Int", GMetricType.INT16, 
        		"bytes", GMetricSlope.BOTH, "Unchanged_Int_Rate", 1);
        sampler.run() ;
        String value = publisher.getResult("Unchanged_Int_Rate");
        assertTrue( Integer.valueOf(value) == 0 | Integer.valueOf(value) == Example.INT_VALUE);
    }
    
    /**
     * Test of long, double and float rates read for the first time
     */
    @Test
    public void ratesTest() throws Exception {
        MBeanSampler sampler = new MBeanSampler(0, 30000, "TEST") ;
        MyPublisher publisher = new MyPublisher() ;
        sampler.setPublisher(publisher);
        sampler.addMBeanAttribute(BEAN_NAME, "Long", GMetricType.INT32, 
        		"bytes", GMetricSlope.POSITIVE, "Long_Rate", 1);
        sampler.addMBeanAttribute(BEAN_NAME, "Double", GMetricType.DOUBLE, 
        		"bytes", GMetricSlope.POSITIVE, "Double_Rate", 1);
        sampler.addMBeanAttribute(BEAN_NAME, "Float", GMetricType.FLOAT, 
        		"bytes", GMetricSlope.POSITIVE, "Float_Rate", 1);
        sampler.run() ;
        String longValue = publisher.getResult("Long_Rate");
        assertTrue( Long.valueOf(longValue) == Example.LONG_VALUE);
        String doubleValue = publisher.getResult("Double_Rate");
        assertTrue( Double.valueOf(doubleValue) == Example.DOUBLE_VALUE);
        String floatValue = publisher.getResult("Float_Rate");
        assertTrue( Float.valueOf(floatValue) == Example.FLOAT_VALUE);
    }
}
