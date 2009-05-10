package jmxetric;

import java.beans.ConstructorProperties;
import java.lang.management.ManagementFactory;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * MXBean implementation used for testing
 * @author humphrej
 *
 */
public class TestExample implements TestExampleMXBean {
	@Override
	public double getDouble() {
		return DOUBLE_VALUE ;
	}
	@Override
	public float getFloat() {
		return FLOAT_VALUE;
	}
	@Override
	public int getInt() {
		return INT_VALUE;
	}
	@Override
	public long getLong() {
		return LONG_VALUE;
	}
	@Override
	public String getString() {
		return STRING_VALUE;
	}
	@Override
	public TestComposite getComposite() {
		return new TestComposite(TestComposite.DATE_VALUE, 
				TestComposite.INT_VALUE,
				TestComposite.STRING_VALUE);
	}
	public static void register() {
		try {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
		ObjectName name = new ObjectName("jmxetric:type=TestExample"); 
		TestExample mbean = new TestExample(); 
		mbs.registerMBean(mbean, name);
		} catch (Exception ex ){
			ex.printStackTrace();
		}
	}
}
