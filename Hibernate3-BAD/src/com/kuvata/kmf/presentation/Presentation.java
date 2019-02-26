/*
 * Created on Nov 19, 2004
 */
package com.kuvata.kmf.presentation;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.SchemaDirectory;
import com.kuvata.kmf.attr.Attr;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.attr.StringAttr;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;
/**
 * @author anaber
 *
 */
public abstract class Presentation
{
	private static Logger logger = Logger.getLogger(Presentation.class);
    protected String SUBDIRECTORY;
	
    public Long assetId;
    private String decodedXmlFile; // Device side variable
    public PresenterType presenterType;
	private ReferencedFile referencedFile;
	
	public abstract ReferencedFile getReferencedFile();
	
    /**
     * 
     * @return
     * @throws HibernateException
     */    
    public String getFilename() throws HibernateException{
    	if(decodedXmlFile != null){
    		return SchemaDirectory.getSchemaBaseDirectory() + "/"+ Constants.PRESENTATIONS +"/" + SUBDIRECTORY + "/" + decodedXmlFile;
    	}else{
    		return SchemaDirectory.getSchemaBaseDirectory() + "/"+ Constants.PRESENTATIONS +"/" + SUBDIRECTORY + "/" + assetId + "-" + this.hashCode() + ".xml";
    	}
    }
    /**
     * 
     * @return
     */
    public String getRelativePathname(){
    	if(decodedXmlFile != null){
    		return SUBDIRECTORY + "/" + decodedXmlFile;
    	}else{
    		return SUBDIRECTORY + "/" + assetId + "-" + this.hashCode() + ".xml";
    	}
    }
    /**
     * 
     * @throws HibernateException
     * @throws FileNotFoundException
     * @throws NoSuchFieldException
     */
    public void xmlEncode() throws HibernateException, FileNotFoundException, NoSuchFieldException, ParseException
    {
        File f = new File( getFilename() );
        if( f.exists() && f.length() > 0)
        {
            // don't have to create it again! -- touch it so we know it's not old
        	f.setLastModified( System.currentTimeMillis() );
        	
        	// touch all referenced files within the presentation so we know they're not "old"
    		touchReferencedFiles( getFilename() );
            
    		return;
        }

        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(getFilename())));
        encoder.writeObject(this);
        encoder.close();
		
		// touch all referenced files within the presentation so we know they're not "old"
		touchReferencedFiles( getFilename() );
    }
    /**
     * 
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static Presentation xmlDecode(String fileName) throws FileNotFoundException
    {
    	File xmlFile = new File(fileName);
        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(xmlFile)));
        Presentation p = (Presentation) decoder.readObject();
        decoder.close();
        p.decodedXmlFile = xmlFile.getName();
        return p;
    }
	
	/**
	 * Locate and touch any ReferencedFiles within the given presentation file so we know
	 * they're not "old", and therefore won't be deleted on server clean up.
	 *  
	 * @param fileName
	 * @throws HibernateException
	 * @throws ParseException
	 */
	private static void touchReferencedFiles( String fileName ) throws HibernateException, ParseException
	{
		// Parse the presentation xml file and locate any ReferencedFiles
		Document d = new Document( new File(fileName) );
		Elements es = d.getElements( new XPath("//object[@class=\""+ Constants.REFERENCED_FILE_CLASS +"\"]"));
		while(es.hasMoreElements())
		{
			Element objectElement = es.next();
			String referencedFileRelativePath = objectElement.getElement( new XPath("void/string") ).getTextString();
			
			// Append the presentations directory to the relative path			
			String referencedFileloc = KuvataConfig.getKuvataHome() + 
									"/"+ Constants.SCHEMAS +					
									"/"+ SchemaDirectory.getSchema().getSchemaName() +
									"/"+ Constants.PRESENTATIONS +
									"/"+ referencedFileRelativePath;
			referencedFileloc = referencedFileloc.replaceAll("\\\\", "/");
			
			// If this ReferencedFile exists (it should at this point), touch it
			File referencedFile = new File( referencedFileloc );
			if( referencedFile.exists() == true )
			{
				// Touch the file so we know it's not old
				referencedFile.setLastModified( System.currentTimeMillis() );
			}				
		}
	}
	
	/*
	 * Bind any metadata parameters that are found in the given startPage.
	 * Split the start page to locate any metadata identifiers -- i.e. {Device.zipCode}
	 */
	protected String appendMetadataParameters(String startPage, Device device)
	{
		String newStartPage = "";
		try
		{			
			if( startPage != null && startPage.length() > 0 )
			{				
				String[] startPageParts = startPage.split("\\{.*?\\}");
				for( int i=0; i<startPageParts.length; i++)
				{			
					newStartPage += startPageParts[i];
					
					// If we've found a section after this one
					int endPos = startPage.length();
					if( i < startPageParts.length - 1 ){
						// Go to the start of the next section
						endPos = startPage.indexOf( startPageParts[i+1] );
					}
					
					// If we're at the end of the string, don't do anything
					if( (startPage.indexOf( startPageParts[i] ) + startPageParts[i].length() + 1) < startPage.length() )
					{				
						// Parse out the metadata parameter
						String metadataParam = startPage.substring( startPage.indexOf( startPageParts[i] ) + startPageParts[i].length() + 1, endPos - 1 );
						
						// Get the metadata type
						if( metadataParam.indexOf(".") > 0 )
						{							
							String metadataType = metadataParam.substring( 0, metadataParam.indexOf(".") );
							String metadataField = metadataParam.substring( metadataParam.indexOf(".") + 1 );
							
							if( metadataField.equals("deviceId") ) {
								newStartPage += device.getDeviceId().toString();
							} else {
								// Prepend the kmf package to the metadataType to get the full class name
								metadataType = "com.kuvata.kmf."+ metadataType;
								
								// Retrieve the metadata value for this metadata field
								AttrDefinition attrDefinition = AttrDefinition.getAttributeDefinition( metadataType, metadataField );
								if( attrDefinition != null )
								{
									/*
									 * Attempt to retrieve an Attr object for this device/attr definition.
									 * NOTE: Currently supporting only Device metadata. We may want to consider
									 * supporting other types of metadata (i.e. Segment, Playlist), but it would
									 * require a significant code change in the Content Scheduler (in order to determine
									 * the "current" Segment or Playlist) which we've decided to hold off on. 
									 */
									Attr a = Attr.getAttribute( device.getDeviceId(), attrDefinition.getAttrDefinitionId() );
									if( a != null && a instanceof StringAttr ){
										StringAttr stringAttr = (StringAttr)a;
										
										// Replace the metadata parameter with the actual value
										newStartPage += stringAttr.getFormattedValue();
									}
								}
							}
						}				
					}
				}		
			}			
		}catch(ClassNotFoundException e){
			logger.error( e );
		}
		return newStartPage;
	}
	
    /**
     * 
     * @param filename
     * @return
     */
    protected String getFileExtension(String filename)
    {
	    String extension = "";
	    int dot = filename.lastIndexOf(".");
	    if( dot>0 && dot<filename.length() )
	    {
	        extension = filename.substring(dot);
	    }
	    return extension;
    }


    /**
     * @return Returns the assetId.
     */
    public Long getAssetId() {
        return assetId;
    }
    /**
     * @param assetId The assetId to set.
     */
    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }
	/**
	 * @return Returns the presenterType.
	 */
	public PresenterType getPresenterType() {
		return presenterType;
	}
	/**
	 * @param presenterType The presenterType to set.
	 */
	public void setPresenterType(PresenterType presenterType) {
		this.presenterType = presenterType;
	}
}
