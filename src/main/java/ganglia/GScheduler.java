package ganglia;

import java.util.concurrent.TimeUnit;

public interface GScheduler {
	
	public void onStart();
	public void onStop();
	
	public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

}
