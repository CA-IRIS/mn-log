/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007,2010  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Proxy selector for HTTP clients
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class HTTPProxySelector extends ProxySelector {

	/** Ports to be proxied */
	static protected final int[] PROXY_PORTS = {80, 8080};

	/** Check if the port of a URI should be proxied */
	static protected boolean isProxyPort(URI uri) {
		int p = uri.getPort();
		if(p == -1)
			return true;
		for(int i: PROXY_PORTS) {
			if(p == i)
				return true;
		}
		return false;
	}

	protected String[] noProxyHosts;

	protected final List<Proxy> DIRECT_LIST = new ArrayList<Proxy>();

	protected final List<Proxy> PROXY_LIST = new ArrayList<Proxy>();

	/** Create a new HTTP proxy selector */
	public HTTPProxySelector(Properties props) {
		DIRECT_LIST.add(Proxy.NO_PROXY);
		setProxyList(props);
		setNoProxyHosts(props);
	}

	private void setNoProxyHosts(Properties props) {
		String hosts = props.getProperty("no.proxy.hosts");
		if(hosts != null) {
			StringTokenizer t =
				new StringTokenizer(hosts, ",", false);
			noProxyHosts = new String[t.countTokens()];
			for(int i = 0; i < noProxyHosts.length; i++) {
				String ip = t.nextToken();
				noProxyHosts[i] = ip;
			}
		}
	}

	private void setProxyList(Properties props) {
		String p = props.getProperty("proxy.port");
		String h = props.getProperty("proxy.host");
		if(h != null && p != null) {
			SocketAddress sa = new InetSocketAddress(h,
				Integer.valueOf(p));
			PROXY_LIST.add(new Proxy(Proxy.Type.HTTP, sa));
		}

	}

	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		// FIXME: implement this method
	}

	protected boolean isInside(URI uri) {
		String host = uri.getHost();
		try {
			InetAddress addr = InetAddress.getByName(host);
			String hostIp = addr.getHostAddress();
			for(String h: noProxyHosts) {
				if(hostIp.indexOf(h) > -1)
					return true;
			}
		}
		catch(UnknownHostException uhe) {
		}
		return false;
	}

	public List<Proxy> select(URI uri) {
		if(uri == null)
			return DIRECT_LIST;
		if(PROXY_LIST.size() == 0)
			return DIRECT_LIST;
		if(isInside(uri))
			return DIRECT_LIST;
		if(!isProxyPort(uri))
			return DIRECT_LIST;
		return PROXY_LIST;
	}
}
