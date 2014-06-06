package info.ganglia.jmxetric;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandLineArgsTest {
	private CommandLineArgs commandLineArgs;

	/**
	 * IMPT: Take note of this special case. When the `config` parameter is
	 * missing from the command line, we cannot return null. We need a default
	 * location for the XML file to be able to read configuration from.
	 */
	private static final String DEFAULT_CONFIG = "jmxetric.xml";

	@Test
	public void testCommandLineArgs_emptyOrNullArguments() {
		commandLineArgs = new CommandLineArgs(null);
		assertCommandLineArgs(null, null, DEFAULT_CONFIG, null, null, null,
				null);

		commandLineArgs = new CommandLineArgs("");
		assertCommandLineArgs(null, null, DEFAULT_CONFIG, null, null, null,
				null);
	}

	@Test
	public void testCommandLineArgs_allParamsPresent() {
		String args = "host=localhost,port=8649,config=etc/jmxetric.xml,mode=multicast,wireformat31x=true,process=ProcessName,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", "8649", "etc/jmxetric.xml",
				"multicast", "true", "ProcessName", "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingHost() {
		String args = "port=8649,config=etc/jmxetric.xml,mode=multicast,wireformat31x=true,process=ProcessName,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs(null, "8649", "etc/jmxetric.xml", "multicast",
				"true", "ProcessName", "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingPort() {
		String args = "host=localhost,config=etc/jmxetric.xml,mode=multicast,wireformat31x=true,process=ProcessName,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", null, "etc/jmxetric.xml",
				"multicast", "true", "ProcessName", "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingConfig() {
		String args = "host=localhost,port=8649,mode=multicast,wireformat31x=true,process=ProcessName,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", "8649", DEFAULT_CONFIG, "multicast",
				"true", "ProcessName", "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingMode() {
		String args = "host=localhost,port=8649,config=etc/jmxetric.xml,wireformat31x=true,process=ProcessName,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", "8649", "etc/jmxetric.xml", null,
				"true", "ProcessName", "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingWireformat() {
		String args = "host=localhost,port=8649,config=etc/jmxetric.xml,mode=multicast,process=ProcessName,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", "8649", "etc/jmxetric.xml",
				"multicast", null, "ProcessName", "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingProcessName() {
		String args = "host=localhost,port=8649,config=etc/jmxetric.xml,mode=multicast,wireformat31x=true,spoof=SpoofName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", "8649", "etc/jmxetric.xml",
				"multicast", "true", null, "SpoofName");
	}

	@Test
	public void testCommandLineArgs_missingSpoofName() {
		String args = "host=localhost,port=8649,config=etc/jmxetric.xml,mode=multicast,wireformat31x=true,process=ProcessName";
		commandLineArgs = new CommandLineArgs(args);
		assertCommandLineArgs("localhost", "8649", "etc/jmxetric.xml",
				"multicast", "true", "ProcessName", null);
	}

	private void assertCommandLineArgs(String host, String port, String config,
			String mode, String wireformat, String processName, String spoof) {
		assertEquals(host, commandLineArgs.getHost());
		assertEquals(port, commandLineArgs.getPort());
		assertEquals(config, commandLineArgs.getConfig());
		assertEquals(mode, commandLineArgs.getMode());
		assertEquals(wireformat, commandLineArgs.getWireformat());
		assertEquals(processName, commandLineArgs.getProcessName());
		assertEquals(spoof, commandLineArgs.getSpoof());
	}

}
