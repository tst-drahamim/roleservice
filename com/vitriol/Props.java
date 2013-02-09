package com.vitriol;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Properties;

public class
Props
{
	private Properties				properties;
	private String					propfile;
	private Map<String, String>		defaults;

	// constructor

	public
	Props(
		String				filename,
		Map<String, String>	map)
	{
		properties = new Properties();
		propfile   = filename;
		defaults   = map;

		try {
			properties.load(new FileInputStream(propfile));
		} catch (IOException ex) {
			System.err.println("can't read properties file \"" +
				propfile + "\", creating it");
			mkprops("", null);
		}
	}

	// get a string value from a Property

	public String
	getString(
		String	name)
	{
		String	stringValue;	// string value of Property

		for (;;) {
			// get value for property
			stringValue = properties.getProperty(name);

			// if this property isn't set, update properties file and try again

			if (stringValue != null) {
				break;
			}

			mkprops("updated ", name);
		}

		return(stringValue);
	}

	// get an integer value from a Property

	public int
	getInt(
		String	name)			// name of property to retrieve
	{
		int		value;			// integer value of Property

		for (;;) {
			try {
				value = Integer.parseInt(getString(name));
			} catch (NumberFormatException ex) {
				mkprops("updated ", name);
				continue;
			}

			break;
		}

		// got a valid integer valued property, return it

		return(value);
	}

	// return a boolean value from a Property

	public boolean
	getBool(
		String	name)			// name of property to retrieve
	{
		return(Boolean.parseBoolean(getString(name)));
	}

	// return our internal Properties file

	public Properties
	getProperties()
	{
		return(properties);
	}

	// construct/update a properties file

	private void
	mkprops(
		String	modifier,	// modify informative message (for update)
		String	replace)	// replace this property with default value
	{
		// set properties to default values
		Set<String>			keys        = defaults.keySet();
		Iterator<String>	keyIterator = keys.iterator();

		boolean updated = false;

		while (keyIterator.hasNext()) {
			String	key   = keyIterator.next();
			String	value = defaults.get(key);

			if (setprop(key, value, replace)) {
				updated = true;
			}
		}

		if ((replace != null) && !updated) {
			System.err.println("no default value for " + replace +
				", setting it to itself to avoid infinite loop!");
			setprop(replace, replace, replace);
		}

		// write properties file

		try {
			properties.store(new FileOutputStream(propfile), null);
		} catch (IOException ex) {
			System.err.println("can't write " + modifier +
				"properties file \"" + propfile + "\"");
		}
	}

	// set a property to a value if not set

	private boolean
	setprop(
		String	name,		// property to set
		String	value,		// value to set
		String	replace)	// if this string matches property name, force set
	{
		if (!name.equals(replace)) {
			if (properties.getProperty(name) != null) {
				// property already set and not force, nothing to do
				return(false);
			}
		}

		// set property

		properties.setProperty(name, value);

		return(true);
	}
}
