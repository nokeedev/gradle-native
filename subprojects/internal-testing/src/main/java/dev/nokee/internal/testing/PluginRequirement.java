package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public final class PluginRequirement {
	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@ExtendWith(Extension.class)
	public @interface Require {
		Class<? extends Plugin<? extends Project>> type() default NONE.class;
		String id() default "";

		interface NONE extends Plugin<Project> {}
	}

	static final class Extension implements BeforeEachCallback {
		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			AnnotationUtils.findAnnotation(context.getElement(), PluginRequirement.Require.class).ifPresent(it -> applyPlugin(context, it));
			val allInstances = new ArrayList<>(context.getRequiredTestInstances().getAllInstances());
			Collections.reverse(allInstances);
			for (Object testInstance : allInstances) {
				for (Require require : testInstance.getClass().getAnnotationsByType(Require.class)) {
					applyPlugin(context, require);
				}
			}
		}

		private void applyPlugin(ExtensionContext context, Require require) {
			val project = (Project) context.getRequiredTestInstances().getAllInstances().stream().flatMap(instance -> {
				val method = ReflectionUtils.findMethod(instance.getClass(), "project");
				if (method.isPresent()) {
					return Stream.of(ReflectionUtils.invokeMethod(method.get(), instance));
				} else {
					return ReflectionUtils.tryToReadFieldValue((Class<Object>) instance.getClass(), "project", instance)
						.toOptional().map(Stream::of).orElse(Stream.empty());
				}
			}).findFirst().orElseThrow(RuntimeException::new);

			if (!require.id().isEmpty()) {
				System.out.println("Applying plugin " + require.id());
				project.getPluginManager().apply(require.id());
			} else {
				System.out.println("Applying plugin " + require.type());
				project.getPluginManager().apply(require.type());
			}
		}
	}
}
