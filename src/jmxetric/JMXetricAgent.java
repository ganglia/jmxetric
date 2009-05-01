package jmxetric;

import ganglia.gmetric.GMetric;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * JMXetricAgent is a JVM agent that will sample MBean attributes on a periodic basis, 
 * publishing the value of those attributes to the Ganglia gmond process.
 * <br>
 * Use:<br>
 * <code>java -javaagent:path/jmxetric.jar=args yourmainclass</code>
 * <br>
 * Example:<br>
 * <code>java -javaagent:/opt/jmxetric_0_1/jmxetric.jar=host="localhost",port="8649",config=/opt/jmxetric_0_1/jmxetric.xml yourmainclass</code>
 * <br>
 * Arguments can be:<br>
 * <table>
 * <tr><th>Argument</th><th>Default</th><th>Description</th></tr>
 * <tr><td>host</td><td></td><td>Host address for ganglia</td></tr>
 * <tr><td>port</td><td></td><td>Port for ganglia</td></tr>
 * <tr><td>config</td><td>jmxetric.xml</td><td>Config file path</td></tr>
 * </table>
 */
public class JMXetricAgent {
    private static Logger log =
      Logger.getLogger(JMXetricAgent.class.getName());
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private List<MBeanSampler> samplers = new ArrayList<MBeanSampler>();
    private boolean daemon = true ;
    private GMetric gmetric = null ;
    private ThreadFactory daemonThreadGroup = new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("JMXetric Sampling Thread");
            t.setDaemon(daemon);
            return t;
        }
    };
    /**
     * Starts the sampling of MBeans
     */    
    public void start() {
        executor.setThreadFactory(daemonThreadGroup);

        for (MBeanSampler s : samplers) {
            executor.scheduleAtFixedRate(s, 0, s.getDelay(), TimeUnit.SECONDS);
        }
    }
    /**
     * Stops the sampling of MBeans
     */
    public void stop() {
        executor.shutdown();
    }
    /**
     * Adds a new MBeanSampler to be sampled
     * @param s the MBeanSampler
     */
    public void addSampler(MBeanSampler s) {
        samplers.add(s);
        s.setPublisher( new GMetricPublisher(gmetric));
    }
    /**
     * Returns the daemon status of the scheduler thread
     * @return true if the scheduler thread is a daemon
     */
    public boolean isDaemon() {
        return daemon;
    }
    /**
     * Sets the scheduler daemon thread to be true/false.  This only has an 
     * effect before the start method is called.
     * @param daemon the requested scheduler daemon status
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }
    public GMetric getGmetric() {
        return gmetric;
    }
    public void setGmetric(GMetric gmetric) {
        this.gmetric = gmetric;
    }
   /**
     * A log running, trivial main method for test purposes
     * premain method
     * @param args Not used
     */
    public static void main(String[] args) throws Exception {
        while( true ) {
           Thread.sleep(1000*60*5);
           System.out.println("Test wakeup");
        }
    }
    /**
     * The JVM agent entry point
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(STARTUP_NOTICE) ;
        JMXetricAgent a = null ;
        try {
            a = new JMXetricAgent();
            XMLConfigurationService.configure(a, agentArgs);
            a.start();
        } catch ( Exception ex ) {
            log.severe("Exception starting JMXetricAgent");
            ex.printStackTrace();
        }
    }
    
    private static final String STARTUP_NOTICE="JMXetricAgent instrumented JVM, see http://code.google.com/p/jmxetric";
}
