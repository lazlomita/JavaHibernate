package com.kuvata.kmf;

import org.hibernate.HibernateException;

import com.kuvata.kmf.logging.HistorizableCollectionMember;

/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class PlaylistAsset extends PersistentEntity implements HistorizableCollectionMember {

	private Long playlistAssetId;
	private Playlist playlist;
	private IAsset asset;
	private AssetPresentation assetPresentation;
	private Integer seqNum;
	/**
	 * 
	 *
	 */
	public PlaylistAsset()
	{		
	}
	/**
	 * 
	 * @return
	 */
	public float getLength()
	{
	    if( assetPresentation != null )
	    {
	        return assetPresentation.getLength().floatValue();
	    }
	    else
	    {
	        return asset.getAssetPresentation().getLength().floatValue();
	    }
	}
	/**
	 * 
	 * @param playlistAssetId
	 * @return
	 * @throws HibernateException
	 */
	public static PlaylistAsset getPlaylistAsset(Long playlistAssetId) throws HibernateException
	{
		return (PlaylistAsset)PersistentEntity.load(PlaylistAsset.class, playlistAssetId);		
	}
	
	/**
	 * If there is an asset presentation object for this playlist
	 * load it -- else, load the asset's default asset presentation properties
	 * 
	 * @return
	 */
	public AssetPresentation loadAssetPresentation()
	{
		if(this.getAssetPresentation() != null)
		{
			return this.getAssetPresentation();
		}
		else
		{
			return this.getAsset().getAssetPresentation();
		}
	}
	
	/**
	 * Creates a new playlistAsset object.
	 * 
	 * @param playlist
	 * @param asset
	 * @param length
	 * @param copyPairedAssets
	 * @return
	 */
	public static PlaylistAsset create(Playlist playlist, Asset asset, String length, Layout layout, Displayarea displayarea, Boolean variableLength, boolean useAssetDefaults, boolean copyPairedAssets, boolean resetPlaylistDisplayareas, boolean updatePlaylist)
	{		
		// Create a playlist asset presentation object
		AssetPresentation ap = new AssetPresentation();
		
		// If the "Use Defaults" radio button was selected
		if( useAssetDefaults )
		{
			// Populate the asset presentation object with the selected asset's presentation properties
			ap.setLayout( asset.getAssetPresentation().getLayout() );
			ap.setDisplayarea( asset.getAssetPresentation().getDisplayarea() );
			ap.setLength( asset.getAssetPresentation().getLength() );
		}
		else
		{
			// Use the values that were selected on the page
			ap.setLayout( layout );
			ap.setDisplayarea( displayarea );
			
			// If "DEFAULT" was submitted, use the length of the selected asset
			if( length.equalsIgnoreCase( Constants.DEFAULT ) ){
				ap.setLength( asset.getAssetPresentation().getLength() );
			} else {
				ap.setLength( new Float( length ) );	
			}				
		}
		ap.setVariableLength( variableLength );
		ap.save();
		
		// Copy the paired assets from the selected asset's if specified
		if( copyPairedAssets )
		{			
			asset.copyPairedAssets( ap );
			ap.update();
		}
		
		// Create a playlist asset object for the selected asset
		PlaylistAsset pa = new PlaylistAsset();			
		int seqNum = playlist.getPlaylistAssets().size();
		pa.setAsset( asset );
		pa.setAssetPresentation( ap );
		pa.setPlaylist( playlist );			
		pa.setSeqNum( new Integer(seqNum) );			
		pa.save();	
		
		playlist.getPlaylistAssets().add( pa );
		
		if(updatePlaylist){
			playlist.update();
			
			// Create PlaylistDisplayarea objects for secondary displayareas if necessary
			if( resetPlaylistDisplayareas ){
				pa.getPlaylist().resetPlaylistDisplayareas();
			}	
		}
		
		return pa;
	}
	
	/**
	 * 
	 */
	public void update(boolean resetPlaylistDisplayareas) throws HibernateException
	{
		// Perform the update
		super.update();
		
		// Create PlaylistDisplayarea objects for secondary displayareas if necessary
		if(resetPlaylistDisplayareas)
			this.getPlaylist().resetPlaylistDisplayareas();	
	}
	
	/**
	 * 
	 */
	public void delete() throws HibernateException
	{
		// Remove this object from the parent collection
		this.asset.getPlaylistAssets().remove( this );		
		this.playlist.getPlaylistAssets().remove( this );
		super.delete();
		
		// Must delete the asset presentation after the playlist asset
		// Otherwise cascading causes unexpected rowcount error
		if(this.assetPresentation != null) {
			this.assetPresentation.delete();
		}		
	}
	
/*	TODO: implement these - might be able to get rid of the list instead of set mapping 
 		  if seq_nm is added to the hashcode
 
 	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof PlaylistAsset) ) result = false;
		
		PlaylistAsset pa = (PlaylistAsset) other;		
		if(this.hashCode() == pa.hashCode())
			result =  true;
		
		return result;					
	}
	
	public int hashCode()
	{
		int result = "PlaylistAsset".hashCode();
		result = 29 * result + String.valueOf(this.getAsset().getAssetId()).hashCode();
		result = 29 * result + String.valueOf(this.getPlaylist().getPlaylistId()).hashCode();		
		return result;
	}	*/	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getPlaylistAssetId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getPlaylist().getPlaylistId();
	}		
	
	public String getEntityName()
	{
		return this.getAsset().getAssetName();
	}

	/**
	 * @return Returns the asset.
	 */
	public IAsset getAsset() {
		return asset;
	}

	/**
	 * @param asset The asset to set.
	 */
	public void setAsset(IAsset asset) {
		this.asset = asset;
	}

	/**
	 * @return Returns the assetPresentation.
	 */
	public AssetPresentation getAssetPresentation() {
		return assetPresentation;
	}

	/**
	 * @param assetPresentation The assetPresentation to set.
	 */
	public void setAssetPresentation(AssetPresentation assetPresentation) {
		this.assetPresentation = assetPresentation;
	}

	/**
	 * @return Returns the playlist.
	 */
	public Playlist getPlaylist() {
		return playlist;
	}

	/**
	 * @param playlist The playlist to set.
	 */
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	/**
	 * @return Returns the playlistAssetId.
	 */
	public Long getPlaylistAssetId() {
		return playlistAssetId;
	}

	/**
	 * @param playlistAssetId The playlistAssetId to set.
	 */
	public void setPlaylistAssetId(Long playlistAssetId) {
		this.playlistAssetId = playlistAssetId;
	}

	/**
	 * @return Returns the seqNum.
	 */
	public Integer getSeqNum() {
		// Necessary for bidirectional one-to-many with an indexed collection
		//return new Integer(this.getPlaylist().getPlaylistAssets().indexOf( this ));
		return this.seqNum;
	}

	/**
	 * @param seqNum The seqNum to set.
	 */
	public void setSeqNum(Integer seqNum) {
		// not used, calculated value, see getIndex() method
		this.seqNum = seqNum;
	}

}
