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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Proxy selector for HTTP clients
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class HTTPProxySelector extends ProxySelector {

	/** List of proxies */
	protected final List<Proxy> proxies;

	/** Array of hosts to skip proxy */
	protected final String[] no_proxy_hosts;

	/** Create a new HTTP proxy selector */
	public HTTPProxySelector(Properties props) {
		proxies = createProxyList(props);
		no_proxy_hosts = createNoProxyHosts(props);
	}

	/** Create a Proxy list from a set of properties */
	protected List<Proxy> createProxyList(Properties props) {
		LinkedList<Proxy> plist = new LinkedList<Proxy>();
		String h = props.getProperty("proxy.host");
		String p = props.getProperty("proxy.port");
		if(h != null && p != null) {
			SocketAddress sa = new InetSocketAddress(h,
				Integer.valueOf(p));
			plist.add(new Proxy(Proxy.Type.HTTP, sa));
		}
		return plist;
	}

	/** Create an array of hosts to skip proxy */
	protected String[] createNoProxyHosts(Properties props) {
		String hosts = props.getProperty("no.proxy.hosts");
		if(hosts != null)
			return hosts.split(",");
		else
			return new String[0];
	}

	/** Handle a failed connection to a proxy server */
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		// FIXME: implement this method
	}

	/** Select available proxy servers based on a URI */
	public List<Proxy> select(URI uri) {
		LinkedList<Proxy> pl = new LinkedList<Proxy>();
		if(uri != null && shouldUseProxy(uri)) {
			int port = uri.getPort();
			for(Proxy proxy: proxies) {
				SocketAddress sa = proxy.address();
				if(sa instanceof InetSocketAddress) {
					InetSocketAddress isa =
						(InetSocketAddress)sa;
					if(isa.getPort() == port)
						pl.add(proxy);
				}
			}
		}
		if(pl.isEmpty())
			pl.add(Proxy.NO_PROXY);
		return pl;
	}

	/** Check if a proxy server should be used for a URI */
	protected boolean shouldUseProxy(URI uri) {
		String host = uri.getHost();
		try {
			InetAddress addr = InetAddress.getByName(host);
			String hip = addr.getHostAddress();
			for(String h: no_proxy_hosts) {
				if(hip.startsWith(h))
					return false;
			}
			return true;
		}
		catch(UnknownHostException uhe) {
			return true;
		}
	}

	/** Check if the selector has defined proxy servers */
	public boolean hasProxies() {
		return proxies.size() > 0;
	}
}
