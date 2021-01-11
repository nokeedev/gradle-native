package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.internal.ComponentName;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.util.GUtil;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class ConfigurationDescription implements Supplier<String> {
	private final Subject subject;
	private final Bucket bucket;
	private final Owner owner;

	public ConfigurationDescription(Subject subject, Bucket bucket, Owner owner) {
		this.subject = subject;
		this.bucket = bucket;
		this.owner = owner;
	}

	public String get() {
		val builder = new StringBuilder();

		builder.append(StringUtils.capitalize(subject.toString()));
		if (!bucket.equals(DescriptionSegment.EMPTY)) {
			builder.append(" ").append(bucket);
		}
		builder.append(" for ").append(owner).append(".");

		return builder.toString();
	}

	@Override
	public String toString() {
		return get();
	}

	public interface Subject {
		String get();

		static Subject ofName(String name) {
			return new DescriptionSegment(GUtil.toWords(name).replace("api", "API"));
		}
	}

	public interface Bucket {
		String get();

		static Bucket ofDeclarable() {
			return new DescriptionSegment("dependencies");
		}

		static Bucket ofConsumable() {
			return DescriptionSegment.EMPTY;
		}

		static Bucket ofResolvable() {
			return DescriptionSegment.EMPTY;
		}
	}

	public interface Owner {
		String get();

		static Owner ofThisProject() {
			return new DescriptionSegment("this project");
		}

		static Owner ofProject(Project project) {
			return new DescriptionSegment("project '" + project.getPath() + "'");
		}

		static Owner ofComponent(ComponentName componentName) {
			return new DescriptionSegment(fromComponent(requireNonNull(componentName)));
		}

		static Owner ofVariant(ComponentName componentName, String variantName) {
			requireNonNull(componentName);
			requireNonNull(variantName);
			if (StringUtils.isEmpty(variantName)) {
				return new DescriptionSegment(fromComponent(componentName));
			} else if (componentName.isMain()) {
				return new DescriptionSegment("variant '" + variantName + "'");
			}
			return new DescriptionSegment("variant '" + variantName + "' of " + fromComponent(componentName));
		}
	}

	private static String fromComponent(ComponentName componentName) {
		if (componentName.isMain()) {
			return "main component";
		}
		return "component '" + componentName + "'";
	}

	@EqualsAndHashCode
	private static final class DescriptionSegment implements Subject, Owner, Bucket {
		static final DescriptionSegment EMPTY = new DescriptionSegment("");
		private final String segment;

		public DescriptionSegment(String segment) {
			this.segment = segment;
		}

		@Override
		public String get() {
			return segment;
		}

		@Override
		public String toString() {
			return segment;
		}
	}
}
