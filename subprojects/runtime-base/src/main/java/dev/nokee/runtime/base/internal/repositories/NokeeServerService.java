package dev.nokee.runtime.base.internal.repositories;

import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class NokeeServerService implements BuildService<NokeeServerService.Parameters>, AutoCloseable {
	public static final String NOKEE_LOCAL_REPOSITORY_NAME = "Nokee Local Repository";
	private static final Logger LOGGER = Logger.getLogger(NokeeServerService.class.getName());
	private final Object lock = new Object();
	private final Server server;

	@Inject
	public NokeeServerService() {
		ToolRepository toolRepository = newToolRepository();
		Map<String, RouteHandler> routeMapping = getParameters().getRouteHandlers().get().stream().map(it -> getObjects().newInstance(toClass(AbstractRouteHandler.class, it), toolRepository)).collect(Collectors.toMap(NokeeServerService::getContextPath, Function.identity()));

		LOGGER.info(() -> String.format("Nokee server will handle %d routes:%n%s", routeMapping.size(), routeMapping.entrySet().stream().map(it -> " * Routing " + it.getKey() + " to " + it.getValue().getClass().getCanonicalName()).collect(Collectors.joining("\n"))));

		server = new Server(0);
		server.setHandler(new JettyEmbeddedHttpServer(routeMapping));
		server.setStopAtShutdown(true);
		try {
			server.start();
			LOGGER.info("Nokee server started on port " + server.getURI().getPort());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ToolRepository newToolRepository() {
		ToolRepository result = new ToolRepository();
		getParameters().getToolLocators().get().stream().map(it -> getObjects().newInstance(toClass(CommandLineToolLocator.class, it))).forEach(locator -> {
			locator.getKnownTools().forEach(knownTool -> {
				result.register(knownTool, locator);
			});
		});

		return result;
	}

	// Extract the context path
	private static String getContextPath(AbstractRouteHandler route) {
		return route.getContextPath();
	}

	// Rehydrate the route handler
	private static <T> Class<T> toClass(Class<T> baseType, String classname) {
		try {
			Class<?> clazz = Class.forName(classname);
			if (baseType.isAssignableFrom(clazz)) {
				return Cast.uncheckedCast(clazz);
			}
			throw new RuntimeException("Bad class base");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@SneakyThrows
	public URI getUri() {
		URI uri = server.getURI();
		return new URI(uri.getScheme(), uri.getUserInfo(), "127.0.0.1", uri.getPort(), uri.getPath(), uri.getFragment(), uri.getQuery());
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

	public interface Parameters extends BuildServiceParameters {
		SetProperty<String> getRouteHandlers();
		SetProperty<String> getToolLocators();
	}
}
