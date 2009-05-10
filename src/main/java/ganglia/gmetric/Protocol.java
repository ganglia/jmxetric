package ganglia.gmetric;

/**
 * An interface to be implemented by protocol implementations
 *
 */
public interface Protocol {

	/**
	 * Announces a metric
	 * @param name the metric name
	 * @param value the metric value
	 * @param type the metric type
	 * @param units the units
	 * @param slope the slope
	 * @param tmax the tmax
	 * @param dmax the dmax
	 * @param groupName the metric group
	 * @throws Exception
	 */
	void announce( String name, 
	            String value, 
	            GMetricType type,
	            String units,
	            GMetricSlope slope,
	            int tmax,
	            int dmax,
	            String groupName ) throws Exception ;
	
}
