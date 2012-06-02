package edu.elon.herps;

import java.io.Serializable;
import java.util.LinkedList;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

public class UploadData extends LinkedList<NameValuePair> {
	private static final long serialVersionUID = 1L;
	public transient String fileName;
	
	public static class Picture implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public String file;
		public Picture(String file) {
			this.file = file;
		}
	}
}
