package info.ganglia.jmxetric;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineArgs {
	public static final String DEFAULT_CONFIG = "jmxetric.xml";
	private final static Pattern pattern = Pattern.compile("(\\S+?)\\=(\\S*)");

	private String host = null;
	private String port = null;
	private String config = null;
	private String mode = null;
	private String wireformat = null;
	private String processName = null;
	private String spoof = null;

	public CommandLineArgs(String arguments) {
		String commandLine = arguments == null ? "" : arguments;
		String[] args = commandLine.split(",");

		host = getTagValue("host", args, null);
		port = getTagValue("port", args, null);
		config = getTagValue("config", args, DEFAULT_CONFIG);
		mode = getTagValue("mode", args, null);
		wireformat = getTagValue("wireformat31x", args, null);
		processName = getTagValue("process", args, null);
		spoof = getTagValue("spoof", args, null);
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public String getConfig() {
		return config;
	}

	public String getMode() {
		return mode;
	}

	public String getWireformat() {
		return wireformat;
	}

	public String getProcessName() {
		return processName;
	}

	public String getSpoof() {
		return spoof;
	}

	/*
	 * Parses the string array, input, looking for a pattern tag=value
	 * 
	 * @param tag the tag to search for
	 * 
	 * @param input the array list
	 * 
	 * @param defaultValue the default value if tag is not found
	 * 
	 * @return tha value
	 */
	private String getTagValue(String tag, String[] input, String defaultValue) {
		for (String arg : input) {
			Matcher matcher = CommandLineArgs.pattern.matcher(arg);
			// Get tagname and contents of tag
			if (matcher.find()) {
				String tagname = matcher.group(1);
				if (tag.equals(tagname)) {
					return matcher.group(2);
				}
			}
		}
		return defaultValue;
	}
}
