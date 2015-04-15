package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.GMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.LogManager;
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
    private static final String JMETRIX_LOGGING_PROPERTIES="jmxetric.logging.properties";
    private static Logger log = Logger.getLogger(JMXetricAgent.class.getName());
   /**
     * A log running, trivial main method for test purposes
     * premain method
     * @param args Not used
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Started Testing thread");
        while( true ) {
           Thread.sleep(1000*10);
           System.out.print(".");
        }
    }
    /**
     * The JVM agent entry point
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        configureLog();
        log.info(STARTUP_NOTICE);
        log.fine("JAVA_TOOL_OPTIONS=" + System.getenv("JAVA_TOOL_OPTIONS"));

        JMXetricAgent a = null ;
        try {
            a = new JMXetricAgent();
            XMLConfigurationService.configure(a, agentArgs);
            a.start();
        } catch ( Exception ex ) {
            // log.severe("Exception starting JMXetricAgent");
            ex.printStackTrace();
        }
    }

    private static void configureLog() {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(JMETRIX_LOGGING_PROPERTIES);
            log.setLevel(Level.OFF);
            LogManager.getLogManager().readConfiguration(is);
//            log.addHandler(new java.util.logging.ConsoleHandler());
            log.setUseParentHandlers(false);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String STARTUP_NOTICE="JMXetricAgent instrumented JVM, see https://github.com/ganglia/jmxetric";
}
