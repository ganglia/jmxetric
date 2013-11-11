package info.ganglia.jmxetric;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * MXBean implementation used for testing
 * @author humphrej
 *
 */
public class Example implements TestExampleMXBean {
	private AtomicInteger counter = new AtomicInteger(0);
	private ScheduledExecutorService executor = null ;
	
	public Example() {
		executor = Executors.newSingleThreadScheduledExecutor( new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread( r );
				t.setName("ExampleMBean");
				return t ;
			}
		});
	}
	
	public void start() {
		executor.scheduleAtFixedRate( new Runnable() {
			public void run() {
				counter.incrementAndGet();
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	public void stop() {
		executor.shutdown();
	}
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
	public int getCounter() {
		return counter.get();
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
