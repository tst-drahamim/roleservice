import	java.io.InputStream;
import	java.io.InputStreamReader;
import	java.io.BufferedReader;
import	java.io.OutputStream;
import	java.io.PrintWriter;
import	java.io.IOException;
import	java.io.UnsupportedEncodingException;

import	java.util.Set;
import	java.util.Map;
import	java.util.Iterator;

import	java.net.Socket;
import	java.net.ServerSocket;

public class
RoleThread
	implements	Runnable
{
	private int					port;
	private String				encoding = "UTF-8";
	private boolean				debug;
	private ServerSocket		serverSocket;
	private Map<String, String>	roleMap;

	// constructor

	RoleThread(
		ServerSocket		socket,
		int					port,
		Map<String, String>	roleMap,
		boolean				debug)
	{
		serverSocket = socket;
		this.debug   = debug;
		this.port    = port;
		this.roleMap = roleMap;
	}

	// thread entry point

	public void
	run()
	{
		try {
			// loop forever waiting for connections
			for (;;) {
				Socket	clientSocket = serverSocket.accept();

				if (debug) {
					System.out.println("got connection on port " +
						serverSocket.getLocalPort());
				}

				handle(clientSocket);

				clientSocket.close();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}	
	}

	// handle connection from client

	private void
	handle(
		Socket	clientSocket)
		throws	IOException
	{
		// got client connection, get reader & writer

		BufferedReader	in  = getReader(clientSocket);
		OutputStream	out = clientSocket.getOutputStream();

		// process request

		process(in, out);

		// clean up

		in.close();
		out.close();
	}

	// gather request and return response
	
	private void
	process(
		BufferedReader	browserIn,		// input from browser
		OutputStream	browserStream)	// output to browser
		throws			IOException
	{
		String			line;
		String[]		tokens;
		String			get        = null;
		boolean			post       = false;
		PrintWriter		browserOut = getStreamWriter(browserStream);

		// read request, a line at a time

		while ((line = browserIn.readLine()) != null) {
			if (line.length() == 0) {
				// blank line, end of HTTP request
				break;
			}

			// split line into tokens separated by spaces

			tokens = line.split(" ");

			// when we find the GET line, keep arguments and print out

			if (tokens[0].equals("GET")) {
				get = tokens[1];
				if (debug) {
					System.err.println(line);
				}
			}

			// when we find a POST line, print it out too

			if (tokens[0].equals("POST")) {
				post = true;
				if (debug) {
					System.err.println(line);
				}
			}
		}

		if ((get == null) && !post) {
			System.err.println("no GET or POST line found");
			return;
		}

		String	roleString = getRole();

		try {
			byte[]	roleBytes = roleString.getBytes(encoding);

			browserOut.println("HTTP/1.1 200 OK");
			browserOut.println("Content-Type: application/json; charset=utf-8");
			browserOut.println("Content-Length: " + roleBytes.length);
			browserOut.println("Server: roleservice/J");
			// browserOut.println("Date: Mon, 04 Feb 2013 22:06:54 GMT");

			browserOut.println();

			browserStream.write(roleBytes, 0, roleBytes.length);
		} catch (UnsupportedEncodingException ex) {
			System.err.println("can't encode roles as " + encoding);
			browserOut.println("HTTP/1.1 500 can't encode roles");
			return;
		}
	}

	private String
	getRole()
	{
		Set<String> 		userSet      = roleMap.keySet();
		Iterator<String>	userIterator = userSet.iterator();

		// just return the first one for now

		if (userIterator.hasNext()) {
			String user = userIterator.next();

			if (debug) {
				System.out.println("returning roles for user " + user);
			}

			return(roleMap.get(user));
		}

		if (debug) {
			System.out.println("no users found, returning blank roles");
		}

		return("");
	}

	// get a Reader for a Socket
	
	private BufferedReader
	getReader(
		Socket	socket)
		throws	IOException
	{
		return(getStreamReader(socket.getInputStream()));
	}
	
	// get a Writer for a Socket
	
	private PrintWriter
	getWriter(
		Socket	socket)
		throws	IOException
	{
		return(getStreamWriter(socket.getOutputStream()));
	}

	// get a Reader for a stream

	private BufferedReader
	getStreamReader(
		InputStream	inputStream)
	{
		return(new BufferedReader(new InputStreamReader(inputStream)));
	}

	// get a Writer for a stream

	private PrintWriter
	getStreamWriter(
		OutputStream	outputStream)
	{
		return(new PrintWriter(outputStream, true));
	}
}
