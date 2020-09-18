package dev.nokee.platform.base.internal;

import com.google.common.collect.Iterables;
import dev.nokee.platform.base.Binary;
import dev.nokee.utils.ProviderUtils;
import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DevelopmentBinaryUtils {
	public static Provider<? extends Binary> selectSingleBinaryByType(Class<? extends Binary> binaryTypeToSelect, Iterable<? extends Binary> binaries) {
		return StreamSupport.stream(binaries.spliterator(), false)
			.filter(binaryTypeToSelect::isInstance)
			.collect(toAtMostOneElement())
			.map(ProviderUtils::fixed)
			.orElseGet(ProviderUtils::notDefined);
	}

	private static <T> Collector<T, ?, Optional<T>> toAtMostOneElement() {
		return Collectors.collectingAndThen(
			Collectors.toList(),
			list -> Optional.of(list).map(it -> Iterables.getOnlyElement(it, null))
		);
	}
}
