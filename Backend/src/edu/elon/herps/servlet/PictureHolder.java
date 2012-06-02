package edu.elon.herps.servlet;

import java.io.Serializable;

public class PictureHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	public long id;
	public PictureHolder(long id) {
		this.id = id;
	}
}
