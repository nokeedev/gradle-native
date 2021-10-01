/*
 * Copyright 2021 the original author or authors.
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
package dev.gradleplugins.grava.testing;

import dev.gradleplugins.grava.testing.file.TestNameTestDirectoryProvider;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.TestCase;
import dev.nokee.internal.testing.runnerkit.ApplySection;
import dev.nokee.internal.testing.runnerkit.BuildScriptFile;
import dev.nokee.internal.testing.runnerkit.GradleDsl;
import dev.nokee.internal.testing.runnerkit.InitscriptSectionBuilder;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.PluginAware;
import org.opentest4j.TestAbortedException;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureCause;
import static dev.nokee.internal.testing.runnerkit.ApplyNotation.plugin;
import static dev.nokee.internal.testing.runnerkit.ApplySection.apply;
import static dev.nokee.internal.testing.runnerkit.DependenciesSection.dependencies;
import static dev.nokee.internal.testing.runnerkit.DependencyNotation.files;
import static dev.nokee.internal.testing.runnerkit.DependenciesSectionBuilder.classpath;
import static dev.nokee.internal.testing.runnerkit.PluginsSection.plugins;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Test Gradle plugin behaviour deemed of a good plugin.
 *
 * <h3>Usage</h3>
 *
 * Standalone test can be achieved with the following:
 * <pre>
 *     new WellBehavedPluginTester().qualifiedPluginId("foo.bar").testWellBehavedPlugin()
 * </pre>
 *
 * JUnit 5 {@code @TestFactory} can be achieved with the following:
 * <pre>
 * &#64;TestFactory
 * Stream&#60;DynamicTest&#62; checkWellBehavedPlugin() {
 *     return new WellBehavedPluginTester()
 *     		.qualifiedPluginId("foo.bar")
 *     		.stream()
 *     		.map(TestCaseUtils::toJUnit5DynamicTest);
 * }
 * </pre>
 */
public final class WellBehavedPluginTester extends AbstractTester {
	private String qualifiedPluginId;
	private Class<? extends Plugin<?>> pluginType;
	private EnumSet<SupportedTarget> targets = null;
	private Boolean crossTargetErrorSupport = null;

	private String getQualifiedPluginIdUnderTest() {
		if (qualifiedPluginId == null) {
			throw new TestAbortedException();
		}
		return qualifiedPluginId;
	}

