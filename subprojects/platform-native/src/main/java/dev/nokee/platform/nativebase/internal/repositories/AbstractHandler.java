package dev.nokee.platform.nativebase.internal.repositories;

import com.google.gson.Gson;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class AbstractHandler implements Handler {
	private static final Logger LOGGER = Logger.getLogger(AbstractHandler.class.getName());
	private final String contextPath;

	protected AbstractHandler(String contextPath) {
		this.contextPath = contextPath;
	}

	public abstract boolean isKnownModule(String moduleName);
	public abstract boolean isKnownVersion(String moduleName, String version);
	public abstract List<String> findVersions(String moduleName);
	public abstract GradleModuleMetadata getGradleModuleMetadata(String moduleName, String version);
	public abstract String handle(String moduleName, String version, String target);

	@Override
	public final Optional<Response> handle(String target) {
		target = target.substring(contextPath.length());

		int idx = target.indexOf('/');
		String moduleName = target.substring(0, idx);
		target = target.substring(idx + 1);

		idx = target.indexOf('/');
		if (idx == -1) {
			if (isKnownModule(moduleName)) {
				return Optional.of(new ListingResponse(findVersions(moduleName)));
			}
			return Optional.empty();
		}
		String version = target.substring(0, idx);
		if (!findVersions(moduleName).contains(version)) {
			// TODO: List versions
			LOGGER.info(String.format("The requested module '%s' version '%s' doesn't match current available versions '%s'.", moduleName, version, String.join(", ", findVersions(moduleName))));
			return Optional.empty();
		}

		if (!isKnownVersion(moduleName, version)) {
			LOGGER.info(String.format("The requested module '%s' version '%s' wasn't found.", moduleName, version));
			return Optional.empty();
		}

		if (target.endsWith(".module")) {
			return Optional.of(new StringResponse(new Gson().toJson(getGradleModuleMetadata(moduleName, version))));
		}
		String result = handle(moduleName, version, target);
		if (result != null) {
			return Optional.of(new StringResponse(result));
		}

		return Optional.empty();
	}
}
