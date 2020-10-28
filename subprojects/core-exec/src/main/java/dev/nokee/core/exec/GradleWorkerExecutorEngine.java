package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.CommandLineToolInvocationOutputRedirectInternal;
import dev.nokee.core.exec.internal.CommandLineToolOutputStreams;
import dev.nokee.core.exec.internal.CommandLineToolOutputStreamsImpl;
import lombok.RequiredArgsConstructor;
import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

public abstract class GradleWorkerExecutorEngine implements CommandLineToolExecutionEngine<GradleWorkerExecutorEngine.Handle> {
	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		WorkQueue workQueue = getWorkerExecutor().noIsolation();
		workQueue.submit(GradleWorkerExecutorEngineWorkAction.class, it -> {
			it.getCommandLine().add(invocation.getTool().getExecutable());
			it.getCommandLine().addAll(invocation.getArguments().get());
			it.getStandardOutputRedirect().set(invocation.getStandardOutputRedirect());
			it.getErrorOutputRedirect().set(invocation.getErrorOutputRedirect());
			it.getEnvironmentVariables().set(invocation.getEnvironmentVariables());
		});
		return new Handle(workQueue);
	}

	@RequiredArgsConstructor
	public static class Handle implements CommandLineToolExecutionHandle {
		private final WorkQueue workQueue;

		public void await() {
			workQueue.await();
		}
	}

	public interface GradleWorkerExecutorEngineWorkParameters extends WorkParameters {
		ListProperty<String> getCommandLine();
		Property<CommandLineToolInvocationStandardOutputRedirect> getStandardOutputRedirect();
		Property<CommandLineToolInvocationErrorOutputRedirect> getErrorOutputRedirect();
		Property<CommandLineToolInvocationEnvironmentVariables> getEnvironmentVariables();
	}

	public static abstract class GradleWorkerExecutorEngineWorkAction implements WorkAction<GradleWorkerExecutorEngineWorkParameters> {
		@Inject
		protected abstract ExecOperations getExecOperations();

		@Override
		public void execute() {
			ByteArrayOutputStream logs = new ByteArrayOutputStream();
			try {
				getExecOperations().exec(spec -> {
					spec.commandLine(getParameters().getCommandLine().get());

					CommandLineToolOutputStreams streams = new CommandLineToolOutputStreamsImpl(logs, logs);
					if (getParameters().getStandardOutputRedirect().get() instanceof CommandLineToolInvocationOutputRedirectInternal) {
						streams = ((CommandLineToolInvocationOutputRedirectInternal) getParameters().getStandardOutputRedirect().get()).redirect(streams);
					}
					if (getParameters().getErrorOutputRedirect().get() instanceof CommandLineToolInvocationOutputRedirectInternal) {
						streams = ((CommandLineToolInvocationOutputRedirectInternal) getParameters().getErrorOutputRedirect().get()).redirect(streams);
					}
					spec.setStandardOutput(streams.getStandardOutput());
					spec.setErrorOutput(streams.getErrorOutput());
					spec.setEnvironment(getParameters().getEnvironmentVariables().get().getAsMap());
				});
			} catch (GradleException e) {
				throw new ExecException("An error happen while executing command, here is the output:\n" + logs.toString());
			}
		}
	}
}