	private Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		if (pluginType == null) {
			throw new TestAbortedException();
		}
		return pluginType;
	}

	public WellBehavedPluginTester qualifiedPluginId(String qualifiedPluginId) {
		this.qualifiedPluginId = qualifiedPluginId;
		return this;
	}

	public <T extends Plugin<?>> WellBehavedPluginTester pluginClass(Class<T> pluginType) {
		this.pluginType = pluginType;
		return this;
	}

	public WellBehavedPluginTester supportedTarget(SupportedTarget... targets) {
		this.targets = EnumSet.copyOf(Arrays.asList(targets));
		return this;
	}

	/**
	 * Mark the plugin under test as misbehaving when applied to unsupported target.
	 * By default, when a plugin is applied to an unsupported target, i.e. Project plugin applied to Settings, a class cast exception will be thrown.
	 * This behavior is not very informative to the user.
	 * A well behaved plugin will show an informative error message to the user on the correct usage.
	 *
	 * @return this tester
	 */
	public WellBehavedPluginTester doesNotWellBehaveWhenAppliedToUnsupportedTarget() {
		this.crossTargetErrorSupport = false;
		return this;
	}

	public enum SupportedTarget {
		Project("build.gradle"),
		Settings("settings.gradle"),
		Init("init.gradle");

		private final String buildScriptName;

		SupportedTarget(String buildScriptName) {
			this.buildScriptName = buildScriptName;
		}

		private String getBuildScriptName() {
			return buildScriptName;
		}
	}

	protected void collectTesters(List<TestCase> testCases) {
		if (targets == null) {
			targets = EnumSet.of(SupportedTarget.Project);
			if (crossTargetErrorSupport == null) {
				crossTargetErrorSupport = false;
			}
		}
		if (crossTargetErrorSupport == null) {
			crossTargetErrorSupport = true;
		}

		for (SupportedTarget target : targets) {
			// Plugin id resolution service is not available in init scripts
			if (target != SupportedTarget.Init) {
				testCases.add(new AppliesPluginTypeWhenApplyingPluginId(target));
				testCases.add(new CanApplyPluginByIdUsingPluginAwareApply(target));
				testCases.add(new CanApplyPluginViaPluginDsl(target));
			}

			testCases.add(new CanApplyPluginByTypeUsingPluginAwareApply(target));

			testCases.add(new CanExecuteHelpTask(target));
			testCases.add(new CanExecuteTasksTask(target));
			testCases.add(new DoesNotRealizeTask(target));
			testCases.add(new DoesNotResolveConfiguration(target));
			testCases.add(new CanResolveAllDomainObjects(target));
		}

		if (crossTargetErrorSupport) {
			for (SupportedTarget target : EnumSet.complementOf(targets)) {
				testCases.add(new ThrowsSensibleExceptionWhenApplyingPluginOnWrongTarget(target));
			}
		}
	}

	// TODO: move to Supported target?
	private ApplySection appliesPluginToTarget(SupportedTarget target) {
		if (target == SupportedTarget.Init || qualifiedPluginId == null) {
			return apply(plugin(getPluginTypeUnderTest()));
		} else {
			return apply(plugin(getQualifiedPluginIdUnderTest()));
		}
	}

	/**
	 * Executes all well behaved plugin tests.
	 * Use this method for opinionated test framework testing.
	 * Use {@link #stream()} to convert the test case into your target test framework.
	 */
	public void testWellBehavedPlugin() {
		if (qualifiedPluginId == null && pluginType == null) {
			throw new AssertionError("Missing qualified plugin id and/or plugin type");
		}
		executeAllTestCases();
	}

	private static final boolean LEAVE_WORKSPACE_BEHIND_ON_ERRORS = Boolean.parseBoolean(System.getProperty("dev.gradleplugins.internal.leave-workspace-behind-on-errors", "false"));
	private abstract class FileTesterTestCase implements TestCase {
		private final TestNameTestDirectoryProvider testDirectory = TestNameTestDirectoryProvider.newInstance(getDisplayName(), WellBehavedPluginTester.this);
		private boolean shouldCleanup = true;

		protected Path getWorkingDirectory() {
			return testDirectory.getTestDirectory();
		}

		@Override
		public final void execute() throws Throwable {
			try {
				doExecute();
			} catch (TestAbortedException t) {
				// check up any aborted tests
				throw t;
			} catch (Throwable t) {
				if (LEAVE_WORKSPACE_BEHIND_ON_ERRORS) {
					shouldCleanup = false;
				}
				throw t;
			}
		}

		protected abstract void doExecute() throws Throwable;

		@Override
		public void tearDown() throws Throwable {
			if (shouldCleanup) {
				testDirectory.cleanup();
			}
		}
	}

	private abstract class AbstractWellBehavedIntegrationTest extends FileTesterTestCase {
		@SneakyThrows
		protected GradleRunner newRunner() {
			val runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(getWorkingDirectory().toFile()).configure(this::configureRunnerPluginDslClasspath).configure(this::configureRunnerBuildscriptClasspath).usingInitScript(getInitFile().toFile());
			getInitFile().append(
				new InitscriptSectionBuilder().dependencies(classpath(files(runner.getPluginClasspath()))).toString(GradleDsl.GROOVY)
			);
			return runner;
		}

		protected GradleRunner runner;

		@Override
		public void setUp() throws Throwable {
			super.setUp();
			runner = newRunner();
		}

		protected final BuildResult succeeds(String... tasks) {
			return runner.withTasks(tasks).build();
		}

		protected final BuildResult fails(String... tasks) {
			return runner.withTasks(tasks).buildAndFail();
		}

		protected final BuildScriptFile getBuildFile() {
			return new BuildScriptFile(getWorkingDirectory().resolve("build.gradle"));
		}

		protected final BuildScriptFile getSettingsFile() {
			return new BuildScriptFile(getWorkingDirectory().resolve("settings.gradle"));
		}

		protected final BuildScriptFile getInitFile() {
			return new BuildScriptFile(getWorkingDirectory().resolve("init.gradle"));
		}

		protected BuildScriptFile buildScript(String path) {
			return new BuildScriptFile(getWorkingDirectory().resolve(path));
		}

		private GradleRunner configureRunnerPluginDslClasspath(GradleRunner runner) {
			// TODO: Should this be a feature of Runner Kit
			if (Thread.currentThread().getContextClassLoader().getResource("plugin-under-test-metadata.properties") == null) {
				return runner.withPluginClasspath(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new).collect(toList()));
			}
			return runner.withPluginClasspath();
		}

		@SneakyThrows // TODO: GradleRunner#configure should accept functional that throws exception
		private GradleRunner configureRunnerBuildscriptClasspath(GradleRunner runner) {
			val initScript = new BuildScriptFile(getWorkingDirectory().resolve("classpath.init.gradle"));
			initScript.append(new InitscriptSectionBuilder().dependencies(classpath(files(runner.getPluginClasspath()))).toString(GradleDsl.GROOVY))
				.append(String.join(System.lineSeparator(),
					"beforeSettings { settings ->",
					"  settings.buildscript." + dependencies(classpath(files(runner.getPluginClasspath()))),
					"  settings.include('a', 'b', 'c')", // Include sub-projects in-case the plugin misbehave on sub-projects
					"}"
				));
			return runner.usingInitScript(initScript.toFile());
		}
	}

	/**
	 * Tests the implicit assumption between the plugin id and the plugin class, applying the plugin by id should introduce the {@literal pluginType} in the {@link org.gradle.api.plugins.PluginContainer}.
	 * This assumption is important to tests as a developer could introduce a typo in the plugin id to class mapping which would result in applying the wrong plugin.
	 * Note that we don't need to test any other assumption as those are implicit Gradle behaviours.
	 */
	private final class AppliesPluginTypeWhenApplyingPluginId extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private AppliesPluginTypeWhenApplyingPluginId(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "applies plugin type when applying plugin id [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			buildScript(target.getBuildScriptName())
				.append("assert plugins.withType(Class.forName('" + getPluginTypeUnderTest().getTypeName() + "')).size() == 0")
				.append(apply(plugin(getQualifiedPluginIdUnderTest())).generateSection(GradleDsl.GROOVY))
				.append("assert plugins.withType(Class.forName('" + getPluginTypeUnderTest().getTypeName() + "')).size() == 1");

			succeeds();
		}
	}

	/**
	 * Test a plugin id can apply using {@link PluginAware#apply(Map)} without exception.
	 * It is recommended to always use the Plugin DSL, but it requires plugin markers which may not be generated.
	 */
	private final class CanApplyPluginByIdUsingPluginAwareApply extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private CanApplyPluginByIdUsingPluginAwareApply(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "can apply plugin by id using apply(plugin: <id>) [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			buildScript(target.getBuildScriptName()).append(
				apply(plugin(getQualifiedPluginIdUnderTest())).generateSection(GradleDsl.GROOVY)
			);

			succeeds();
		}
	}

	/**
	 * Test a plugin class can apply using {@link PluginAware#apply(Map)} without exception.
	 * It is recommended to always use the Plugin DSL, however, for internal plugins, plugin ID may be unwanted.
	 */
	private final class CanApplyPluginByTypeUsingPluginAwareApply extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private CanApplyPluginByTypeUsingPluginAwareApply(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "can apply plugin by id using apply(plugin: <class>) [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			buildScript(target.getBuildScriptName()).append(
				apply(plugin(getPluginTypeUnderTest())).generateSection(GradleDsl.GROOVY)
			);
			succeeds();
		}
	}

	/**
	 * Applies the plugin via plugin DSL without exception.
	 */
	private final class CanApplyPluginViaPluginDsl extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private CanApplyPluginViaPluginDsl(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "can apply plugin via plugin DSL [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			buildScript(target.getBuildScriptName()).append(
				plugins(it -> it.id(getQualifiedPluginIdUnderTest())).generateSection(GradleDsl.GROOVY)
			);

			succeeds();
		}
	}

	/**
	 * Throws sensible, e.g. non-default, exception if plugin is applied to wrong target, i.e. applying a Project plugin to a Settings build script.
	 * By default, the exception is a class cast exception, e.g. Settings cannot be cast to Project.
	 * The exception message is non-informative for the user and would be in the best interest to rewrite the error message when developing a Settings and Init plugin.
	 */
	private final class ThrowsSensibleExceptionWhenApplyingPluginOnWrongTarget extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		ThrowsSensibleExceptionWhenApplyingPluginOnWrongTarget(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "throws exception when applying plugin on wrong target, e.g. " + target;
		}

		@Override
		public void doExecute() throws Throwable {
			// Applies the plugin however possible
			buildScript(target.getBuildScriptName()).append(appliesPluginToTarget(target));

			val result = fails();
			assertThat("gives sensible exception when applying to wrong target",
				result, not(hasFailureCause(containsString("_Decorated cannot be cast to org.gradle.api"))));
		}
	}

	/**
	 * Test {@literal help} task can always execute without exception.
	 */
	private class CanExecuteHelpTask extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private CanExecuteHelpTask(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "can execute help task [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			// Applies the plugin however possible
			buildScript(target.getBuildScriptName()).append(appliesPluginToTarget(target));

			succeeds("help");
		}
	}

	/**
	 * Test {@literal tasks} task can always execute without exception.
	 */
	private class CanExecuteTasksTask extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private CanExecuteTasksTask(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "can execute tasks task [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			// Applies the plugin however possible
			buildScript(target.getBuildScriptName()).append(appliesPluginToTarget(target));

			succeeds("tasks");
		}
	}

	/**
	 * Plugins should never realize any tasks unless they are part of the task graph.
	 */
	private class DoesNotRealizeTask extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private DoesNotRealizeTask(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "does not realize task [" + target + "]";
		}

		protected List<String> getRealizedTaskPaths() {
			return Collections.singletonList(":help");
		}

		private List<String> getRealizedQuotedTaskPaths() {
			return getRealizedTaskPaths().stream().map(this::quoted).collect(toList());
		}

		private String quoted(String s) {
			return "'" + s + "'";
		}

		@Override
		public void doExecute() throws Throwable {
			// Applies the plugin however possible
			buildScript(target.getBuildScriptName()).append(appliesPluginToTarget(target));

			getBuildFile().append(String.join(System.lineSeparator(),
				"def configuredTasks = []",
				"allprojects {",
				"  tasks.configureEach {",
				"    configuredTasks << it",
				"  }",
				"}",
				"",
				"gradle.buildFinished {",
				"  def configuredTaskPaths = configuredTasks*.path",
				"",
				"  // TODO: Log warning if getRealizedTaskPaths() is different than ':help'",
				"  configuredTaskPaths.removeAll([" + join(", ", getRealizedQuotedTaskPaths()) + "])",
				"  assert configuredTaskPaths == []",
				"}"
			));

			succeeds("help");
		}
	}

	/**
	 * Plugins should never resolve configuration.
	 */
	private class DoesNotResolveConfiguration extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private DoesNotResolveConfiguration(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "does not resolve configuration [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			// Applies the plugin however possible
			buildScript(target.getBuildScriptName()).append(appliesPluginToTarget(target));

			getBuildFile().append(String.join(System.lineSeparator(),
				"def resolvedDependenciesPaths = []",
				"allprojects {",
				"  configurations.all { configuration ->",
				"    // In case the configuration was already resolved...",
				"    if (configuration.state == Configuration.State.RESOLVED) {",
				"      resolvedDependenciesPaths << incoming.path",
				"    } else {",
				"      configuration.incoming.afterResolve { resolvedDep ->",
				"        resolvedDependenciesPaths << resolvedDep.path",
				"      }",
				"    }",
				"  }",
				"}",
				"",
				"gradle.buildFinished {",
				"    assert resolvedDependenciesPaths == [] : 'some configuration were resolved'",
				"}"
			));

			succeeds();
		}
	}

	/**
	 * Tests all domain objects can configure without exception.
	 */
	private class CanResolveAllDomainObjects extends AbstractWellBehavedIntegrationTest {
		private final SupportedTarget target;

		private CanResolveAllDomainObjects(SupportedTarget target) {
			this.target = target;
		}

		@Override
		public String getDisplayName() {
			return "can resolve all domain objects [" + target + "]";
		}

		@Override
		public void doExecute() throws Throwable {
			// Applies the plugin however possible
			buildScript(target.getBuildScriptName()).append(appliesPluginToTarget(target));

			getBuildFile().append(String.join(System.lineSeparator(),
				"allprojects {",
				"  tasks.all { /* TaskContainer#all force all object to realize */ }",
				"  configurations.all { /* ConfigurationContainer#all force all object to realize */ }",
				"}"
			));

			succeeds();
		}
	}
}
