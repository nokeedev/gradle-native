package dev.nokee.platform.nativebase.internal.rules;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.NativeBinary;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.gradleplugins.grava.util.TransformerUtils.toSetTransformer;

enum ToBinariesCompileTasksTransformer implements Transformer<Provider<Set<? extends SourceCompile>>, Variant> {
	TO_DEVELOPMENT_BINARY_COMPILE_TASKS;

	@Override
	public Provider<Set<? extends SourceCompile>> transform(Variant variant) {
		return variant.getBinaries().flatMap(FlatMapBinaryToCompileTasks.INSTANCE).map(toSetTransformer());
	}

	enum FlatMapBinaryToCompileTasks implements Transformer<Iterable<SourceCompile>, Binary> {
		INSTANCE;

		@Override
		public Iterable<SourceCompile> transform(Binary binary) {
			if (binary instanceof NativeBinary) {
				return ((NativeBinary) binary).getCompileTasks().get();
			}
			return ImmutableSet.of();
		}
	}
}
