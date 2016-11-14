package net.earthcomputer.meme.gradle.tasks;

import java.io.File;
import java.nio.file.Path;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.common.base.Throwables;

import net.earthcomputer.meme.diff.IPatchFileFormat;
import net.earthcomputer.meme.diff.InvalidPatchFormatException;
import net.earthcomputer.meme.diff.PatchFileFormats;
import net.earthcomputer.meme.diff.Patcher;

public class ApplyPatches extends AbstractRecursiveFileOperation {

	@InputDirectory
	private Object baseDir;

	@InputDirectory
	private Object patchDir;

	@Input
	@Optional
	private String patchFileFormat = "text";

	@OutputDirectory
	private Object workDir;

	@TaskAction
	public void doTask() {
		File baseDir = getProject().file(this.baseDir);
		Path baseDirPath = baseDir.toPath();
		File patchDir = getProject().file(this.patchDir);
		Path patchDirPath = patchDir.toPath();
		File workDir = getProject().file(this.workDir);
		Path workDirPath = workDir.toPath();
		IPatchFileFormat patchFileFormat = PatchFileFormats.byName(this.patchFileFormat);

		recurse(baseDir, baseFile -> {
			Path relativePath = baseDirPath.relativize(baseFile.getParentFile().toPath());
			File patchFile = new File(patchDirPath.resolve(relativePath).toFile(), baseFile.getName() + ".mpatch");
			File workFile = new File(workDirPath.resolve(relativePath).toFile(), baseFile.getName());
			try {
				new Patcher.Builder<Object>().setPatchFileFormat(patchFileFormat).setBaseFile(baseFile)
						.setPatchFile(patchFile).setOutputFile(workFile).build().writeWorkFile();
			} catch (InvalidPatchFormatException e) {
				throw Throwables.propagate(e);
			}
		});
	}

	public Object getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(Object baseDir) {
		this.baseDir = baseDir;
	}

	public Object getPatchDir() {
		return patchDir;
	}

	public void setPatchDir(Object patchDir) {
		this.patchDir = patchDir;
	}

	public String getPatchFileFormat() {
		return patchFileFormat;
	}

	public void setPatchFileFormat(String patchFileFormat) {
		this.patchFileFormat = patchFileFormat;
	}

	public Object getWorkDir() {
		return workDir;
	}

	public void setWorkDir(Object workDir) {
		this.workDir = workDir;
	}

}
