package dev.nokee.platform.nativebase.internal.rules;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Set;

enum ToDevelopmentBinaryCompileTasksTransformer implements Transformer<Provider<Set<? extends SourceCompile>>, Variant> {
	TO_DEVELOPMENT_BINARY_COMPILE_TASKS;

	@Override
	public Provider<Set<? extends SourceCompile>> transform(Variant variant) {
		val developmentBinary = variant.getDevelopmentBinary().get();
		if (developmentBinary instanceof NativeBinary) {
			return ((NativeBinary) developmentBinary).getCompileTasks().getElements();
		}
		return ProviderUtils.fixed(ImmutableSet.of());
	}
}
