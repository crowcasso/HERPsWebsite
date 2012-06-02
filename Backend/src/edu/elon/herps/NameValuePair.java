package edu.elon.herps;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Embeddable
public class NameValuePair implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String name;
	public Object value;
	
	public NameValuePair(String name, Object value) {
		this.name = name;
		this.value = value;
	}
}
