package net.earthcomputer.meme.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import net.earthcomputer.meme.util.DecompilerUtils;

public class DeobfuscateJar extends DefaultTask {

	@InputFile
	private Object toDeobfuscate;

	@InputFile
	private Object mappings;

	@OutputFile
	private Object output;

	@TaskAction
	public void doTask() {
		DecompilerUtils.createDeobfuscatedJar(getProject().file(toDeobfuscate), getProject().file(mappings),
				getProject().file(output));
	}

	public Object getToDeobfuscate() {
		return toDeobfuscate;
	}

	public void setToDeobfuscate(Object toDeobfuscate) {
		this.toDeobfuscate = toDeobfuscate;
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
