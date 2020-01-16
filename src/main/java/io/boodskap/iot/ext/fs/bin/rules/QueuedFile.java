package io.boodskap.iot.ext.fs.bin.rules;

import java.io.Serializable;
import java.util.Date;

public class QueuedFile implements Serializable{

	private static final long serialVersionUID = 8629012402907999154L;
	
	private final long createdStamp;
	private final String rule;
	private final String file;
	
	public QueuedFile(String rule, String file) {
		this.createdStamp = System.currentTimeMillis();
		this.rule = rule;
		this.file = file;
	}

	public long getCreatedStamp() {
		return createdStamp;
	}

	public String getFile() {
		return file;
	}

	public String getRule() {
		return rule;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (createdStamp ^ (createdStamp >>> 32));
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueuedFile other = (QueuedFile) obj;
		if (createdStamp != other.createdStamp)
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QueuedFile [createdStamp=" + new Date(createdStamp) + ", rule=" + rule + ", file=" + file + "]";
	}

}

