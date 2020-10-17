package dev.nokee.language.base.internal.rules;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import java.lang.reflect.InvocationTargetException;

// FIXME: Test
public class LanguageSourceSetConventionRule implements Action<LanguageSourceSetInternal> {
	private final ObjectFactory objectFactory;

	public LanguageSourceSetConventionRule(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void execute(LanguageSourceSetInternal sourceSet) {
		sourceSet.convention(objectFactory.fileTree().setDir("src/" + componentOwnerName(sourceSet.getIdentifier()) + "/" + sourceSet.getIdentifier().getName().get()));
	}

	private String componentOwnerName(LanguageSourceSetIdentifier<?> identifier) {
		assert identifier.getOwnerIdentifier().getClass().getSimpleName().equals("ComponentIdentifier");
		try {
			val getNameMethod = identifier.getOwnerIdentifier().getClass().getMethod("getName");
			val componentName = getNameMethod.invoke(identifier.getOwnerIdentifier());
			val name = componentName.getClass().getMethod("get").invoke(componentName);
			return (String) name;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
