package sample.core;

import java.io.Closeable;
import java.util.Properties;

import io.aergo.openkeychain.backend.Backend;

public interface BackendProvider extends Closeable {
	
	public Backend getBackend();
	
	public Properties getConfig();
	
	public Properties getScratch();
	
	
	public static BackendProvider defaultProvider = new AergoBackendProvider();

}
