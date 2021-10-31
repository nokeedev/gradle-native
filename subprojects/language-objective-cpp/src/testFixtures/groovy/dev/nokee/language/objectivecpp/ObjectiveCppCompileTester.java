package dev.nokee.language.objectivecpp;

import dev.nokee.language.base.testers.HasDestinationDirectoryTester;
import dev.nokee.language.nativebase.HasObjectFilesTester;
import dev.nokee.language.nativebase.NativeSourceCompileTester;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;

public interface ObjectiveCppCompileTester extends NativeSourceCompileTester, HasDestinationDirectoryTester, HasObjectFilesTester {
	ObjectiveCppCompile subject();
}
