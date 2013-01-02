package ganglia;

import java.util.logging.Logger;

import ganglia.gmetric.GMetricSlope;
import ganglia.gmetric.GMetricType;
import ganglia.gmetric.GangliaException;

public class CoreSampler extends GSampler {
	
	private static Logger log =
        Logger.getLogger(CoreSampler.class.getName());
	
	public CoreSampler() {
		super(0, 30, "core");
	}

	@Override
	public void run() {
		Publisher gm = getPublisher();
        log.finer("Announcing heartbeat");
        try {
			gm.publish("core", "heartbeat", "0", GMetricType.UINT32, GMetricSlope.ZERO, "");
		} catch (GangliaException e) {
			log.severe("Exception while sending heartbeat");
			e.printStackTrace();
		}

	}

}
