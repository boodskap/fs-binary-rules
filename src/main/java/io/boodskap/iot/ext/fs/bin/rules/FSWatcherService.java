package io.boodskap.iot.ext.fs.bin.rules;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSWatcherService implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(FSWatcherService.class);

	public static interface Handler{
		public void handle(String inout, String rule, Path path);
	}

	private final Handler handler;
	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	
	/**
	 * Creates a WatchService and registers the given directory
	 */
	FSWatcherService(Path dir, Handler handler) throws IOException {
		this.handler = handler;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();

		walkAndRegisterDirectories(dir);
	}

	/**
	 * Register the given directory with the WatchService; This function will be
	 * called by FileVisitor
	 */
	private void registerDirectory(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void walkAndRegisterDirectories(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				LOG.info(String.format("Registering dir:%s for input", dir));
				registerDirectory(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				
				final File bfile = file.toFile();
				final String inout = bfile.getParentFile().getParentFile().getName();
				final String rule = bfile.getParentFile().getName();
				
				LOG.debug(String.format("Existing file type:%s rule:%s file:%s", inout, rule, file));
				
				handler.handle(inout, rule, file);
				
				return FileVisitResult.CONTINUE;
			}
			
		});
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public void run() {
		
		while (!Thread.currentThread().isInterrupted()) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}
			
			

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();

				// Context for directory entry event is the file name of entry
				@SuppressWarnings("unchecked")
				Path name = ((WatchEvent<Path>) event).context();
				Path child = dir.resolve(name);

				// print out event
				LOG.debug(String.format("%s: %s\n", kind.name(), child));

				
				// if directory is created, and watching recursively, then register it and its
				// sub-directories
				if (kind == ENTRY_CREATE) {
					try {
						if (Files.isDirectory(child)) {
							
							walkAndRegisterDirectories(child);
							
						}else {
							
							final File bfile = child.toFile();
							final String inout = bfile.getParentFile().getParentFile().getName();
							final String rule = bfile.getParentFile().getName();
							
							LOG.debug(String.format("New file type:%s rule:%s file:%s", inout, rule, child));
							
							handler.handle(inout, rule, child);
						}

					} catch (IOException x) {
						LOG.error("Processing Failed", x);
					}
				}
				
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

}
