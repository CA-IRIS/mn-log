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
import java.util.Hashtable;
import java.util.Properties;


public class TmsConnection extends DatabaseConnection {

	protected static final String CAMERA_ID = "name";
	protected static final String CAMERA_ENCODER = "encoder";
	protected static final String CAMERA_ENCODER_CHANNEL = "encoder_channel";
	protected static final String CAMERA_PUBLISH = "publish";
	protected static final String CAMERA_NVR = "nvr";
	
	public static final int TYPE_CONTROLLER = 1;
	public static final int TYPE_COMMUNICATION_LINE = 2;
	public static final int TYPE_CAMERA = 3;
	public static final int TYPE_DETECTOR = 4;
	public static final int TYPE_LCS = 5;
	public static final int TYPE_DMS = 6;
	public static final int TYPE_METER = 7;
	
	protected static final String TABLE_CAMERA = "camera_view";
	protected static final String TABLE_DMS = "dms_view";
	protected static final String TABLE_METER = "ramp_meter_view";
	protected static final String TABLE_DETECTOR = "detector_view";
	protected static final String TABLE_COMMLINK = "comm_link";
	protected static final String TABLE_CONTROLLER = "controller_loc_view";

	protected static final String F_CROSS_STREET = "cross_street";
	protected static final String F_CROSS_DIR = "cross_dir";
	protected static final String F_CROSS_MOD = "cross_mod";
	protected static final String F_FREEWAY = "freeway";
	protected static final String F_FREEWAY_DIR = "free_dir";

