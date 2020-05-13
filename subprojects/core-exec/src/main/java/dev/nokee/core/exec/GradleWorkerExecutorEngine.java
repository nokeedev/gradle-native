package dev.nokee.core.exec;

import lombok.RequiredArgsConstructor;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

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
			getExecOperations().exec(spec -> {
				spec.commandLine(getParameters().getCommandLine().get());

				if (getParameters().getStandardStreamFile().isPresent()) {
					try {
						OutputStream outStream = new FileOutputStream(getParameters().getStandardStreamFile().get().getAsFile(), true);
						spec.setStandardOutput(outStream);
						spec.setErrorOutput(outStream);
					} catch (FileNotFoundException e) {
						throw new UnsupportedOperationException(e);
					}
				}
			});
		}
	}
}
