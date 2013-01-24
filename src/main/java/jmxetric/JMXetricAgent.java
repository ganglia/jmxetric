package jmxetric;


import info.ganglia.GMonitor;

import java.lang.instrument.Instrumentation;
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
public class JMXetricAgent extends GMonitor {
    private static Logger log =
      Logger.getLogger(JMXetricAgent.class.getName());
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
