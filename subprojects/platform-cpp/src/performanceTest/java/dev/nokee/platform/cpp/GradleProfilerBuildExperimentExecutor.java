/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.cpp;

import dev.gradleplugins.runnerkit.distributions.VersionAwareGradleDistribution;
import org.gradle.internal.UncheckedException;
import org.gradle.profiler.BuildAction;
import org.gradle.profiler.DaemonControl;
import org.gradle.profiler.GradleBuildConfiguration;
import org.gradle.profiler.GradleBuildInvocationResult;
import org.gradle.profiler.GradleBuildInvoker;
import org.gradle.profiler.GradleScenarioDefinition;
import org.gradle.profiler.GradleScenarioInvoker;
import org.gradle.profiler.InvocationSettings;
import org.gradle.profiler.Logging;
import org.gradle.profiler.RunTasksAction;
import org.gradle.profiler.ScenarioDefinition;
import org.gradle.profiler.ScenarioInvoker;
import org.gradle.profiler.ToolingApiGradleClient;
import org.gradle.profiler.instrument.PidInstrumentation;
import org.gradle.profiler.report.CsvGenerator;
import org.gradle.profiler.result.BuildInvocationResult;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.platform.cpp.GradleProfilerConfigurationTimeMeasurement.configurationTimeMeasurement;
import static dev.nokee.platform.cpp.GradleProfilerGarbageCollectionMeasurement.garbageCollectionMeasurement;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

final class GradleProfilerBuildExperimentExecutor implements BuildExperimentExecutor<GradleInvocationSpec> {
	private static final String GRADLE_USER_HOME_NAME = "gradleUserHome";
	private final PidInstrumentation pidInstrumentation;
	private final GradleProfilerBuildExperimentExecutorContext context;

