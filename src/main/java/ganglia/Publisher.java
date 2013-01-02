package ganglia;

import ganglia.gmetric.GMetricSlope;
import ganglia.gmetric.GMetricType;
import ganglia.gmetric.GangliaException;

public interface Publisher {
	void publish( String processName, String attributeName, 
			String value, GMetricType type, GMetricSlope slope, String units )
				throws GangliaException;
}
