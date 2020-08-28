package dev.nokee.buildadapter.cmake.internal.fileapi;

import com.google.common.collect.Iterables;
import lombok.val;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class RemoveTargetIdFromConfiguration implements UnaryOperator<CodeModel.Configuration> {
	private final String targetIdToRemove;

	public RemoveTargetIdFromConfiguration(String targetIdToRemove) {
		this.targetIdToRemove = targetIdToRemove;
	}

	@Override
	public CodeModel.Configuration apply(CodeModel.Configuration configuration) {
		val targetsToKeep = configuration.getTargets().stream().filter(it -> !it.getId().equals(targetIdToRemove)).collect(Collectors.toList());
		val projectsToKeep = targetsToKeep.stream().map(it -> it.getProject(configuration)).collect(Collectors.toCollection(LinkedHashSet::new));

		List<CodeModel.Configuration.TargetReference> targetsToKeepIndexAdjusted = targetsToKeep.stream().map(it -> {
			val projectIndex = Iterables.indexOf(projectsToKeep, a -> a.equals(it.getProject(configuration)));
			return it.withProjectIndex(projectIndex);
		}).collect(Collectors.toList());

		val projectsToKeepIndexAdjusted = projectsToKeep.stream().map(it -> {
			return it.withTargetIndexes(it.getTargets(configuration).stream().map(target -> {
				return Iterables.indexOf(targetsToKeep, a -> a.equals(target));
			}).filter(index -> index >= 0).collect(Collectors.toList()));
		}).collect(Collectors.toList());

		return new CodeModel.Configuration(configuration.getName(), projectsToKeepIndexAdjusted, targetsToKeepIndexAdjusted);
	}
}
