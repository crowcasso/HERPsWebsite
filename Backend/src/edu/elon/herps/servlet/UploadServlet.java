package edu.elon.herps.servlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.jdo.PersistenceManager;
import javax.persistence.EntityManager;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import edu.elon.herps.NameValuePair;
import edu.elon.herps.UploadData;
import edu.elon.herps.UploadData.Picture;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ServletFileUpload upload = new ServletFileUpload();

		try {
			FileItemIterator iterator = upload.getItemIterator(req);
			UploadData toUpload = null;

			if (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();
				ObjectInputStream ois = new ObjectInputStream(stream);
				toUpload = (UploadData)ois.readObject();
			}
			if (toUpload != null) {
				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

				LinkedList<Key> pictureKeys = new LinkedList<Key>();				
				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					InputStream stream = item.openStream();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int read;
					while ((read = stream.read(buffer, 0, buffer.length)) > 0) {
						baos.write(buffer, 0, read);
					}
					byte[] bytes = baos.toByteArray();

					System.out.println("PICTURES");
					Entity entity = new Entity("Picture");
					Blob blob = new Blob(bytes);
					entity.setProperty("data", blob);
					datastore.put(entity);

					pictureKeys.add(entity.getKey());
				}

				String category = toUpload.get(0).value.toString();
				System.out.println("Uploading " + category + "...");

				Query q = new Query(category);
				FetchOptions fo = FetchOptions.Builder.withLimit(1);
				int entries = datastore.prepare(q).countEntities(fo);

				if (entries == 0) {
					Entity entity = new Entity(category);

					System.out.println("entries == 0");

					int slot = 0;
					for (int i = 0; i < toUpload.size(); i++) {
						NameValuePair nvp = toUpload.get(i);
						System.out.println("0: " + nvp.name);
						if (nvp.name.length() == 0) nvp.name = " ";
						setEntityProperty(entity, nvp.name, slot++);
					}
					entity.setProperty("order", "true");
					datastore.put(entity);
				}

				Entity entity = new Entity(category);

				int pictureIndex = 0;
				for (int i = 0; i < toUpload.size(); i++) {
					NameValuePair nvp = toUpload.get(i);
					if (nvp != null && nvp.value instanceof Picture) {
						System.out.println("Picture: " + nvp.name);
						setEntityProperty(entity, nvp.name, 
								pictureKeys.get(pictureIndex++));
					} else {
						if (nvp.name.length() != 0) {
							System.out.println("Other: " + nvp.name);
							setEntityProperty(entity, nvp.name, nvp.value);
						}
					}
				}

				System.out.println("putting entity");
				entity.setProperty("order", "false");
				datastore.put(entity);	

				resp.getWriter().println("true");
			} else {
				System.out.println("FALSE");
			}
		} catch (Exception e) {
			System.out.println("EXCEPTION" + e);
			e.printStackTrace();
		}
	}

	private void setEntityProperty(Entity entity, String name, Object value) {

		if (value instanceof String) {
			if (((String)value).length() > 300) {
				value = new Text((String)value);
			}
		}

		int index = 1;
		String newName = name;
		while (entity.getProperties().containsKey(newName)) {
			index++;
			newName = name + " (" + index + ")"; 
		}
		entity.setProperty(newName, value);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getWriter().println("!");
	}
}
