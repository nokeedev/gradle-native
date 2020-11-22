package dev.gradleplugins.exemplarkit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.File;

@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ExemplarExecutionContext {
	File workingDirectory;
	Exemplar exemplar;
}
