package net.earthcomputer.meme.gradle.tasks;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import net.earthcomputer.meme.util.MinecraftJarMerger;

public class MergeJars extends DefaultTask {

	@InputFile
	private Object clientInput;

	@InputFile
	private Object serverInput;

	@OutputFile
	private Object output;

	@TaskAction
	public void doTask() throws IOException {
		new MinecraftJarMerger(getProject().file(clientInput), getProject().file(serverInput),
				getProject().file(output)).mergeJars();
	}

	public Object getClientInput() {
		return clientInput;
	}

	public void setClientInput(Object clientInput) {
		this.clientInput = clientInput;
	}

	public Object getServerInput() {
		return serverInput;
	}

	public void setServerInput(Object serverInput) {
		this.serverInput = serverInput;
	}

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}

}
