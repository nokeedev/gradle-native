package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLineToolArguments;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultCommandLineToolArguments implements CommandLineToolArguments {
	private final List<Object> arguments;

	@Override
	public List<String> get() {
		return ImmutableList.copyOf(arguments.stream().map(Object::toString).collect(Collectors.toList()));
	}
}
