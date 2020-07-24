package dev.nokee.core.exec;

import dev.nokee.core.exec.internal.FromListCommandLineToolInvocationEnvironmentVariables;

import java.util.List;
import java.util.Map;

public interface CommandLineToolInvocationEnvironmentVariables {
	Map<String, String> getAsMap();

	List<String> getAsList();

	// TODO: allow more manipulation of the environment variables such as getting, putting (creates another instance with the result), etc.

	// TODO: allow to compare various instances
	//   for example: listOf("foo=a", "bar=b") == mapOf(bar=b, foo=a)

	static CommandLineToolInvocationEnvironmentVariables from(List<String> environmentVariables) {
		return new FromListCommandLineToolInvocationEnvironmentVariables(environmentVariables);
	}

	// TODO: factory methods should include from(Map<String, String>)
	// TODO: factory methods should include inherit() -> from current process
	// TODO: factory methods should include empty() -> No environment variables
	// TODO: factory methods could include from(File) -> files that is properties style values
	// TODO: factory methods could include from(Properties)
}
