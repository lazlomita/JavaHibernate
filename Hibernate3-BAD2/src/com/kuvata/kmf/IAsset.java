package com.kuvata.kmf;

import java.util.Set;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.AssetType;


public interface IAsset {

	public Long getAssetId();
	public String getAssetName();		
	public AssetPresentation getAssetPresentation();
	public Set getPairedAssets();
	public Set getAssetSegmentParts();
	public Set getContentRotationAssets();
	public Set getPlaylistAssets();
	
	public abstract String getPresentationType();
	public abstract String getCreateAssetPage();
	public abstract String getAssetPropertiesPage();
	public abstract AssetType getAssetType();	
	public abstract String getPreviewPath() throws HibernateException;
	public abstract String getThumbnailPath() throws HibernateException;
	public abstract String renderHTML();
	
	public void makeDirty(boolean lengthChanged, boolean expirationChanged) throws HibernateException;
	public void update() throws HibernateException;
	public void removeFromPlaylists() throws HibernateException;
	public void delete() throws HibernateException;	
}
