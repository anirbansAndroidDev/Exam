package ica.Utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class DownloaderService {

	public static boolean remoteHTTPDownloader(String VirtualPath,
			String PhysicalPath) {

		boolean idDownloadSuccessful = false;

		File PhysicalFile = new File(PhysicalPath);

		if (PhysicalFile.exists()) {
			PhysicalFile.delete();
		}

		// InputStream in = null;
		String urlString = VirtualPath;
		BufferedInputStream bis = null;

		try {

			urlString = urlString.replaceAll(" ", "%20");

			URI uri = new URI(urlString);

			// URL url = new URL(urlString.replace(" ", "%20"));
			HttpGet httpRequest = null;

			httpRequest = new HttpGet(uri);

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient
					.execute(httpRequest);

			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream input = bufHttpEntity.getContent();

			// URL url = new URL(urlString);
			// URLConnection ucon = url.openConnection();

			// in = ucon.getInputStream();
			bis = new BufferedInputStream(input);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;

			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(PhysicalFile);
			fos.write(baf.toByteArray());
			fos.close();
			bis.close();
			// in.close();

			if (PhysicalFile.exists()) {
				idDownloadSuccessful = true;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return idDownloadSuccessful;
	}

	public static boolean remoteSampleHTTPDownloader(String VirtualPath,
			String PhysicalPath) {

		boolean idDownloadSuccessful = false;

		File PhysicalFile = new File(PhysicalPath);

		if (PhysicalFile.exists()) {
			PhysicalFile.delete();
		}

		AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

		// /
		String urlString = VirtualPath;
		URI uri = null;
		try {
			uri = new URI(urlString.replace(" ", "%20"));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// /

		HttpGet getRequest = new HttpGet(uri);

		try {
			HttpResponse response = client.execute(getRequest);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("PDF Downloader", "Error " + statusCode
						+ " while retrieving pdf from " + VirtualPath);

			}

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream inputStream = null;
				try {

					inputStream = entity.getContent();

					// /save stream to file

					try {

						FileOutputStream fos = new FileOutputStream(
								PhysicalPath, false);

						OutputStream os = new BufferedOutputStream(fos);

						byte[] buffer = new byte[1024];

						int byteRead = 0;

						while ((byteRead = inputStream.read(buffer)) != -1) {
							Log.e("byte reading", Integer.toString(byteRead));
							os.write(buffer, 0, byteRead);
						}

						fos.close();

						Log.e("Size", Long.toString(PhysicalFile.length()));

						if (PhysicalFile.exists() && PhysicalFile.length() > 0) {
							idDownloadSuccessful = true;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
					getRequest.abort();
				}
			}
		} catch (Exception e) {

			try {
				getRequest.abort();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();

		}

		finally {
			if (client != null) {
				client.close();
			}
		}
		return idDownloadSuccessful;
	}

	public static boolean download(String VirtualPath, String PhysicalPath) {

		final int MAX_BUFFER_SIZE = 1024; // 1kb
		final int DOWNLOADING = 0;
		final int COMPLETE = 1;

		HttpURLConnection conn;
		InputStream stream; // to read
		ByteArrayOutputStream out; // to write

		double fileSize;
		double downloaded = 0; // number of bytes downloaded
		int status = DOWNLOADING; // status of current process

		boolean idDownloadSuccessful = false;

		try {
			String filename = PhysicalPath;

			File PhysicalFile = new File(PhysicalPath);

			if (PhysicalFile.exists()) {
				PhysicalFile.delete();
			}

			conn = (HttpURLConnection) new URL(VirtualPath.replace(" ", "%20"))
					.openConnection();
			fileSize = conn.getContentLength();
			out = new ByteArrayOutputStream((int) Math.ceil(fileSize));
			conn.connect();

			stream = conn.getInputStream();
			// loop with step
			while (status == DOWNLOADING) {
				byte buffer[];

				if (fileSize - downloaded > MAX_BUFFER_SIZE) {
					buffer = new byte[MAX_BUFFER_SIZE];
				} else {
					buffer = new byte[(int) (fileSize - downloaded)];
				}
				int read = stream.read(buffer);

				if (read == -1) {

					break;
				}
				// writing to buffer
				out.write(buffer, 0, read);
				downloaded += read;
				// update progress bar

			} // end of while

			if (status == DOWNLOADING) {
				status = COMPLETE;
			}

			try {
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(out.toByteArray());
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();

			}

			if (PhysicalFile.exists()) {
				idDownloadSuccessful = true;
			}

		} catch (Exception e) {
			e.printStackTrace();

		}// end of catch

		return idDownloadSuccessful;
	}

	// Use This If URL Contains Space and other Special Characters
	public static boolean DownloadFile(String fileURL, String directoryPath) {
		boolean idDownloadSuccessful = false;

		File directory = new File(directoryPath);

		if (directory.exists()) {
			directory.delete();
		}

		try {

			FileOutputStream f = new FileOutputStream(directory);
			URL u = new URL(fileURL);
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("User-agent", "Mozilla/4.0");
			c.setDoOutput(true);
			c.connect();

			InputStream in = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0) {
				f.write(buffer, 0, len1);
			}
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (directory.exists() && directory.length() > 0) {
			idDownloadSuccessful = true;
		}

		return idDownloadSuccessful;
	}

	public static boolean downloadStudyMatPDF(String fileURL,
			String directoryPath) {

		boolean idDownloadSuccessful = false;

		File directory = new File(directoryPath);

		if (directory.exists()) {
			directory.delete();
		}
		String urlString = fileURL;
		try {
			urlString = urlString.replaceAll(" ", "%20");

			URL url = new URL(urlString);
			HttpGet httpRequest = null;

			httpRequest = new HttpGet(url.toURI());

			HttpClient httpclient = new DefaultHttpClient();

			HttpResponse response = (HttpResponse) httpclient
					.execute(httpRequest);

			HttpEntity entity = response.getEntity();

			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);

			InputStream input = bufHttpEntity.getContent();

			byte[] buffer = new byte[1024];
			int len1 = 0;

			FileOutputStream f = new FileOutputStream(directory);

			while ((len1 = input.read(buffer)) > 0) {
				f.write(buffer, 0, len1);
				f.flush();
			}

			input.close();

		} catch (MalformedURLException e) {
			Log.e("PDF Download", "bad url", e);
		} catch (Exception e) {
			Log.e("PDF Download", "io error", e);
		}

		if (directory.exists() && directory.length() > 0) {
			idDownloadSuccessful = true;
		}

		return idDownloadSuccessful;

	}

	public static Bitmap downloadBitmap(String url) {
		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);
					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or
			// IllegalStateException
			getRequest.abort();
			e.printStackTrace();
			// Log.w("ImageDownloader", "Error while retrieving bitmap from " +
			// url, e.toString());
		}

		finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

}
