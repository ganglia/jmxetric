package jmxetric;

import info.ganglia.gmetric.GMetricSlope;
import info.ganglia.gmetric.GMetricType;
import info.ganglia.gmetric.GangliaException;

public interface Publisher {
	void publish( String processName, String attributeName, 
			String value, GMetricType type, GMetricSlope slope, String units )
				throws GangliaException;
}
