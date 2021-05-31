package dev.nokee.model.internal;

import com.google.common.collect.Streams;
import lombok.val;
import org.gradle.api.InvalidUserDataException;

import java.util.Objects;
import java.util.stream.Collectors;

final class RegistrableTypeAssertions {
	private RegistrableTypeAssertions() {}

	public static void assertRegistrableType(String displayName, NamedDomainObjectRegistry.RegistrableTypes actual, Class<?> expected) {
		if (!actual.canRegisterType(expected)) {
			throw unregistrableTypeException(expected, displayName, actual);
		}
	}

	public static RuntimeException unregistrableTypeException(Class<?> type, String displayName, Iterable<NamedDomainObjectRegistry.SupportedType> supportedTypes) {
		return new InvalidUserDataException(
			String.format("Cannot register a %s because this type is not known to %s. Known types are: %s", type.getSimpleName(), displayName, supportedTypeNames(supportedTypes)),
			new NoFactoryRegisteredForTypeException());
	}

	private static String supportedTypeNames(Iterable<NamedDomainObjectRegistry.SupportedType> supportedTypes) {
		val result = Streams.stream(supportedTypes).map(Objects::toString).sorted().collect(Collectors.joining(", "));
		return result.isEmpty() ? "(None)" : result;
	}

	private static final class NoFactoryRegisteredForTypeException extends RuntimeException {}
}
