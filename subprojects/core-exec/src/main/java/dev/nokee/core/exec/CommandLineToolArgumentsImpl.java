package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@RequiredArgsConstructor
final class CommandLineToolArgumentsImpl implements CommandLineToolArguments {
	private final List<Object> arguments;

	@Override
	public List<String> get() {
		return ImmutableList.copyOf(arguments.stream().map(Object::toString).collect(Collectors.toList()));
	}
}
