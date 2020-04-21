package dev.nokee.platform.nativebase.internal.repositories;

import dev.nokee.platform.nativebase.internal.locators.CachingXcRunLocator;
import dev.nokee.platform.nativebase.internal.locators.MacOSSdkPathLocator;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.MultiException;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Logger;

import static dev.nokee.platform.nativebase.internal.ArtifactSerializationTypes.*;
import static dev.nokee.platform.nativebase.internal.ArtifactTypes.ARTIFACT_TYPES_ATTRIBUTE;
import static dev.nokee.platform.nativebase.internal.ArtifactTypes.FRAMEWORK_TYPE;
import static dev.nokee.platform.nativebase.internal.LibraryElements.FRAMEWORK_BUNDLE;

public abstract class NokeeServerService implements BuildService<BuildServiceParameters.None>, AutoCloseable {
	private static final Logger LOGGER = Logger.getLogger(NokeeServerService.class.getName());
	private final Object lock = new Object();
	private final Server server;
	private int port;

	@Inject
	public NokeeServerService() {
		MacOSSdkPathLocator locator = getObjects().newInstance(MacOSSdkPathLocator.class);
		int retryCount = 3;
		MultiException exception = new MultiException();
		Server server = null;
		int port = -1;

		do {
			port = findRandomOpenPortOnAllLocalInterfaces();
			server = new Server();
			server.setHandler(new JettyEmbeddedHttpServer(new CachingXcRunLocator(locator)));
			server.setStopAtShutdown(true);
			ServerConnector connector = new ServerConnector(server);
			connector.setReuseAddress(true);
			connector.setPort(port);
			server.addConnector(connector);
			try {
				server.start();
				LOGGER.info("Nokee server started on port " + port);
			} catch (Exception e) {
				exception.add(e);
				retryCount--;
				LOGGER.warning("Failed starting the Nokee server with: " + e.getMessage());
				try {
					server.stop();
					server.join();
					server.destroy();
				} catch (Exception ee) {
					exception.add(ee);
					exception.ifExceptionThrowRuntime(); // Give up, too many errors
				}
			}
		} while(!server.isStarted() && retryCount != 0);

		if (retryCount == 0) {
			exception.ifExceptionThrowRuntime(); // Will certainly throw
		}

		this.server = server;
		this.port = port;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	private int findRandomOpenPortOnAllLocalInterfaces() {
		try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getByName(null))) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException("Wat");
		}
	}

	@Override
	public void close() throws Exception {
		synchronized (lock) {
			server.stop();
			server.join(); // TODO Timeout
			server.destroy();
			LOGGER.info("Nokee server stopped");
		}
	}

	public void configure(RepositoryHandler repositories) {
		repositories.maven(repo -> {
			repo.setUrl("http://localhost:" + port);
			repo.metadataSources(MavenArtifactRepository.MetadataSources::gradleMetadata);
			repo.mavenContent(content -> {
				content.includeGroup("dev.nokee.framework");
			});
		});
	}

	public void configure(DependencyHandler dependencies) {
		dependencies.artifactTypes(it -> {
			it.create("localpath", type -> type.getAttributes().attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED));
			it.create(FRAMEWORK_TYPE);
		});
		dependencies.registerTransform(DeserializeLocalFramework.class, variantTransform -> {
			variantTransform.getFrom()
				.attribute(ARTIFACT_TYPES_ATTRIBUTE, "localpath")
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED);
			variantTransform.getTo()
				.attribute(ARTIFACT_TYPES_ATTRIBUTE, FRAMEWORK_TYPE)
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, DESERIALIZED);
		});
	}

	public static abstract class DeserializeLocalFramework implements TransformAction<TransformParameters.None> {
		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		public void transform(TransformOutputs outputs) {
			try {
				String s = FileUtils.readFileToString(getInputArtifact().get().getAsFile(), Charset.defaultCharset());
				File framework = new File(s);
				File o = outputs.dir(framework.getName());
				if (!o.delete()) {
					throw new RuntimeException("Can't delete file");
				}
				Files.createSymbolicLink(o.toPath(), framework.toPath());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
