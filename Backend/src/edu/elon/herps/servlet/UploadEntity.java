package edu.elon.herps.servlet;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

import javax.jdo.annotations.Persistent;
import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

import edu.elon.herps.NameValuePair;
import edu.elon.herps.UploadData;
import edu.elon.herps.UploadData.Picture;

@Entity
public class UploadEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	public String category;
	@Basic
	public LinkedList<String> names;
	@Basic
	public LinkedList<String> values;
	
	public UploadEntity(UploadData data) {
		names = new LinkedList<String>();
		values = new LinkedList<String>();
		int pictureIndex = 0;
		for (NameValuePair nvp : data) {
			names.add(nvp.name);
			if (nvp.value instanceof Picture) {
				values.add("" + pictureIndex++);
			} else {
				values.add(nvp.value == null ? "" : nvp.value.toString());
			}
		}
		this.category = (String)data.get(0).value;
	}
}
