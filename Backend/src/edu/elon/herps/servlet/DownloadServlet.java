package edu.elon.herps.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableHyperlink;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.urlfetch.HTTPResponse;

import edu.elon.herps.NameValuePair;
import edu.elon.herps.UploadData;


@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		String category = req.getParameter("category");
		String view = req.getParameter("view");
		String pictureAction = req.getParameter("pictures");
		
		if (category == null || view == null || pictureAction == null) {
			return;
		}

		Query q = new Query(category);
		q.addFilter("order", FilterOperator.EQUAL, "true");
		Entity order = datastore.prepare(q).asSingleEntity();
		if (order == null) {
			resp.getWriter().println("No data for " + category);
			return;
		}

		final Map<String, Object> map = order.getProperties();
		LinkedList<String> headers = new LinkedList<String>();
		for (String key : map.keySet()) headers.add(key);
		headers.remove("order");
		Collections.sort(headers, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return ((Long)map.get(o1)).compareTo((Long)map.get(o2));
			}
		});

		q = new Query("Aquatic Turtle");
		q.addFilter("order", FilterOperator.NOT_EQUAL, "true");
		Iterable<Entity> results = datastore.prepare(q).asIterable();

		boolean embed = pictureAction.equals("embed");
		if (view.equals("web")) {
			writeHTML(resp, headers, results, embed);
		} else if (view.equals("xls")) {
			writeExcel(req, resp, headers, results, embed);
		}
	}

	private void writeExcel(HttpServletRequest req, HttpServletResponse resp,
			LinkedList<String> headers, Iterable<Entity> results,
			boolean embedPictures) throws IOException {
		String filename = embedPictures ? "data.zip" : "data.xls";
		resp.setHeader("Content-disposition", "attachment; filename=" + filename);

		String picture = req.getRequestURL().toString();
		picture = picture.substring(0, picture.lastIndexOf("/")) + "/picture?id=";
		
		OutputStream output;
		ZipOutputStream zos = null;
		
		if (embedPictures) {
			zos = new ZipOutputStream(resp.getOutputStream());
			ZipEntry entry = new ZipEntry("data.xls");
			zos.putNextEntry(entry); 
			output = zos;
		} else {
			output = resp.getOutputStream();
		}
		
		LinkedList<Long> pictures = new LinkedList<Long>();
		
		WritableWorkbook wb = Workbook.createWorkbook(output);
		WritableSheet sheet = wb.createSheet("Page 1", 0);

		try {
			int c = 0;
			WritableFont bold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
			WritableCellFormat format = new WritableCellFormat(bold);
			for (String header : headers) {
				Label label = new Label(c++, 0, header);
				label.setCellFormat(format);
				sheet.addCell(label);
			}

			int r = 1;
			for (Entity data : results) {
				c = 0;
				for (String header : headers) {
					Object value = data.getProperty(header);
					if (value != null) {
						WritableCell cell = null;
						if (value instanceof Key) {
							long id = ((Key)value).getId();
							if (embedPictures) {
								sheet.addHyperlink(new WritableHyperlink(c, r, 
										new File("pictures/" + id + ".jpg")));
								pictures.add(id);
								
							} else {
								sheet.addHyperlink(new WritableHyperlink(c, r, 
										new URL(picture + id)));								
							}
						} else if (value instanceof Long) {
							cell = new Number(c, r, ((Long)value).longValue());
						} else if (value instanceof Integer) {
							cell = new Number(c, r, ((Integer)value).intValue());
						} else if (value instanceof Double) {
							cell = new Number(c, r, (Double)value);
						} else if (value instanceof Date) {
							cell = new jxl.write.DateTime(c, r, (Date)value);
						} else {
							cell = new Label(c, r, toString(value));
						}
						if (cell != null) sheet.addCell(cell);
					}
					c++;
				}
				r++;
			}

			wb.write();
			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (embedPictures) {
			final ZipOutputStream fzos = zos;
			
			for (Long id : pictures) {
				ZipEntry entry = new ZipEntry("pictures/" + id + ".jpg");
				zos.putNextEntry(entry);
				
				RequestDispatcher d = req.getRequestDispatcher("picture?id=" + id);
				HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(resp) {
					@Override
					public ServletOutputStream getOutputStream() {
						return new ServletOutputStream() {
							@Override
							public void write(int b) throws IOException {
								fzos.write(b);
							}
							
							@Override
							public void write(byte[] b, int off, int len) 
							throws IOException {
								fzos.write(b, off, len);
							}
						};
					}
				};
				try {
					d.include(req, wrapper);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			zos.closeEntry();
			zos.close();
		}
	}

	private void writeHTML(HttpServletResponse resp, LinkedList<String> headers, 
			Iterable<Entity> results, boolean embedPictures) throws IOException {
		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();

		writer.println("<!DOCTYPE html><html><body><table width='400%'>");

		writer.print("<tr>");
		for (String header : headers) {
			writer.print("<th>" + header + "</th>");
		}
		writer.println("</tr>");		

		for (Entity data : results) {
			writer.print("<tr>");
			for (String header : headers) {
				Object value = data.getProperty(header);
				if (value != null) {
					if (value instanceof Key) {
						String link = "picture?id=" + ((Key)value).getId();
						String content = String.format(embedPictures ? 
								"<img src='%s' width='150'/>" : "%s", link);
						writer.printf("<td><a href='%s'>%s</a></td>",
								link, content);
					} else {
						writer.print("<td>" + toString(value) + "</td>");
					}
				}
			}
			writer.println("</tr>");
		}
		writer.println("</table></body></html>");
	}

	private static String toString(Object o) {
		return o == null ? "" : o.toString();
	}
}
