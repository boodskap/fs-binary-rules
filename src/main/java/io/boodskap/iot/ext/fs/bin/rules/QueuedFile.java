package io.boodskap.iot.ext.fs.bin.rules;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

public class QueuedFile implements Serializable{

	private static final long serialVersionUID = 8629012402907999154L;
	
	private final long createdStamp;
	private final String rule;
	private final String file;
	private String md5;
	
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

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	public String computeMD5() throws FileNotFoundException, IOException {
		return this.md5 = DigestUtils.md5Hex(new FileInputStream(file));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (createdStamp ^ (createdStamp >>> 32));
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((md5 == null) ? 0 : md5.hashCode());
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
		if (md5 == null) {
			if (other.md5 != null)
				return false;
		} else if (!md5.equals(other.md5))
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
		return "QueuedFile [createdStamp=" + new Date(createdStamp) + ", rule=" + rule + ", file=" + file + ", md5=" + md5 + "]";
	}

}

