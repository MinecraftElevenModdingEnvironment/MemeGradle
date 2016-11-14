package net.earthcomputer.meme.gradle.tasks;

import java.io.File;
import java.nio.file.Path;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.common.base.Throwables;

import net.earthcomputer.meme.diff.DiffFinder;
import net.earthcomputer.meme.diff.DiffFormats;
import net.earthcomputer.meme.diff.IDiffFormat;
import net.earthcomputer.meme.diff.IPatchFileFormat;
import net.earthcomputer.meme.diff.PatchFileFormats;

public class FindDiff extends AbstractRecursiveFileOperation {

	@InputDirectory
	private Object baseDir;

	@InputDirectory
	private Object workDir;

	@Input
	@Optional
	private String patchFileFormat = "text";

	@OutputDirectory
	private Object patchDir;

	@SuppressWarnings("unchecked")
	@TaskAction
	public void doTask() {
		File baseDir = getProject().file(this.baseDir);
		Path baseDirPath = baseDir.toPath();
		File workDir = getProject().file(this.workDir);
		Path workDirPath = workDir.toPath();
		File patchDir = getProject().file(this.patchDir);
		Path patchDirPath = patchDir.toPath();
		IPatchFileFormat patchFileFormat = PatchFileFormats.byName(this.patchFileFormat);
		IDiffFormat<?> normalDiffFormat = patchFileFormat == PatchFileFormats.BINARY ? DiffFormats.BYTE
				: DiffFormats.NORMAL;
		IDiffFormat<?> javaDiffFormat = patchFileFormat == PatchFileFormats.BINARY ? DiffFormats.BYTE
				: DiffFormats.JAVA;

		recurse(baseDir, baseFile -> {
			Path relativePath = baseDirPath.relativize(baseFile.getParentFile().toPath());
			File workFile = new File(workDirPath.resolve(relativePath).toFile(), baseFile.getName());
			File patchFile = new File(patchDirPath.resolve(relativePath).toFile(), baseFile.getName() + ".mpatch");
			try {
				new DiffFinder.Builder<Object>()
						.setDiffFormat(baseFile.getName().endsWith(".java") ? (IDiffFormat<Object>) javaDiffFormat
								: (IDiffFormat<Object>) normalDiffFormat)
						.setPatchFileFormat(patchFileFormat).setBaseFile(baseFile).setWorkFile(workFile)
						.setOutputFile(patchFile).build().writePatchFile();
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		});
	}

}
