package dev.nokee.platform.nativebase.internal.repositories;

import dev.nokee.platform.nativebase.internal.locators.CachingXcRunLocator;
import dev.nokee.platform.nativebase.internal.locators.MacOSSdkPathLocator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class NokeeServerService implements BuildService<BuildServiceParameters.None>, AutoCloseable {
	public static final String NOKEE_LOCAL_REPOSITORY_NAME = "Nokee Local Repository";
	private static final Logger LOGGER = Logger.getLogger(NokeeServerService.class.getName());
	private final Object lock = new Object();
	private final Server server;

	@Inject
	public NokeeServerService() {
		MacOSSdkPathLocator locator = getObjects().newInstance(MacOSSdkPathLocator.class);
		server = new Server(0);
		server.setHandler(new JettyEmbeddedHttpServer(new CachingXcRunLocator(locator)));
		server.setStopAtShutdown(true);
		try {
			server.start();
			LOGGER.info("Nokee server started on port " + server.getURI().getPort());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	public URI getUri() {
		return server.getURI();
	}

	// TODO: Maybe registerIfAbsent(Class...)

	@Override
	public void close() throws Exception {
		synchronized (lock) {
			server.stop();
			server.join(); // TODO Timeout
			server.destroy();
			LOGGER.info("Nokee server stopped");
		}
	}
}
