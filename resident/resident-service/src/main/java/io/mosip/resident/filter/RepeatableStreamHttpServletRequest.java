package io.mosip.resident.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

import io.mosip.resident.util.ObjectWithMetadata;

/**
 * The request wrapper used in filter that allows to re-read the request
 * body.
 *
 * @author Loganathan Sekar
 */
public class RepeatableStreamHttpServletRequest extends HttpServletRequestWrapper implements ObjectWithMetadata {

	/** The raw data. */
	private byte[] rawData;

	/** The request. */
	private HttpServletRequest request;

	/** The servlet stream. */
	private RepeatableServletInputStream servletStream;
	
	private Map<String, Object> metadata;
	
	/**
	 * Instantiates a new resettable stream http servlet request.
	 *
	 * @param request the request
	 */
	public RepeatableStreamHttpServletRequest(HttpServletRequest request) {
		super(request);
		this.request = request;
		this.servletStream = new RepeatableServletInputStream();
	}

	/**
	 * Reset input stream.
	 */
	public void resetInputStream() {
		servletStream.stream = createServletInputStream();
	}

	private RepeatableServletInputStream createServletInputStream() {
		return new RepeatableServletInputStream(new ByteArrayInputStream(rawData));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequestWrapper#getInputStream()
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (rawData == null) {
			rawData = StreamUtils.copyToByteArray(this.request.getInputStream());
			servletStream.stream = createServletInputStream();
		}
		RepeatableServletInputStream servletStreamOldRef = servletStream;
		//Reset the servlet stream with a new copy
		resetInputStream();
		//Return the old copy of servlet stream
		return servletStreamOldRef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequestWrapper#getReader()
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		if (rawData == null) {
			rawData = StreamUtils.copyToByteArray(this.request.getInputStream());
			servletStream.stream = createServletInputStream();
		}
		RepeatableServletInputStream servletStreamOldRef = servletStream;
		//Reset the servlet stream with a new copy
		resetInputStream();
		//Return the reader with the old copy of servlet stream
		return new BufferedReader(new InputStreamReader(servletStreamOldRef));
	}

	/**
	 * Replace the request data with the given bytes
	 * 
	 * @param newData the new data to be replaced with
	 */
	public void replaceData(byte[] newData) {
		rawData = newData;
		servletStream.stream = new ByteArrayInputStream(rawData);
	}

	/**
	 * The Class RepeatableServletInputStream - used in
	 * ResettableStreamHttpServletRequest
	 */
	private class RepeatableServletInputStream extends ServletInputStream {

		/** The stream. */
		private InputStream stream;

		/** The eof reached. */
		private boolean eofReached;

		/** The closed. */
		private boolean closed;

		/**
		 * Instantiates a new resettable servlet input stream.
		 *
		 * @param stream the stream
		 */
		public RepeatableServletInputStream(InputStream stream) {
			this.stream = stream;
		}

		/**
		 * Instantiates a new resettable servlet input stream.
		 */
		public RepeatableServletInputStream() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			int val = stream.read();
			if (val == -1) {
				eofReached = true;
			}
			return val;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.servlet.ServletInputStream#isFinished()
		 */
		@Override
		public boolean isFinished() {
			return eofReached;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.servlet.ServletInputStream#isReady()
		 */
		@Override
		public boolean isReady() {
			return !eofReached && !closed;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#close()
		 */
		@Override
		public void close() throws IOException {
			super.close();
			closed = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.servlet.ServletInputStream#setReadListener(javax.servlet.
		 * ReadListener)
		 */
		@Override
		public void setReadListener(ReadListener listener) {
			// Nothing to do
		}
	}

	@Override
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
		
	}	
	

}
