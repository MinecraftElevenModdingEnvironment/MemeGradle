package net.earthcomputer.meme.gradle.tasks;

import java.io.File;
import java.util.function.Consumer;

import org.gradle.api.DefaultTask;

public abstract class AbstractRecursiveFileOperation extends DefaultTask {

	protected void recurse(File directory, Consumer<File> fileConsumer) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				recurse(file, fileConsumer);
			} else {
				fileConsumer.accept(file);
			}
		}
	}

}
