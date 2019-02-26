package com.kuvata.kmf;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import com.kuvata.kmf.usertype.DeviceCommandType;
import com.kuvata.kmf.usertype.StatusType;
import com.kuvata.kmf.usertype.SubStatusType;

import com.kuvata.kmf.util.Reformat;

public class DeviceCommand extends PersistentEntity 
{
	private static Logger logger = Logger.getLogger(DeviceCommand.class);
	private static final String DEVICE_ADMIN_SERVLET_PATH = "DeviceAdmin/DeviceAdminServlet";
	private Long deviceCommandId;
	private Device device;	
	private String command;
	private String parameters;
	private Date createDt;
	private Date lastModifiedDt;
	private StatusType status;
	private SubStatusType subStatus;
	
	/**
	 * 
	 *
	 */
	public DeviceCommand()
	{		
	}
	
	/**
	 * 
	 * @param deviceCommandId
	 * @return
	 * @throws HibernateException
	 */
	public static DeviceCommand getDeviceCommand(Long deviceCommandId) throws HibernateException
	{
		return (DeviceCommand)PersistentEntity.load(DeviceCommand.class, deviceCommandId);		
	}

	/**
	 * Returns a list of device commands for the given parameters
	 * that has a status of either "In Progress", "Queued", "Pending", "Success" or null.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<DeviceCommand> getDeviceCommands(Device device, String command, String params, boolean includeFailedStatus) throws HibernateException
	{
		String hql = "SELECT dc "
			+ "FROM DeviceCommand as dc "	
			+ "WHERE dc.device.deviceId = "+ device.getDeviceId() +" "
			+ "AND dc.command = '"+ command +"' "
			+ "AND dc.parameters = '"+ params +"' "
			+ "AND (dc.status IS NULL "
			+ "OR dc.status = '"+ StatusType.IN_PROGRESS.getStatusTypeName() +"' "
			+ "OR dc.status = '"+ StatusType.QUEUED.getStatusTypeName() +"' "				
			+ "OR dc.status = '"+ StatusType.SUCCESS.getStatusTypeName() +"' "	
			+ "OR dc.status = '"+ StatusType.PENDING.getStatusTypeName() +"' ";
		if( includeFailedStatus ){
			hql += "OR dc.status = '"+ StatusType.FAILED.getStatusTypeName() +"' ";
		}
		hql += ") ORDER BY createDt desc";
		Session session = HibernateSession.currentSession();	
		return session.createQuery( hql ).list();			
	}	
	
	public static List<DeviceCommand> getDeviceCommandsForManagingPresentations(Device device, String command, boolean includeFailedStatus) throws HibernateException
	{
		String hql = "SELECT dc "
			+ "FROM DeviceCommand as dc "	
			+ "WHERE dc.device.deviceId = "+ device.getDeviceId() +" "
			+ "AND dc.command = '"+ command +"' "
			+ "AND (dc.status IS NULL "
			+ "OR dc.status = '"+ StatusType.IN_PROGRESS.getStatusTypeName() +"' "
			+ "OR dc.status = '"+ StatusType.QUEUED.getStatusTypeName() +"' "				
			+ "OR dc.status = '"+ StatusType.SUCCESS.getStatusTypeName() +"' "	
			+ "OR dc.status = '"+ StatusType.PENDING.getStatusTypeName() +"' ";
		if( includeFailedStatus ){
			hql += "OR dc.status = '"+ StatusType.FAILED.getStatusTypeName() +"' ";
		}
		hql += ") ORDER BY createDt desc";
		Session session = HibernateSession.currentSession();	
		return session.createQuery( hql ).list();			
	}	
	
	/**
	 * Returns a list of device commands for the given device/command/status
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static List<DeviceCommand> getDeviceCommands(Device device, DeviceCommandType command, StatusType status, String params) throws HibernateException
	{
		String hql = "SELECT dc "
			+ "FROM DeviceCommand as dc "	
			+ "WHERE dc.device.deviceId = "+ device.getDeviceId() +" "
			+ "AND dc.command = '"+ command.getPersistentValue() +"' "
			+ "AND dc.status = '"+ status.getStatusTypeName() +"' ";
		if( params != null ){
			hql += "AND dc.parameters = '"+ params +"' ";
		}
		hql += "ORDER BY createDt desc";
		Session session = HibernateSession.currentSession();	
		return session.createQuery( hql ).list();			
	}
	
	public static List<DeviceCommand> getDeviceCommands(Device device, DeviceCommandType command, StatusType[] statuses, String params) throws HibernateException
	{
		String hql = "SELECT dc "
			+ "FROM DeviceCommand as dc "	
			+ "WHERE dc.device.deviceId = "+ device.getDeviceId() +" "
			+ "AND dc.command = '"+ command.getPersistentValue() +"' "
			+ "AND dc.status IN (:statuses) ";
		if( params != null ){
			hql += "AND dc.parameters = '"+ params +"' ";
		}
		hql += "ORDER BY createDt desc";
		Session session = HibernateSession.currentSession();
		return session.createQuery( hql ).setParameterList("statuses", statuses).list();
	}
	
	/**
	 * 
	 * @param edgeServer
	 * @param command
	 * @param params
	 * @return
	 * @throws HibernateException
	 */
	public static void activateNodeDeviceCommandsOnHold(Device edgeServer, String command, String params) throws HibernateException
	{
		String hql = "UPDATE DeviceCommand dc "
			+ "SET dc.status = '" + StatusType.QUEUED.getStatusTypeName()+"' "
			+ "WHERE dc.device.deviceId IN (:nodeDeviceIds) "
			+ "AND dc.command = '"+ command +"' "
			+ "AND dc.parameters = '"+ params +"' "
			+ "AND dc.status = '"+ StatusType.HOLD.getStatusTypeName() +"'";
		Session session = HibernateSession.currentSession();	
		session.createQuery( hql ).setParameterList("nodeDeviceIds", edgeServer.getEdgeDeviceIds()).executeUpdate();			
	}
	
