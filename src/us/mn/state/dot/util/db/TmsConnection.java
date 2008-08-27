/*
 * Utility classes project
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
package us.mn.state.dot.util.db;

import java.sql.ResultSet;
import java.util.Properties;


public class TmsConnection extends DatabaseConnection {

	protected static final String CAMERA_ID = "name";
	protected static final String CAMERA_ENCODER = "encoder";
	protected static final String CAMERA_ENCODER_CHANNEL = "encoder_channel";
	protected static final String CAMERA_PUBLISH = "publish";
	protected static final String CAMERA_NVR = "nvr";
	
	protected static final String CAMERA = "camera";

	public TmsConnection(Properties p){
		super(
			DatabaseConnection.TYPE_POSTGRES,
			p.getProperty("tms.db.user"),
			p.getProperty("tms.db.pwd"),
			p.getProperty("tms.db.host"),
			Integer.parseInt(p.getProperty("tms.db.port")), "tms");
	}

	protected String createId(int camNumber){
		String id = Integer.toString(camNumber);
		while(id.length()<4) id = "0" + id;
		return "C" + id;
	}
	
	public String getNvrHost(String camId){
		try{
			String q = "select " + CAMERA_NVR + " from " + CAMERA +
				" where " + CAMERA_ID + " = '" + camId + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(CAMERA_NVR);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}

	public String getEncoderHost(String camId){
		try{
			String q = "select " + CAMERA_ENCODER + " from " + CAMERA +
				" where " + CAMERA_ID + " = '" + camId + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(CAMERA_ENCODER);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}

	/**
	 * Get an array of encoder hostnames for all cameras.
	 */
	public String[] getEncoderHosts(){
		String q = "select distinct " + CAMERA_ENCODER + " from " + CAMERA +
			" where " + CAMERA_ENCODER + " is not null";
		return getColumn(query(q), CAMERA_ENCODER);
	}
	
	/**
	 * Get an array of camera ids for the given encoder ip address.
	 * @param host The hostname of the encoder.
	 * @return An array camera ids.
	 */
	public String[] getCameraIdsByEncoder(String ip){
		String q = "select " + CAMERA_ID + " from " + CAMERA +
			" where " + CAMERA_ENCODER + " like '" + ip + ":%'";
		return getColumn(query(q), CAMERA_ID);
	}

	/**
	 * Get an array of camera ids for the given nvr ip address.
	 * @param host The IP of the nvr.
	 * @return An array camera ids.
	 */
	public String[] getCameraIdsByNvr(String ip){
		String q = "select " + CAMERA_ID + " from " + CAMERA +
			" where " + CAMERA_NVR + " like '" + ip + ":%'";
		return getColumn(query(q), CAMERA_ID);
	}

	public int getEncoderChannel(String camId){
		String q = "select " + CAMERA_ENCODER_CHANNEL + " from " + CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs.next()) return rs.getInt(CAMERA_ENCODER_CHANNEL);
		}catch(Exception e){
			e.printStackTrace();
		}
		return -1;
	}

	/** Get the publish attribute of the camera */
	public boolean isPublished(String camId){
		String q = "select " + CAMERA_PUBLISH + " from " + CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs.next()) return rs.getBoolean(CAMERA_PUBLISH);
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
}
