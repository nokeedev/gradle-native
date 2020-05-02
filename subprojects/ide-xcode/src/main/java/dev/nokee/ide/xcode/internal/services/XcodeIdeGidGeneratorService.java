package dev.nokee.ide.xcode.internal.services;

import dev.nokee.ide.xcode.internal.xcodeproj.GidGenerator;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;
import java.util.Collections;

public abstract class XcodeIdeGidGeneratorService extends GidGenerator implements BuildService<BuildServiceParameters.None> {
	@Inject
	public XcodeIdeGidGeneratorService() {
		super(Collections.emptySet());
	}
}
