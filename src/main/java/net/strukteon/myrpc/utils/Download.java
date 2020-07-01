package net.strukteon.myrpc.utils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable {
	public static final String[] STATUSES;
	public static final int DOWNLOADING = 0;
	public static final int COMPLETE = 1;
	public static final int CANCELLED = 2;
	public static final int ERROR = 3;
	static {
		STATUSES = new String[] { "Downloading", "Complete", "Cancelled", "Error" };
	}
	private URL url;
	private File destination;
	private int size;
	private int downloaded;

	private int status;

	public Download(final URL url, final File destination) {
		this.url = url;
		this.size = -1;
		this.downloaded = 0;
		this.status = 0;
		this.destination = destination;
		this.download();
	}

	public void cancel() {
		this.status = 2;
		this.stateChanged();
	}

	private void download() {
		final Thread thread = new Thread(this);
		thread.start();
	}

	private void error() {
		this.status = 3;
		this.stateChanged();
	}

	public float getProgress() {
		return this.downloaded / (float) this.size * 100.0f;
	}

	public int getSize() {
		return this.size;
	}

	public int getStatus() {
		return this.status;
	}

	public String getUrl() {
		return this.url.toString();
	}

	public void resume() {
		this.status = 0;
		this.stateChanged();
		this.download();
	}

	@Override
	public void run() {
		RandomAccessFile file = null;
		InputStream stream = null;
		try {
			final HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
			connection.setRequestProperty("Range", "bytes=" + this.downloaded + "-");
			connection.connect();
			if (connection.getResponseCode() / 100 != 2) {
				this.error();
			}
			final int contentLength = connection.getContentLength();
			if (contentLength < 1) {
				this.error();
			}
			if (this.size == -1) {
				this.size = contentLength;
				this.stateChanged();
			}
			file = new RandomAccessFile(this.destination, "rw");
			file.seek(this.downloaded);
			stream = connection.getInputStream();
			while (this.status == 0) {
				byte[] buffer;
				if (this.size - this.downloaded > 1024) {
					buffer = new byte[1024];
				} else {
					buffer = new byte[this.size - this.downloaded];
				}
				final int read = stream.read(buffer);
				if (read == -1) {
					break;
				}
				file.write(buffer, 0, read);
				this.downloaded += read;
				this.stateChanged();
			}
			if (this.status == 0) {
				this.status = 1;
				this.stateChanged();
			}
		} catch (Exception e) {
			this.error();
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (Exception ex) {
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex2) {
				}
			}
		}
	}

	private void stateChanged() {
		this.setChanged();
		this.notifyObservers();
	}
}
