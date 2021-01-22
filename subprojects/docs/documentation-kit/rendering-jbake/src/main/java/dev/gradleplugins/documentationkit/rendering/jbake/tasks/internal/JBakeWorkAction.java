package dev.gradleplugins.documentationkit.rendering.jbake.tasks.internal;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.MapProperty;
import org.gradle.execution.MultipleBuildFailures;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.jbake.app.Oven;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.app.configuration.JBakeConfigurationFactory;

import java.io.File;
import java.util.HashMap;

public abstract class JBakeWorkAction implements WorkAction<JBakeWorkAction.Parameters> {
	private static final Logger LOGGER = Logging.getLogger(JBakeWorkAction.class);

	@Override
	public void execute() {
		val jbake = new Oven(jbakeConfiguration(getParameters()));
		jbake.bake();
		val errors = jbake.getErrors();
		if (!errors.isEmpty()) {
			errors.forEach(it -> LOGGER.error(it.getMessage()));
			throw new IllegalStateException(new MultipleBuildFailures(errors));
		}
	}

	private static JBakeConfiguration jbakeConfiguration(Parameters parameters) {
		val factory = new JBakeConfigurationFactory();
		return factory.createDefaultJbakeConfiguration(
			sourceDirectory(parameters),
			destinationDirectory(parameters),
			configuration(parameters),
			false);
	}

	private static File sourceDirectory(Parameters parameters) {
		return parameters.getSourceDirectory().get().getAsFile();
	}

	private static File destinationDirectory(Parameters parameters) {
		return parameters.getDestinationDirectory().get().getAsFile();
	}

	@SneakyThrows
	private static CompositeConfiguration configuration(Parameters parameters) {
		val result = new CompositeConfiguration();
		result.addConfiguration(new MapConfiguration(new HashMap<>(parameters.getConfigurations().get())));
		result.addConfiguration(defaultConfiguration(parameters));
		return result;
	}

	@SneakyThrows
	private static Configuration defaultConfiguration(Parameters parameters) {
		return ((DefaultJBakeConfiguration)new ConfigUtil().loadConfig(parameters.getSourceDirectory().get().getAsFile())).getCompositeConfiguration();
	}

	public interface Parameters extends WorkParameters {
		DirectoryProperty getSourceDirectory();
		DirectoryProperty getDestinationDirectory();
		MapProperty<String, Object> getConfigurations();
	}
}
