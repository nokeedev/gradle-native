package dev.nokee.core.exec;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.TeeOutputStream;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.*;

public abstract class GradleWorkerExecutorEngine implements CommandLineToolExecutionEngine<GradleWorkerExecutorEngine.Handle> {
	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		WorkQueue workQueue = getWorkerExecutor().noIsolation();
		workQueue.submit(GradleWorkerExecutorEngineWorkAction.class, it -> {
			it.getCommandLine().add(invocation.getTool().getExecutable());
			it.getCommandLine().addAll(invocation.getArguments().get());
			it.getStandardStreamFile().set(invocation.getStandardStreamFile().orElse(null));
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
		RegularFileProperty getStandardStreamFile();
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

					OutputStream outStream = logs;
					if (getParameters().getStandardStreamFile().isPresent()) {
						try {
							outStream = new TeeOutputStream(new FileOutputStream(getParameters().getStandardStreamFile().get().getAsFile(), true), outStream);
						} catch (FileNotFoundException e) {
							throw new UncheckedIOException(e);
						}
					}
					spec.setStandardOutput(outStream);
					spec.setErrorOutput(outStream);
				});
			} catch (GradleException e) {
				throw new ExecException("An error happen while executing command, here is the output:\n" + logs.toString());
			}
		}
	}
}
