package com.kuvata.kmf.asset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;

public class MediaInfo {

	private static Logger logger = Logger.getLogger(MediaInfo.class);
	private static final String VIDEO_INFO_COMMAND = "Application.videoInfoCommand";
	private static final String AUDIO_INFO_COMMAND = "Application.audioInfoCommand";
	private static final String AUDIO_PROPERTY_LENGTH = "ID_LENGTH";
	private static final String EXIFTOOL_COMMAND = "/usr/bin/exiftool";
	
	private static final int PROCESS_TIMEOUT = 120;
	
	private static DecimalFormat df = new DecimalFormat();
	
	private Float length;
	private Integer height;
	private Integer width;
	private String videoCodec;
	private Integer normalizationBeginAvg;
	
	static{
		// Limit the max decimal places to 2 and disable grouping
		df.setMaximumFractionDigits( 2 );
		df.setGroupingUsed(false);
	}
	
	public static synchronized MediaInfo getMediaInfo(Asset asset)
	{			
		MediaInfo result = null;
		File f = null;
		
		// Currently only retrieving intrinsic properties for Audio and Video
		if(asset.getFileloc() != null){
			if( asset instanceof Video ){
				f = new File( ((Video)asset).getFileloc() );
			}else if( asset instanceof Audio ){
				f = new File( ((Audio)asset).getFileloc() );
			}
		}
		
		if( f != null && f.exists() )
		{
			if(asset instanceof Video){
				/*
				 * Mencoder output
				 *	videocodec: framecopy (720x480 12bpp fourcc=30355844)
				 *  Video stream: 4744.016 kbit/s  (593001 B/s)  size: 53344400 bytes  89.957 secs  2696 frames
				 *	Audio stream: 1536.000 kbit/s  (192000 B/s)  size: 17264640 bytes  89.920 secs
				 */
				try {
					MediaInfo mit = new MediaInfo();
					MediaInfoThread t = mit.new MediaInfoThread(){
						public void run(){
							try{
								//String buf;
								String videoCodec = null;
								Integer normalizationBeginAvg = null;
								Integer height = new Integer(Constants.INTRINSIC_LENGTH_PLACEHOLDER );
								Integer width = new Integer(Constants.INTRINSIC_LENGTH_PLACEHOLDER );
								Float exiftoolLength = new Float( Constants.INTRINSIC_LENGTH_PLACEHOLDER );
								float mplayerVideoLength = -1;
								boolean isMovFile = this.getFile().getName().toLowerCase().endsWith(".mov");
								
								// If this is an mov file
								if(isMovFile){
									// Try to read data from metadata embedded on the file
									Object[] data = getDataFromMetadata(this.getFile().getAbsolutePath(), true);
									String duration = (String)data[0];
									exiftoolLength = Float.parseFloat(duration);
									width = (Integer)data[1];
									height = (Integer)data[2];
								}
								
								try {
									// Create tmpFile path
									String filePath = "/kuvata/tmp/" + this.getMetadataFileName();
									File tmpFile = new File(filePath);
									tmpFile.delete();
									
									Process p = Runtime.getRuntime().exec(this.getCmd());
									
									// Give it 5 seconds to execute
									Thread.sleep(5000);
									
									// Kill the process if its still running
									try {
										p.exitValue();
									} catch (Exception e) {
										p.destroy();
										p.waitFor();
									}
									
									p.getErrorStream().close();
									p.getOutputStream().close();
									p.getInputStream().close();
									p = null;
									
									if(tmpFile.exists()){
										FileReader fr = new FileReader(tmpFile);
										BufferedReader in = new BufferedReader(fr);
										String buffer;
										while((buffer = in.readLine()) != null){
											if (buffer.contains("ID_VIDEO_WIDTH")) {
												width = Integer.parseInt(buffer.split("=")[1].trim());
											}
											else if (buffer.contains("ID_VIDEO_HEIGHT")) {
												height = Integer.parseInt(buffer.split("=")[1].trim());
											}
											else if (buffer.contains("ID_LENGTH")) {
												mplayerVideoLength = Float.parseFloat(buffer.split("=")[1].trim());
											}
											else if(buffer.contains("beginAvg")){
												normalizationBeginAvg = (int)Float.parseFloat(buffer.split("=")[1].trim());
											}
										}
										in.close();
										fr.close();
										tmpFile.delete();
									}
								} catch (Exception e) {
									logger.error(e);
								}
								
								// Pick the maximum value out of the three
								float mplayerLength = mplayerVideoLength;
								float finalLength;
								
								// If the difference between these values is greater than 1 sec, use the greater of the two
								if(exiftoolLength != -1 && Math.max(exiftoolLength, mplayerLength) - Math.min(exiftoolLength, mplayerLength) > 1){
									finalLength = Math.max(exiftoolLength, mplayerLength);
									
									// If we are using exiftool length and exiftool did not return the duration with milliseconds
									if(finalLength == exiftoolLength && (float)exiftoolLength.intValue() == exiftoolLength){
										// Add 1 sec to exiftool length, as exiftool rounds off the length to the ground integer
										finalLength = exiftoolLength + 1;
									}
								}else{
									finalLength = mplayerLength;
								}
								
								try {
									// Get codec info from ffmpeg
									Process p = Runtime.getRuntime().exec("/usr/bin/ffmpeg -i " + this.getFile().getAbsolutePath());
									
									String buffer;
									BufferedReader se = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );
									while((buffer = se.readLine()) != null){
										if(buffer.matches(".*Stream.*Video:.*")){
											videoCodec = buffer.substring(buffer.indexOf("Video:") + 7);
											videoCodec = videoCodec.substring(0, videoCodec.indexOf(" "));
											videoCodec = videoCodec.replaceAll(",", "");
										}
									
									}
									se.close();
									p.getErrorStream().close();
									p.getOutputStream().close();
									p.getInputStream().close();
									p = null;
									
								} catch (Exception e) {
									logger.error(e);
								}
								
								MediaInfo mediaInfo = new MediaInfo();
								mediaInfo.setLength(Float.parseFloat(df.format(finalLength)));
								mediaInfo.setHeight( height );
								mediaInfo.setWidth( width );
								mediaInfo.setVideoCodec(videoCodec);
								mediaInfo.setNormalizationBeginAvg(normalizationBeginAvg);
								
								this.setMediaInfo(mediaInfo);
							}catch (Exception e){
								logger.error(e);
							}
						}
					};
					
					// Attempt to locate the mediaInfoCommand
					String mediaInfoCommand = "";
					String metadataFileName = String.valueOf(Math.random());
					mediaInfoCommand += " "+ f.getAbsolutePath() + " 2>/dev/null > " + "/tmp/" + metadataFileName;
					String cmd[] = { "sh", "-c", mediaInfoCommand };
					t.setCmd(cmd);
					t.setFile(f);
					t.setMetadataFileName(metadataFileName);
					t.start();
					
					// Continually check the flag from the rsync thread, or until we've reached our timeout threshold
					for( int i=0; i < PROCESS_TIMEOUT; i++ )
					{
						if( t.getMediaInfo() != null ){
							break;
						}
						Thread.sleep( 1000 );
					}
					
					// Destroy the process if necessary
					if( t != null ){
						result = t.getMediaInfo();
						if( t.getProc() != null ){
							t.getProc().destroy();
							t.setProc( null );
						}
						t = null;
					}
					
				} catch (Exception e) {
					logger.error("Unable to locate property "+ VIDEO_INFO_COMMAND +". Unable to retrieve MediaInfo.");
				}
			}else if(asset instanceof Audio){
				// Defaults
				Float audioLength = new Float( Constants.INTRINSIC_LENGTH_PLACEHOLDER );
				
				try {
					String exiftoolLength = "-1";
					
					// If this is an mp3 file, try to extract duration from tags
					if(f.getName().endsWith(".mp3")){
						exiftoolLength = (String)getDataFromMetadata(f.getAbsolutePath(), false)[0];
					}
					
					boolean compareMplayerResults = false;
					
					// If the duration doesn't have milliseconds
					if(exiftoolLength.contains(".") == false){
						compareMplayerResults = true;
					}
					
					// If we were unable to find duration in metadata, use mplayer to detect file length
					if(compareMplayerResults || exiftoolLength.equals("-1")){
						// Attempt to locate the mediaInfoCommand
						String mediaInfoCommand = "";
						mediaInfoCommand += " "+ f.getAbsolutePath();
						Process proc = Runtime.getRuntime().exec( mediaInfoCommand );
						String buf;
						BufferedReader se = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );				
						while( (buf = se.readLine()) != null ){				
							if( buf.indexOf( AUDIO_PROPERTY_LENGTH ) >= 0 ){
								audioLength = new Float( buf.substring(buf.indexOf( AUDIO_PROPERTY_LENGTH ) + AUDIO_PROPERTY_LENGTH.length() + 1 ) );
							}						
						}
						se.close();
						se = null;
					}
					
					if(compareMplayerResults){
						// If the difference between these values is greater than 1 sec, use the exiftool length.
						if(exiftoolLength.equals("-1") == false && Math.max(Float.parseFloat(exiftoolLength), audioLength) - Math.min(Float.parseFloat(exiftoolLength), audioLength) > 1){
							// If the difference between these values is greater than 1 sec, use the exiftool length.
							audioLength = Float.parseFloat(exiftoolLength) + 1;
						}
					}else{
						// We dont add a sec to exiftool length as in this case, exiftool length already has milliseconds attached.
						audioLength = Float.parseFloat(exiftoolLength);
					}
					
				} catch (IOException e) {
					logger.error( e );
				}
				
				Integer normalizationBeginAvg = null;
				
				try {
					
					// Create tmpFile path
					String filePath = "/kuvata/tmp/" + Math.random();
					File tmpFile = new File(filePath);
					tmpFile.delete();
					
					// Get the audio normalization begin average
					String[] cmd = {"/bin/sh", "-c", "mplayer -quiet -ao null -vo null -af volnorm=2:0.25:-1 " + f.getAbsolutePath() + " >" + filePath + " 2>/dev/null"};
					Process p = Runtime.getRuntime().exec(cmd);
					
					// Give it 5 seconds to execute
					Thread.sleep(5000);
					
					// Kill the process if its still running
					try {
						p.exitValue();
					} catch (Exception e) {
						p.destroy();
						p.waitFor();
					}
					
					p.getErrorStream().close();
					p.getOutputStream().close();
					p.getInputStream().close();
					p = null;
					
					if(tmpFile.exists()){
						FileReader fr = new FileReader(tmpFile);
						BufferedReader in = new BufferedReader(fr);
						String buffer;
						while((buffer = in.readLine()) != null){
							if(buffer.contains("beginAvg")){
								normalizationBeginAvg = (int)Float.parseFloat(buffer.split("=")[1].trim());
							}
						
						}
						in.close();
						fr.close();
						tmpFile.delete();
					}
				} catch (Exception e) {
					logger.error(e);
				}
				
				result = new MediaInfo();
				result.setLength(Float.parseFloat(df.format(audioLength)));
				result.setNormalizationBeginAvg(normalizationBeginAvg);
			}
		}else{
			logger.info("Unable to retrieve MediaInfo. File does not exist: "+ f != null ? f.getAbsolutePath() : "" );
		}
		
		return result;
	}
	
	private static Object[] getDataFromMetadata(String filePath, boolean getSize){
		Object result[];
		String duration = "-1";
		int width = -1;
		int height = -1;

		try {
			Process proc = Runtime.getRuntime().exec(EXIFTOOL_COMMAND + " " + filePath);
			String buf;
			BufferedReader se = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
			while( (buf = se.readLine()) != null ){		
				// Parse out the duration
				if( buf.startsWith( "Duration" ) || buf.startsWith("Send Duration")){
					buf = buf.substring(buf.indexOf(":") + 2).trim();
					// If the duration doesn't have milliseconds
					if(buf.indexOf(":") > 0){
						// Remove an '(approx)' if exists
						buf = buf.split(" ")[0];
						String length[] = buf.split(":");
						duration = Integer.toString((Integer.parseInt(length[0]) * 60) + Integer.parseInt(length[1]));
					}
					// If the duration has time in milliseconds
					else if(buf.indexOf(".") > 0){
						// Remove the 's' for seconds
						buf = buf.split(" ")[0];
						duration = buf;
					}
				}
				else if(getSize && buf.startsWith( "Image Size" ) ){
					String size = buf.substring(buf.indexOf(":") + 2).trim();
					width = Integer.parseInt(size.split("x")[0]);
					height = Integer.parseInt(size.split("x")[1]);
				}
			}
			se.close();
			se = null;
		} catch (Exception e) {
			logger.error(e);
		}
		if(getSize){
			result = new Object[]{duration, width, height};
		}else{
			result = new Object[]{duration};
		}
		return result;
	}

	/**
	 * @return Returns the height.
	 */
	public Integer getHeight() {
		return height;
	}
	

	/**
	 * @param height The height to set.
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}
	

	/**
	 * @return Returns the length.
	 */
	public Float getLength() {
		return length;
	}
	

	/**
	 * @param length The length to set.
	 */
	public void setLength(Float length) {
		this.length = length;
	}
	

	/**
	 * @return Returns the width.
	 */
	public Integer getWidth() {
		return width;
	}
	

	/**
	 * @param width The width to set.
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}
	
	public String getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}
	
	public class MediaInfoThread extends Thread{
		private String[] cmd;
		private MediaInfo mediaInfo;
		private Process proc = null;
		private File file;
		private String metadataFileName;
		public String[] getCmd() {
			return cmd;
		}
		public void setCmd(String[] cmd) {
			this.cmd = cmd;
		}
		public MediaInfo getMediaInfo() {
			return mediaInfo;
		}
		public void setMediaInfo(MediaInfo mediaInfo) {
			this.mediaInfo = mediaInfo;
		}
		public Process getProc() {
			return proc;
		}
		public void setProc(Process proc) {
			this.proc = proc;
		}
		public File getFile() {
			return file;
		}
		public void setFile(File file) {
			this.file = file;
		}
		public String getMetadataFileName() {
			return metadataFileName;
		}
		public void setMetadataFileName(String metadataFileName) {
			this.metadataFileName = metadataFileName;
		}
	}

	public Integer getNormalizationBeginAvg() {
		return normalizationBeginAvg;
	}

	public void setNormalizationBeginAvg(Integer normalizationBeginAvg) {
		this.normalizationBeginAvg = normalizationBeginAvg;
	}
}