	protected static final String F_DMS_ID = "id";
	protected static final String F_CAMERA_ID = "name";
	protected static final String F_METER_ID = "id";
	protected static final String F_DETECTOR_ID = "det_id";
	protected static final String F_COMMLINK_ID = "name";
	protected static final String F_COMMLINK_URL = "url";
	protected static final String F_CONTROLLER_ID = "name";
	
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
			String q = "select " + CAMERA_NVR + " from " + TABLE_CAMERA +
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
			String q = "select " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
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
		String q = "select distinct " + CAMERA_ENCODER + " from " + TABLE_CAMERA +
			" where " + CAMERA_ENCODER + " is not null";
		return getColumn(query(q), CAMERA_ENCODER);
	}
	
	/**
	 * Get an array of camera ids for the given encoder ip address.
	 * @param host The hostname of the encoder.
	 * @return An array camera ids.
	 */
	public String[] getCameraIdsByEncoder(String ip){
		String q = "select " + CAMERA_ID + " from " + TABLE_CAMERA +
			" where " + CAMERA_ENCODER + " like '" + ip + ":%'";
		return getColumn(query(q), CAMERA_ID);
	}

	/**
	 * Get an array of camera ids for the given nvr ip address.
	 * @param host The IP of the nvr.
	 * @return An array camera ids.
	 */
	public String[] getCameraIdsByNvr(String ip){
		String q = "select " + CAMERA_ID + " from " + TABLE_CAMERA +
			" where " + CAMERA_NVR + " like '" + ip + ":%'";
		return getColumn(query(q), CAMERA_ID);
	}

	public int getEncoderChannel(String camId){
		String q = "select " + CAMERA_ENCODER_CHANNEL + " from " + TABLE_CAMERA +
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
		String q = "select " + CAMERA_PUBLISH + " from " + TABLE_CAMERA +
			" where " + CAMERA_ID + " = '" + camId + "'";
		try{
			ResultSet rs = query(q);
			if(rs.next()) return rs.getBoolean(CAMERA_PUBLISH);
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/** Get the location of a device */
	public String getLocation(int type, String deviceName){
		String table = null;
		String idField = null;
		switch (type) {
			case TYPE_DMS:
				table = TABLE_DMS;
				idField = F_DMS_ID;
				break;
			case TYPE_CAMERA:
				table = TABLE_CAMERA;
				idField = F_CAMERA_ID;
				break;
			case TYPE_DETECTOR:
				table = TABLE_DETECTOR;
				idField = F_DETECTOR_ID;
				break;
			case TYPE_METER:
				table = TABLE_METER;
				idField = F_METER_ID;
				break;
			case TYPE_CONTROLLER:
				table = TABLE_CONTROLLER;
				idField = F_CONTROLLER_ID;
				break;
			default:
				break;
		}
		String q = "select " + F_FREEWAY + ", " + F_FREEWAY_DIR + ", " +
			F_CROSS_STREET + ", " + F_CROSS_DIR + ", " + F_CROSS_MOD +
			" from " + table + " where " + idField + " = '" + deviceName + "'";
		String loc = "";
		try{
			ResultSet rs = query(q);
			if(rs.next()){
				loc = loc.concat(rs.getString(F_FREEWAY));
				loc = loc.concat(" " + rs.getString(F_FREEWAY_DIR));
				loc = loc.concat(" " + rs.getString(F_CROSS_MOD));
				loc = loc.concat(" " + rs.getString(F_CROSS_STREET));
				loc = loc.concat(" " + rs.getString(F_CROSS_DIR));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return loc;
	}

	/** Get a hash of vault_oid's indexed by the
	 *  device id
	 */
	public Hashtable getVaultOids(int type){
		Hashtable hash = new Hashtable();
		switch(type){
			case(TYPE_CONTROLLER):
				return getControllerIDs();
			case(TYPE_COMMUNICATION_LINE):
				return getCommLineOIDs();
			case(TYPE_CAMERA):
				return getCameraOIDs();
			case(TYPE_DETECTOR):
				return getDetectorOIDs();
			case(TYPE_DMS):
				return getDMSOIDs();
			case(TYPE_LCS):
				return getLCSOIDs();
			case(TYPE_METER):
				return getMeterOIDs();
		}
		return hash;
	}
	
	private Hashtable getControllerIDs(){
		Hashtable hash = new Hashtable();
		String sql = "select name, comm_link, drop_id " +
				"from controller_loc_view ";
		ResultSet set = query(sql);
		String id = null;
		String oid = null; 
		try{
			set.beforeFirst();
			while(set.next()){
				id = set.getString("comm_link") + "D" + set.getString("drop_id");
				oid = set.getString("name");
				hash.put(oid, id);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return hash;
	}
	
	/** Get a <code>Hashtable</code> that contains comm line
	 * ID's as the keys and vault OID's as the values. */
	private Hashtable getCommLineOIDs(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		String sql = "select vault_oid, index from communication_line";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				String id = set.getString("index");
				hash.put(id, new Integer(set.getInt("vault_oid")));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
		return hash;
	}

	/** Get a <code>Hashtable</code> that contains meter
	 * ID's as the keys and vault OID's as the values. */
	private Hashtable getMeterOIDs(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		String sql = "select vault_oid, id from ramp_meter";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				String id = set.getString("id");
				hash.put(id, new Integer(set.getInt("vault_oid")));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
		return hash;
	}

	/** Get a <code>Hashtable</code> that contains detector
	 * ID's as the keys and vault OID's as the values. */
	private Hashtable<String, Integer> getDetectorOIDs(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		String sql = "select vault_oid, index from detector";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				String id = set.getString("index");
				hash.put(id, new Integer(set.getInt("vault_oid")));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
		return hash;
	}

	/** Get a <code>Hashtable</code> that contains DMS
	 * ID's as the keys and vault OID's as the values. */
	private Hashtable getDMSOIDs(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		String sql = "select vault_oid, id from dms";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				String id = set.getString("id");
				hash.put(id, new Integer(set.getInt("vault_oid")));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
		return hash;
	}

	/** Get a <code>Hashtable</code> that contains camera
	 * ID's as the keys and vault OID's as the values. */
	private Hashtable getCameraOIDs(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		String sql = "select vault_oid, id from camera";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				String id = set.getString("id");
				hash.put(id, new Integer(set.getInt("vault_oid")));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
		return hash;
	}

	/** Get a <code>Hashtable</code> that contains LCS
	 * ID's as the keys and vault OID's as the values. */
	private Hashtable getLCSOIDs(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		String sql = "select vault_oid, id from lcs";
		ResultSet set = query(sql);
		try {
			set.beforeFirst();
			while ( set.next() ) {
				String id = set.getString("id");
				hash.put(id, new Integer(set.getInt("vault_oid")));
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
		return hash;
	}

	/* Get the comm_link name for the given URL */
	public String getCommLink(String url){
		try{
			String q = "select " + F_COMMLINK_ID + " from " + TABLE_COMMLINK +
				" where " + F_COMMLINK_URL + " = '" + url + "'";
			ResultSet rs = query(q);
			if(rs.next()) return rs.getString(F_COMMLINK_ID);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null; 
	}
}
