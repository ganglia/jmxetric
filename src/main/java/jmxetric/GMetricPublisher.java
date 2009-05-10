package jmxetric;

import ganglia.gmetric.GMetric;
import ganglia.gmetric.GMetricSlope;
import ganglia.gmetric.GMetricType;
import ganglia.gmetric.GangliaException;

public class GMetricPublisher implements Publisher {

	private GMetric gm = null ;
	public GMetricPublisher( GMetric gm ) {
		this.gm = gm ;
	}

	public void publish(String processName, String attributeName, String value,
			GMetricType type, String units) throws GangliaException {
        gm.announce(attributeName, value, type, units, 
        		GMetricSlope.BOTH, 60, 0, processName);
	}

}