	/**
	 * Returns true or false depending if a device command exists for the given parameters
	 * that has a status of either "In Progress", "Queued", "Pending", "Success" or null.
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static boolean deviceCommandExists(Device device, String command, String params) throws HibernateException
	{		
		List<DeviceCommand> l = getDeviceCommands( device, command, params, false );
		return l.size() > 0 ? true : false;		
	}	
	
	/**
	 * Creates a new DeviceCommand object
	 * @param device
	 * @param cmd
	 * @param params
	 */
	public static DeviceCommand create(Device device, DeviceCommandType cmd, String params, boolean attemptPush) throws InterruptedException
	{
		DeviceCommand deviceCommand = new DeviceCommand();					
		deviceCommand.setCreateDt( new Date() );
		deviceCommand.setDevice( device );
		deviceCommand.setCommand( cmd.getPersistentValue() );
		deviceCommand.setParameters( params );
		deviceCommand.setStatus( StatusType.QUEUED );
		deviceCommand.save();
		
		/*
		 * First, try to push the command down to the device.
		 * We do not want to push property_change commands because we want to aggregate
		 * the property_change commands once on the client, allowing all processes to be restarted in the correct order.
		 */							
		boolean pushSuccess = false;		
		if( attemptPush && cmd.getPersistentValue().equalsIgnoreCase( DeviceCommandType.PROPERTY_CHANGE_COMMAND.getPersistentValue() ) == false )
		{
			/*
			 * If there are any device commands for this device with a status of QUEUED or IN PROGRESS, 
			 * we do not want to push the device command because we could end up executing 
			 * device commands out of order on the device. This would lead to problems like: 
			 * device executes "applySettings" mcm command prior to updating its mcm properties. 
			 */
			if( cmd.equals(DeviceCommandType.UPLOAD_SCREENSHOT) || device.getNumUnexecutedDeviceCommands( deviceCommand.getDeviceCommandId() ) == 0 )
			{
				// If this is an Android device
				if(device.getOsVersion() != null && device.getOsVersion().startsWith(Constants.ANDROID)){
					device.heartbeatNow();
				}else if(device.getVpnIpAddr() != null){
					// Push the device command to port 8080/8181 on the device
					String devicePort = device.getVersion() != null && Device.isNewerVersion(device.getVersion(), "3.6b13") ? ":8181" : ":8080";
					String pushAddress = device.getVpnIpAddr() + devicePort;
					pushSuccess = pushDeviceCommand( pushAddress, device.getDeviceId(), deviceCommand.getDeviceCommandId(), cmd.getPersistentValue(), params );
				}
			}								
		}
		
		// If we pushed the command successfully to the device, update it's status
		if( pushSuccess ) {					
			deviceCommand.setStatus( StatusType.IN_PROGRESS );
			deviceCommand.update();						
		}	
		
		return deviceCommand;
	}
	
