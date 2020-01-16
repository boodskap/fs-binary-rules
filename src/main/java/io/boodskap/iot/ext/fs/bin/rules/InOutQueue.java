package io.boodskap.iot.ext.fs.bin.rules;

import java.util.concurrent.LinkedBlockingQueue;

public class InOutQueue {

	private static final LinkedBlockingQueue<QueuedFile> inQ = new LinkedBlockingQueue<QueuedFile>();
	private static final LinkedBlockingQueue<QueuedFile> outQ = new LinkedBlockingQueue<QueuedFile>();
	
	private InOutQueue() {
	}

	public static LinkedBlockingQueue<QueuedFile> getInq() {
		return inQ;
	}

	public static LinkedBlockingQueue<QueuedFile> getOutq() {
		return outQ;
	}

}
