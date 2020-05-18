package dev.nokee.core.exec;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingProcessBuilderEngine implements CommandLineToolExecutionEngine<CachingProcessBuilderEngine.Handle> {
	private final CommandLineToolExecutionEngine<ProcessBuilderEngine.Handle> delegate;
	private final LoadingCache<CommandLineToolInvocation, CommandLineToolExecutionResult> cache = CacheBuilder.newBuilder()
		.concurrencyLevel(4)
		.build(new CacheLoader<CommandLineToolInvocation, CommandLineToolExecutionResult>() {
		@Override
		public CommandLineToolExecutionResult load(CommandLineToolInvocation key) throws Exception {
			return delegate.submit(key).waitFor().assertNormalExitValue();
		}
	});

	public CachingProcessBuilderEngine(CommandLineToolExecutionEngine<ProcessBuilderEngine.Handle> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		return () -> cache.getUnchecked(invocation);
	}

	public interface Handle extends CommandLineToolExecutionHandle {
		CommandLineToolExecutionResult getResult();
	}
}
