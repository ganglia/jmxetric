package jmxetric;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * MXBean implementation used for testing
 * @author humphrej
 *
 */
public class Example implements TestExampleMXBean {
	public double getDouble() {
		return DOUBLE_VALUE ;
	}
	public float getFloat() {
		return FLOAT_VALUE;
	}
	public int getInt() {
		return INT_VALUE;
	}
	public long getLong() {
		return LONG_VALUE;
	}
	public String getString() {
		return STRING_VALUE;
	}
	public ExampleComposite getComposite() {
		return new ExampleComposite(ExampleComposite.DATE_VALUE, 
				ExampleComposite.INT_VALUE,
				ExampleComposite.STRING_VALUE);
	}
	public static void register() {
		try {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
		ObjectName name = new ObjectName("jmxetric:type=TestExample"); 
		Example mbean = new Example(); 
		mbs.registerMBean(mbean, name);
		} catch (Exception ex ){
			ex.printStackTrace();
		}
	}
}
