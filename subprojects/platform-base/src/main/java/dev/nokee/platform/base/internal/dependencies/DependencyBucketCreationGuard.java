package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyBucketCreationGuard {
	private final Map<String, DependencyBucket> configurationNameToBuckets = new HashMap<>();

	public <T extends DependencyBucket> T guard(DependencyIdentifier<T> identifier, DependencyBucketFactory<T> factory) {
		identifierUnderCreation = identifier;
		try {
			val bucket = factory.create(identifier);
			val previousValue = configurationNameToBuckets.put(identifier.getConfigurationName(), bucket);
			assert previousValue == null : "Duplicate identifier '" + identifier.getName() + "'";
			actions.forEach(it -> it.execute(bucket));
			return bucket;
		} finally {
			identifierUnderCreation = null;
			actions.clear();
		}
	}

	private final List<Action<? super DependencyBucket>> actions = new ArrayList<>();
	private DependencyIdentifier<? extends DependencyBucket> identifierUnderCreation = null;

	@Nullable
	private DependencyIdentifier<? extends DependencyBucket> getIdentifierUnderCreation() {
		return identifierUnderCreation;
	}

	public Action<? super Configuration> mapToDependencyBucket(Action<? super DependencyBucket> action) {
		return new Action<Configuration>() {
			@Override
			public void execute(Configuration configuration) {
				if (getIdentifierUnderCreation() != null && configuration.getName().equals(getIdentifierUnderCreation().getConfigurationName())) {
					// Let's come back to that
					actions.add(action);
				} else if (configurationNameToBuckets.containsKey(configuration.getName())) {
					action.execute(configurationNameToBuckets.get(configuration.getName()));
				}
			}
		};
	}
}
