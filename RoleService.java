import	java.lang.StringBuffer;

import	java.io.PrintWriter;
import	java.io.InputStreamReader;
import	java.io.BufferedReader;
import	java.io.IOException;

import	java.util.Set;
import	java.util.HashMap;
import	java.util.Iterator;
import	java.util.Properties;
import	java.util.Enumeration;

import	java.net.Socket;
import	java.net.ServerSocket;

import	com.vitriol.Props;

public class
RoleService
{
	private int						port;
	private int						nthreads;
	private Props					props;
	private String					defaultUser  = "testAdmin1";
	private String					defaultRoles = "creator,approver,admin";
	private boolean					debug;
	private HashMap<String, String>	roleMap;

	// constructor

	RoleService()
	{
		HashMap<String, String> defaults = new HashMap<String, String>();

		defaults.put("port",       "3000");
		defaults.put("nthreads",   "5");
		defaults.put("debug",      "false");

		defaults.put(defaultUser, defaultRoles);

		props    = new Props("roleservice.properties", defaults);
		port     = props.getInt("port");
		nthreads = props.getInt("nthreads");
		debug    = props.getBool("debug");
     	roleMap  = new HashMap<String, String>();

		Properties		properties = props.getProperties();
		Enumeration		userEnum   = properties.propertyNames();
		Set<String>		defaultSet = defaults.keySet();

		while (userEnum.hasMoreElements()) {
			String	user = (String) userEnum.nextElement();

			if (skipUser(user, defaultSet)) {
				if (debug) {
					System.out.println("skipping " + user);
				}

				continue;
			}

			if (debug) {
				System.out.println("building role string for " + user);
			}

			String			roleString = properties.getProperty(user);

			roleMap.put(user, mkJson(roleString));
		}
	}

	private String
	mkJson(
		String	roleString)
	{
		if (roleString.length() == 0) {
			return("{\"count\":0,\"data\":[]}");
		}

		String[]		roles      = roleString.split(",");
		StringBuffer	json       = new StringBuffer(
									"{\"count\":" + roles.length +
									",\"data\":[");

		for (int role = 0; role < roles.length; ++role) {
			json.append("{\"title\":\"" + roles[role] + "\"}");
			if (role != (roles.length - 1)) {
				json.append(",");
			}
		}

		json.append("]}");

		return(json.toString());
	}

	private boolean
	skipUser(
		String			user,
		Set<String>		defaultSet)
	{
		Iterator<String>	skipIterator = defaultSet.iterator();

		while (skipIterator.hasNext()) {
			String	skipName = skipIterator.next();

			if (user.equals(skipName)) {
				return(!user.equals(defaultUser));
			}
		}

		return(false);
	}

	// main entry point
	
	public static void
	main(
		String[]	args)	// command line arguments
	{
		RoleService roleService = new RoleService();
		roleService.run(args);
	}

	// non static action code (called from main after creating instance)

	void
	run(
		String[]	args)	// command line arguments
	{
		try {
			// listen on sockets
			ServerSocket	socket = new ServerSocket(port);

			if (debug) {
				System.out.println(
					"listening for browser connection on port " + port);
			}

			// build thread service objects using the sockets
			RoleThread thread = new RoleThread(socket, port, roleMap, debug);

			// light off the requested number of threads to handle connections

			for (int count = nthreads; count > 0; --count) {
				new Thread(thread).start();
			}

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}
