package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.LanguageSourceSet;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface LanguageSourceSetConventionSupplier {
	List<String> get(String sourceSetName);

	static LanguageSourceSetConventionSupplier defaultObjectiveCGradle(ComponentName componentName) {
		return name -> {
			assert name.equals("objectiveC") : "apply this convention only to source set named 'objectiveC'.";
			return ImmutableList.of("src/" + componentName + "/objc");
		};
	}

	static LanguageSourceSetConventionSupplier defaultObjectiveCppGradle(ComponentName componentName) {
		return name -> {
			assert name.equals("objectiveCpp") : "apply this convention only to source set named 'objectiveCpp'.";
			return ImmutableList.of("src/" + componentName + "/objcpp");
		};
	}

	static LanguageSourceSetConventionSupplier maven(ComponentName componentName) {
		return name -> ImmutableList.of("src/" + componentName + "/" + name);
	}

	static <T extends LanguageSourceSet> Consumer<T> withConventionOf(LanguageSourceSetConventionSupplier... suppliers) {
		return sourceSet -> {
			sourceSet.convention((Callable<List<String>>) () -> Arrays.stream(suppliers).flatMap(it -> it.get(sourceSet.getName()).stream()).collect(Collectors.toList()));
		};
	}
}
