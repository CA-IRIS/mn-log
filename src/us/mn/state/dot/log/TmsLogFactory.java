/*
 * Project: TMS Log
 * Copyright (C) 2007  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TmsLogFactory {

	static protected void removeParentHandlers(Logger l) {
		Logger parent = l.getParent();
		while(parent != null) {
			Handler[] pHandlers = parent.getHandlers();
			for(int i = 0; i < pHandlers.length; i++)
				parent.removeHandler(pHandlers[i]);
			parent = parent.getParent();
		}
	}

	static protected void printError(String e) {
		System.out.println(e + "... using standard out.");
	}

	static protected void setDirectory(Logger l, File dir, String name)
	{
		removeParentHandlers(l);
		Handler h = null;
		if(dir==null || !dir.isDirectory() || !dir.canWrite()){
			h = new ConsoleHandler();
		}else {
			try{
				String d = dir.getAbsolutePath();
				String n = d + File.separator + name;
				h = new FileHandler(n + "_%g.log", 1024 * 1024 * 5, 4);
			}catch(IOException ioe){
				printError(ioe.getMessage());
			}
		}
		h.setFormatter(new TmsLogFormatter());
		if(h!=null) l.addHandler(h);
	}

	/**
	 * Create a standard logger for Mn/Dot applications
	 * @return
	 */
	public static Logger createLogger(String name) {
		return createLogger(name, null, null);
	}

	/**
	 * Create a standard logger for Mn/Dot applications
	 * @param name The base name for the log files
	 * @param level The level of logging
	 * @param dir The location for the log files
	 * @return
	 */
	public static Logger createLogger(String name, Level level, File dir) {
		Logger l = Logger.getLogger(name);
		try{
			l.setLevel( level!=null ? level: Level.FINE);
		}catch(Exception e){
		}
		setDirectory(l, dir, name);
		return l;
	}
	
	/** Redirect the standard output and error streams to log files. */
	public static void redirectStdStreams(String appName, File dir)
			throws FileNotFoundException {
		String fileName = dir.getAbsolutePath() +
			File.separator + appName;
		FileOutputStream fos =
			new FileOutputStream(fileName + ".out", true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		System.setOut(new PrintStream(bos, true));
		fos = new FileOutputStream(fileName + ".err", true);
		bos = new BufferedOutputStream(fos);
		System.setErr(new PrintStream(bos, true));
	}

	public static void main(String[] args){
		Logger l = TmsLogFactory.createLogger("mylogger");
		l.warning("this is a warning.");
	}
}
