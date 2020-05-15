package dev.nokee.runtime.base.internal.repositories;

import com.google.gson.Gson;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class AbstractRouteHandler implements RouteHandler {
	private static final Logger LOGGER = Logger.getLogger(AbstractRouteHandler.class.getName());

	public abstract String getContextPath();

	protected abstract boolean isKnownModule(String moduleName);
	protected abstract boolean isKnownVersion(String moduleName, String version);

	// TODO: Maybe rename to listVersions(...):MavenVersionListing
	protected abstract List<String> findVersions(String moduleName);

	protected abstract GradleModuleMetadata getResourceMetadata(String moduleName, String version);

	// TODO: Maybe rename to getResource(...): MavenResource
	protected abstract String handle(String moduleName, String version, String target);

	@Override
	public final Optional<Response> handle(String target) {
		target = target.substring(getContextPath().length() + 1); // Assumes '/' at the end of context path

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
			return Optional.of(new StringResponse(new Gson().toJson(getResourceMetadata(moduleName, version))));
		}
		String result = handle(moduleName, version, target);
		if (result != null) {
			return Optional.of(new StringResponse(result));
		}

		return Optional.empty();
	}
}
