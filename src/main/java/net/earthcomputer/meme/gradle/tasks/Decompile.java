package net.earthcomputer.meme.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import net.earthcomputer.meme.util.DecompilerUtils;

public class Decompile extends DefaultTask {

	@InputFile
	private Object toDecompile;

	@InputFile
	private Object mappings;

	@OutputDirectory
	private Object output;

	@TaskAction
	public void doTask() {
		DecompilerUtils.decompile(getProject().file(toDecompile), getProject().file(mappings),
				getProject().file(output));
	}

	public Object getToDecompile() {
		return toDecompile;
	}

	public void setToDecompile(Object toDecompile) {
		this.toDecompile = toDecompile;
	}

	public Object getMappings() {
		return mappings;
	}

	public void setMappings(Object mappings) {
		this.mappings = mappings;
	}

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}

}