	/**
	 * Attempt to connect to the DispatcherServlet running on the device.
	 *
	 */
	private static boolean pushDeviceCommand(String pushAddress, Long deviceId, Long deviceCommandId, String cmd, String params) throws InterruptedException
	{
		boolean success = false;
		String characterEncoding = "UTF-8";
		
		try {
			// Build the url out of the pushAddress
			String sUrl = "http://"+ pushAddress +"/"+ DEVICE_ADMIN_SERVLET_PATH;
			logger.info("Pushing to device: "+ sUrl);
			
			URL url = new URL( sUrl );
			URLConnection urlConn = url.openConnection();
			urlConn.setDoInput (true);
			urlConn.setDoOutput (true);
			urlConn.setUseCaches (false);
			
			// Wait 5 seconds before giving up on the push connect and 1 minute for reading data
			urlConn.setConnectTimeout( Constants.CONNECTION_TIMEOUT_SECONDS * 1000 );
			urlConn.setReadTimeout( Constants.HTTP_CONNECTION_TIMEOUT_SECONDS * 1000 );
			urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			// Send POST output.
			DataOutputStream printout = new DataOutputStream (urlConn.getOutputStream ());
			String content ="deviceId="+ deviceId.toString() +"&deviceCommandId="+ deviceCommandId.toString() +"&cmd="+ URLEncoder.encode(cmd, characterEncoding) +"&params="+ URLEncoder.encode(params, characterEncoding);
			logger.info("pushing device command: "+ content );
			printout.writeBytes (content);
			printout.flush ();
			printout.close ();
			
	        BufferedReader in = new BufferedReader( new InputStreamReader( urlConn.getInputStream()));
			String inputLine;
			StringBuffer result = new StringBuffer("");
			while ((inputLine = in.readLine()) != null) 
				result.append( inputLine );		
			in.close ();
			
			logger.info("Response from servlet: "+ result.toString() );
			
			// If received a "success" response from the servlet -- it must have gone through ok
			if( result.toString().equalsIgnoreCase(Constants.SUCCESS) )
			{
				success = true;
			}
		} catch (SocketTimeoutException toe) {
			// do not log timeout exceptions
		} catch (IOException ioe) {
			// do not log io exceptions
		} catch (Exception e) {
			if(e instanceof InterruptedException){
				throw (InterruptedException)e;
			}else{
				logger.error( e );
			}
		}	
		return success;
	}	
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getDeviceCommandId();
	}
	
	public static String addPriority(Device d, Integer priority, String parameters){
		// Makes sure that a device is on 3.5.2b5 or greater and then adds priority to the parameters
		if( priority != null && d.getVersion() != null && Device.isNewerVersion(d.getVersion(), "3.5.2b4") ){
			parameters = parameters + "&priority=" + priority;
		}
		return parameters;
	}
	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof DeviceCommand) ) result = false;
		
		DeviceCommand de = (DeviceCommand) other;		
		if(this.hashCode() == de.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "DeviceCommand".hashCode();		
		result = Reformat.getSafeHash( this.getDevice().getDeviceId(), result, 13 );
		result = Reformat.getSafeHash( this.getCommand(), result, 29 );
		result = Reformat.getSafeHash( this.getParameters(), result, 31 );		
		return result;
	}
	
	public int eTag(){
		int result = "DeviceCommand".hashCode();		
		result = Reformat.getSafeHash( this.getDevice().getDeviceId(), result, 5 );
		result = Reformat.getSafeHash( this.getCommand(), result, 7 );
		result = Reformat.getSafeHash( this.getParameters(), result, 11 );
		result = Reformat.getSafeHash( this.getStatus().getPersistentValue(), result, 13 );
		if(this.getSubStatus() != null){
			result = Reformat.getSafeHash( this.getSubStatus().getPersistentValue(), result, 17 );
		}
		return result;
	}
		
	/**
	 * @return Returns the command.
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command The command to set.
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return Returns the device.
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device The device to set.
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return Returns the deviceCommandId.
	 */
	public Long getDeviceCommandId() {
		return deviceCommandId;
	}

	/**
	 * @param deviceCommandId The deviceCommandId to set.
	 */
	public void setDeviceCommandId(Long deviceCommandId) {
		this.deviceCommandId = deviceCommandId;
	}

	/**
	 * @return Returns the parameters.
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * @param parameters The parameters to set.
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return Returns the status.
	 */
	public StatusType getStatus() {
		return status;
	}
	

	/**
	 * @param status The status to set.
	 */
	public void setStatus(StatusType status) {
		this.status = status;
	}


	/**
	 * @return the createDt
	 */
	public Date getCreateDt() {
		return createDt;
	}

	/**
	 * @param createDt the createDt to set
	 */
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}

	/**
	 * @return the lastModifiedDt
	 */
	public Date getLastModifiedDt() {
		return lastModifiedDt;
	}

	/**
	 * @param lastModifiedDt the lastModifiedDt to set
	 */
	public void setLastModifiedDt(Date lastModifiedDt) {
		this.lastModifiedDt = lastModifiedDt;
	}

	/**
	 * @return the subStatus
	 */
	public SubStatusType getSubStatus() {
		return subStatus;
	}

	/**
	 * @param subStatus the subStatus to set
	 */
	public void setSubStatus(SubStatusType subStatus) {
		this.subStatus = subStatus;
	}
	
	

}
