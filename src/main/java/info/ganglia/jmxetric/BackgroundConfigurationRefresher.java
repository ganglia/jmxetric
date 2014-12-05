package info.ganglia.jmxetric;

import info.ganglia.gmetric4j.gmetric.GMetric;
import org.xml.sax.InputSource;

import org.w3c.dom.Node;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;


public class BackgroundConfigurationRefresher extends XMLConfigurationService {

    private JMXetricAgent agent = null;
    private final InputSource inputSource;
    private final CommandLineArgs args;
    private final String agentArgs;
    private int pollingFrequency;
    private int lastHash = -1;

    public BackgroundConfigurationRefresher(String agentArgs) {
        this.agentArgs = agentArgs;
        this.args = new CommandLineArgs(agentArgs);
        this.inputSource = new InputSource(args.getConfig());
    }

    public void initialize() throws Exception {
        Node root = getXmlNode("/jmxetric-config/refreshconfig", inputSource);

        startNewAgent();

        if (isEnabled(root)) {
            pollingFrequency = getPollingFrequencySeconds(root);
            startBackgroundThread();
        }
    }

    private void startNewAgent() throws Exception {
        if (agent != null) {
            agent.stop();
            GMetric gmetric = agent.getGmetric();
            if (gmetric != null) {
                gmetric.close();
            }
            agent = null;
        }

        agent = new JMXetricAgent();
        XMLConfigurationService.configure(agent, agentArgs);
        agent.start();
        lastHash = getConfigHash();
    }

    private boolean isEnabled(Node root) {
        return selectParameterFromNode(root, "enabled", "false").toLowerCase().equals("true");
    }

    private int getPollingFrequencySeconds(Node root) {
        return Integer.parseInt(selectParameterFromNode(root, "frequency", "5"));
    }

    private int getConfigHash() throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        File theFile = new File(args.getConfig());
        InputStream is = new FileInputStream(theFile);

        DigestInputStream dis = new DigestInputStream(is, md);
        while (dis.read() != -1) {
            // Place holder to read the entire file
        }

        byte[] digest = md.digest();
        return new String(digest).hashCode();
    }

    private void startBackgroundThread() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    int currentHash = getConfigHash();
                    if (currentHash != lastHash) {
                        System.out.println("The file has changed.  Initializing a dynamic reload for the instance.");
                        startNewAgent();
                    }
                } catch (FileNotFoundException e) {
                    System.out.println(String.format("Could not find the config file: %s", args.getConfig()));
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("Your system does not contain the MD5 digest algorithm.");
                } catch (Exception e) {
                    System.out.println("Unknown exception encountered.");
                    e.printStackTrace();
                }
            }
        };

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, pollingFrequency * 1000, pollingFrequency * 1000);
    }

}