	GradleProfilerBuildExperimentExecutor(GradleProfilerBuildExperimentExecutorContext context) {
		this.context = context;
		try {
			this.pidInstrumentation = new PidInstrumentation();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BuildExperimentExecutionResult run(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
		List<String> additionalJvmOpts = new ArrayList<>();
		List<String> additionalArgs = new ArrayList<>();
		additionalArgs.add("-PbuildExperimentDisplayName=" + context.getDisplayName());

//		GradleInvocationSpec buildSpec = invocation.withAdditionalJvmOpts(additionalJvmOpts).withAdditionalArgs(additionalArgs);

//		GradleBuildExperimentSpec gradleExperiment = (GradleBuildExperimentSpec) experiment;
		InvocationSettings invocationSettings = createInvocationSettings(parameters);
		GradleScenarioDefinition scenarioDefinition = createScenarioDefinition(parameters, invocationSettings);

		List<GradleBuildInvocationResult> results = new ArrayList<>();

		File workingDirectory = parameters.getWorkingDirectory().get();
		// TODO: scenario invoker should be in try catch???
		GradleScenarioInvoker scenarioInvoker = createScenarioInvoker(invocationSettings.getGradleUserHome());
		try {
			Logging.setupLogging(workingDirectory);
//			if (buildSpec.isUseAndroidStudio()) {
//				StudioGradleScenarioDefinition studioScenarioDefinition = new StudioGradleScenarioDefinition(scenarioDefinition, buildSpec.getStudioJvmArgs());
//				StudioGradleScenarioInvoker studioScenarioInvoker = new StudioGradleScenarioInvoker(scenarioInvoker);
//				doRunScenario(studioScenarioDefinition, studioScenarioInvoker, invocationSettings, results);
//			} else {
//				if (gradleExperiment.getInvocation().isUseToolingApi()) {
					initializeNativeServicesForTapiClient(parameters, scenarioDefinition);
//				}
				doRunScenario(scenarioDefinition, scenarioInvoker, invocationSettings, results);
//			}
		} catch (IOException | InterruptedException e) {
			throw UncheckedException.throwAsUncheckedException(e);
		} finally {
			try {
				Logging.resetLogging();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ConnectorServices.reset(); // TODO: Remove internal dependency
		}
		return new GradleProfilerBuildExperimentExecutionResult<>(results, scenarioDefinition, scenarioInvoker, invocationSettings);
	}

	private <S extends ScenarioDefinition, R extends BuildInvocationResult> void doRunScenario(
		S scenarioDefinition,
		ScenarioInvoker<S, R> scenarioInvoker,
		InvocationSettings invocationSettings,
		List<R> results
	) throws IOException, InterruptedException {
		scenarioInvoker.run(scenarioDefinition, invocationSettings, results::add);
	}

	private void initializeNativeServicesForTapiClient(BuildExperimentExecutionContext<GradleInvocationSpec> parameters, GradleScenarioDefinition scenarioDefinition) {
		GradleConnector connector = GradleConnector.newConnector();
		try {
			connector.forProjectDirectory(parameters.getWorkingDirectory().get());
			connector.useInstallation(scenarioDefinition.getBuildConfiguration().getGradleHome());
			// First initialize the Gradle instance using the default user home dir
			// This sets some static state that uses files from the user home dir, such as DLLs
			try {
				Files.createDirectories(new File("gradle-home-dir").toPath());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			connector.useGradleUserHomeDir(new File("gradle-home-dir"));//context.getGradleUserHomeDir()); // TODO: Take form InvocationSpec
			try (ProjectConnection connection = connector.connect()) {
				connection.getModel(BuildEnvironment.class);
			}
		} finally {
			connector.disconnect();
		}
	}

	// TODO: Maybe merge probeBuildConfig with initializeNativeServicesForTapiClient ... there are some overlap
	//region See https://github.com/gradle/gradle-profiler/blob/master/src/main/java/org/gradle/profiler/DefaultGradleBuildConfigurationReader.java
	private GradleBuildConfiguration probeBuildConfig(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
		try {
			File initScript = File.createTempFile("gradle-profiler", ".gradle").getCanonicalFile();
			initScript.deleteOnExit();

			File buildDetails = File.createTempFile("gradle-profiler", "build-details");
			buildDetails.deleteOnExit();

			generateInitScript(initScript, buildDetails);

			DaemonControl daemonControl = new DaemonControl(determineGradleUserHome(parameters));

			GradleConnector connector = GradleConnector.newConnector();
			try {
				connector.forProjectDirectory(parameters.getWorkingDirectory().get());
				connector.useGradleVersion("7.4.1");
				return probe(connector, initScript, buildDetails, daemonControl);
			} finally {
				connector.disconnect();
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private GradleBuildConfiguration probe(GradleConnector connector, File initScript, File buildDetailsFile, DaemonControl daemonControl) {
		GradleBuildConfiguration version;
		try (ProjectConnection connection = connector.connect()) {
			BuildEnvironment buildEnvironment = connection.getModel(BuildEnvironment.class);
			new ToolingApiGradleClient(connection).runTasks(Arrays.asList("help"), Arrays.asList("-I", initScript.getAbsolutePath()), Collections.emptyList());
			List<String> buildDetails = readBuildDetails(buildDetailsFile);
			JavaEnvironment javaEnvironment = buildEnvironment.getJava();
			List<String> allJvmArgs = new ArrayList<>(javaEnvironment.getJvmArguments());
//			allJvmArgs.addAll(readSystemPropertiesFromGradleProperties());
			version = new GradleBuildConfiguration(
				GradleVersion.version(buildEnvironment.getGradle().getGradleVersion()),
				new File(buildDetails.get(0)),
				javaEnvironment.getJavaHome(),
				allJvmArgs,
				Boolean.valueOf(buildDetails.get(1))
			);
		}
		daemonControl.stop(version);
		return version;
	}

//	private List<String> readSystemPropertiesFromGradleProperties() {
//		String jvmArgs = getJvmArgsProperty(gradleUserHome);
//		if (jvmArgs == null) {
//			jvmArgs = getJvmArgsProperty(projectDir);
//		}
//		if (jvmArgs == null) {
//			return Collections.emptyList();
//		}
//		return ArgumentsSplitter.split(jvmArgs).stream().filter(arg -> arg.startsWith("-D")).collect(Collectors.toList());
//	}

	private List<String> readBuildDetails(File buildDetails) {
		try {
			return Files.readAllLines(buildDetails.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not read the build's configuration.", e);
		}
	}

	private void generateInitScript(File initScript, File buildDetails) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(initScript))) {
			writer.println(
				"rootProject {\n" +
					"  afterEvaluate {\n" +
					"    def detailsFile = new File(new URI('" + buildDetails.toURI() + "'))\n" +
					"    detailsFile.text = \"${gradle.gradleHomeDir}\\n\"\n" +
					"    detailsFile << plugins.hasPlugin('com.gradle.build-scan') << '\\n'\n" +
					"  }\n" +
					"}\n"
			);
		}
	}
	//endregion

	private InvocationSettings createInvocationSettings(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
//		boolean measureGarbageCollection = experiment.isMeasureGarbageCollection()
//			// Measuring GC needs build services which have been introduced in Gradle 6.1
//			&& experiment.getInvocation().getGradleDistribution().getVersion().getBaseVersion().compareTo(GradleVersion.version("6.1")) >= 0;
		return createInvocationSettingsBuilder(parameters)
			.setInvoker(context.getInvoker())
			.setDryRun(false)

			// TODO: Support non-version distribution
			.setVersions(Collections.singletonList(((VersionAwareGradleDistribution) parameters.getInvocation().get().getGradleDistribution().get()).getVersion()))
			.setGradleUserHome(determineGradleUserHome(parameters)) // TODO: take from InvocationSpec
			.setMeasureConfigTime(context.getMeasurements().anyMatch(configurationTimeMeasurement()))
			.setMeasuredBuildOperations(context.getMeasurements().flatMap(buildOperationMeasurements()).collect(toList()))
			.setMeasureGarbageCollection(context.getMeasurements().anyMatch(garbageCollectionMeasurement()))
//			.setBuildLog(invocationSpec.getBuildLog()) // TODO: take from context or maybe invocation spec
			.build();
	}

	private static Function<GradleProfilerBuildExperimentExecutorMeasurement, Stream<String>> buildOperationMeasurements() {
		return measurement -> {
			if (measurement instanceof GradleProfilerBuildOperationMeasurement) {
				return Stream.of(measurement.toString());
			} else {
				return Stream.empty();
			}
		};
	}

	private static File determineGradleUserHome(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
		File projectDirectory = parameters.getWorkingDirectory().get();
		// do not add the Gradle user home in the project directory, so it is not watched
		return new File(projectDirectory.getParent(), projectDirectory.getName() + "-" + GRADLE_USER_HOME_NAME);
	}

	private InvocationSettings.InvocationSettingsBuilder createInvocationSettingsBuilder(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
		return new InvocationSettings.InvocationSettingsBuilder()
			.setProjectDir(parameters.getWorkingDirectory().get())
			.setProfiler(context.getProfiler())
			.setBenchmark(true)
			.setOutputDir(context.getOutputDirectory().toFile())
			.setScenarioFile(null)
			.setSysProperties(emptyMap())
			.setWarmupCount(warmupsForExperiment(parameters).orElseGet(defaultWarmUpRuns(context)))
			.setIterations(invocationsForExperiment(parameters).orElseGet(defaultInvocationRuns()))
			.setCsvFormat(CsvGenerator.Format.LONG)
			.setTargets(parameters.getBuildAction().map(asTasksToRun()).orElse(Collections.emptyList()))
			;
	}

	// TODO: The override should be global to all BuildExperimentExecutor
	//region Override warmups/invocations
	private static Optional<Integer> warmupsForExperiment(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
		return or(getExperimentOverride("warmups"), () -> parameters.getWarmUpRuns().map(it -> it));
	}

	private static Optional<Integer> invocationsForExperiment(BuildExperimentExecutionContext<GradleInvocationSpec> parameters) {
		return or(getExperimentOverride("runs"), () -> parameters.getMeasurementRuns().map(it -> it));
	}

	private static Optional<Integer> getExperimentOverride(String key) {
		return Optional.ofNullable(System.getProperty("org.gradle.performance.execution." + key)).filter(it -> !"defaults".equals(it)).map(Integer::parseInt);
	}


	public static <T> Optional<T> or(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> self, Supplier<Optional<T>> supplier) {
		Objects.requireNonNull(supplier);
		if (self.isPresent()) {
			return self;
		} else {
			return Objects.requireNonNull(supplier.get());
		}
	}
	//endregion

	private static Supplier<Integer> defaultWarmUpRuns(GradleProfilerBuildExperimentExecutorContext context) {
		return () -> {
			if (context.usesDaemon()) {
				return 10;
			} else {
				return 1; // no need to warm up a daemon process
			}
		};
	}

	private static Supplier<Integer> defaultInvocationRuns() {
		return () -> 40;
	}

	private GradleScenarioDefinition createScenarioDefinition(BuildExperimentExecutionContext<GradleInvocationSpec> parameters, InvocationSettings invocationSettings) {
//		GradleDistribution gradleDistribution = invocationSpec.getGradleDistribution();

		final List<String> actualJvmArgs = Arrays.asList(loadJvmOptsFromGradlePropertiesIfPresent(invocationSettings.getProjectDir().toPath()));
//		final ImmutableList<String> actualJvmArgs = ImmutableList.<String>builder()
//			.add(jvmOptsFromGradleProperties)
//			.addAll(invocationSpec.getJvmArguments())
//			.build();
		return new GradleScenarioDefinition(
			OutputDirSelector.fileSafeNameFor(context.getDisplayName()),
			context.getDisplayName(),
			(GradleBuildInvoker) invocationSettings.getInvoker(),
//			new GradleBuildConfiguration(gradleDistribution.getVersion(), gradleDistribution.getGradleHomeDir(), Jvm.current().getJavaHome(), actualJvmArgs, false, invocationSpec.getClientJvmArguments()),
			probeBuildConfig(parameters),
//			new GradleBuildConfiguration(GradleVersion.version("7.4.1"), new File("gradle-home-dir"), Jvm.current().getJavaHome(), actualJvmArgs, false, Collections.emptyList()),
			// TODO: Add better exception
			parameters.getBuildAction().map(toGradleInvokerBuildAction()).orElseThrow(RuntimeException::new),
			parameters.getCleanAction().map(toGradleInvokerBuildAction()).orElse(BuildAction.NO_OP),
			parameters.getInvocation().get().getArguments().get(), // gradleArgs
			invocationSettings.getSystemProperties(),
			Collections.emptyList(), // build mutators
			invocationSettings.getWarmUpCount(),
			invocationSettings.getBuildCount(),
			invocationSettings.getOutputDir(),
			Collections.emptyList(), // jvmArgs
			invocationSettings.getMeasuredBuildOperations()
		);
	}

	// Required as not all invoker will "start" a JVM so extracting the jvmargs and passing them along is correct.
	// TODO: GradleRunner should also do this...
	private static String[] loadJvmOptsFromGradlePropertiesIfPresent(Path projectDirectory) {
		final Path gradlePropertiesFile = projectDirectory.resolve("gradle.properties");
		if (Files.exists(gradlePropertiesFile)) {
			Properties gradleProperties = new Properties();
			try (InputStream inputStream = Files.newInputStream(gradlePropertiesFile)) {
				gradleProperties.load(inputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return gradleProperties.getProperty("org.gradle.jvmargs", "").split(" ");
		} else {
			return new String[0];
		}
	}

	private GradleScenarioInvoker createScenarioInvoker(File gradleUserHome) {
		DaemonControl daemonControl = new DaemonControl(gradleUserHome);
		return new GradleScenarioInvoker(daemonControl, pidInstrumentation);
	}

	private Function<BuildExperimentAction, BuildAction> toGradleInvokerBuildAction() {
		return action -> {
			if (action instanceof GradleTaskBuildExperimentAction) {
				return new RunTasksAction(((GradleTaskBuildExperimentAction) action).getTasksToRun());
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private Function<BuildExperimentAction, List<String>> asTasksToRun() {
		return action -> {
			if (action instanceof GradleTaskBuildExperimentAction) {
				return ((GradleTaskBuildExperimentAction) action).getTasksToRun();
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}
}
