package edu.elon.herps.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class PictureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String sId = req.getParameter("id");
		long id = 0;
		try {
			id = Long.parseLong(sId);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Picture");
		Key key = new KeyFactory.Builder("Picture", id).getKey();
		
		q.addFilter("Key", FilterOperator.EQUAL, key.toString());
		Entity picture = null;
		try {
			picture = datastore.get(key);
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (picture != null) {
			Blob blob = (Blob)picture.getProperty("data");
			resp.setContentType("image/jpeg");
			resp.getOutputStream().write(blob.getBytes());
		}
	}
}
