package jmxetric;

import ganglia.gmetric.GMetricType;
import ganglia.gmetric.GangliaException;

public interface Publisher {
	void publish( String processName, String attributeName, 
			String value, GMetricType type, String units ) throws GangliaException;
}
