package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLineToolArguments;
import lombok.Value;

import java.util.List;

@Value
public class EmptyCommandLineToolArguments implements CommandLineToolArguments {
	public static final EmptyCommandLineToolArguments INSTANCE = new EmptyCommandLineToolArguments();

	@Override
	public List<String> get() {
		return ImmutableList.of();
	}
}
