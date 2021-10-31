package dev.nokee.language.c;

import dev.nokee.language.base.testers.HasDestinationDirectoryTester;
import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.nativebase.HasObjectFilesTester;
import dev.nokee.language.nativebase.NativeSourceCompileTester;

public interface CCompileTester extends NativeSourceCompileTester, HasDestinationDirectoryTester, HasObjectFilesTester {
	CCompile subject();
}
