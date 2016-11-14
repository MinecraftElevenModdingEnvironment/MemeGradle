package net.earthcomputer.meme.gradle.tasks;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import net.earthcomputer.meme.util.MinecraftJarMerger;

public class DownloadServer extends DefaultTask {

	@OutputFile
	private Object to;

	@TaskAction
	public void doTask() throws IOException {
		MinecraftJarMerger.downloadServer(getProject().file(to));
	}

	public Object getTo() {
		return to;
	}

	public void setTo(Object to) {
		this.to = to;
	}

}
