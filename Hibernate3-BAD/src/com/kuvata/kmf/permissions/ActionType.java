package com.kuvata.kmf.permissions;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.util.ContentRotationImporter;
import com.kuvata.kmf.util.PlaylistImporter;
import com.kuvata.kmm.actions.AddAssetToPlaylistAction;
import com.kuvata.kmm.actions.AddAssetToPlaylistGroupAction;
import com.kuvata.kmm.actions.AddContentRotationTargetingAction;
import com.kuvata.kmm.actions.AddMirrorSourceDevicesAction;
import com.kuvata.kmm.actions.AddTargetedAssetMemberAction;
import com.kuvata.kmm.actions.AdvertisersOverviewAction;
import com.kuvata.kmm.actions.AssetAiringReportAction;
import com.kuvata.kmm.actions.AssetExpirationEditAction;
import com.kuvata.kmm.actions.AssetIngesterAction;
import com.kuvata.kmm.actions.AssetMetadataEditAction;
import com.kuvata.kmm.actions.AssetPropertiesAction;
import com.kuvata.kmm.actions.AssetSummaryReportAction;
import com.kuvata.kmm.actions.AssetsAction;
import com.kuvata.kmm.actions.CampaignsOverviewAction;
import com.kuvata.kmm.actions.ContentRotationGroupPropertiesAction;
import com.kuvata.kmm.actions.ContentRotationMetadataEditAction;
import com.kuvata.kmm.actions.ContentRotationPropertiesAction;
import com.kuvata.kmm.actions.ContentRotationsAction;
import com.kuvata.kmm.actions.ContentRotationsOverviewAction;
import com.kuvata.kmm.actions.ContentSchedulerStatusOverviewAction;
import com.kuvata.kmm.actions.ContentUpdateOverviewAction;
import com.kuvata.kmm.actions.CopyDeviceAction;
import com.kuvata.kmm.actions.CopySegmentAction;
import com.kuvata.kmm.actions.CreateAssetAction;
import com.kuvata.kmm.actions.CreateContentRotationAction;
import com.kuvata.kmm.actions.CreateContentRotationAssetAction;
import com.kuvata.kmm.actions.CreateContentRotationGroupAction;
import com.kuvata.kmm.actions.CreateDeviceAction;
import com.kuvata.kmm.actions.CreateDeviceGroupAction;
import com.kuvata.kmm.actions.CreateDisplayareaAction;
import com.kuvata.kmm.actions.CreateLayoutAction;
import com.kuvata.kmm.actions.CreateMcmAction;
import com.kuvata.kmm.actions.CreatePairedAssetAction;
import com.kuvata.kmm.actions.CreatePlaylistAction;
import com.kuvata.kmm.actions.CreatePlaylistGroupAction;
import com.kuvata.kmm.actions.CreateSegmentAction;
import com.kuvata.kmm.actions.CreateSegmentGroupAction;
import com.kuvata.kmm.actions.DeviceGroupPropertiesAction;
import com.kuvata.kmm.actions.DeviceIngesterAction;
import com.kuvata.kmm.actions.DeviceMetadataEditAction;
import com.kuvata.kmm.actions.DevicePlaybackReportAction;
import com.kuvata.kmm.actions.DevicePropertiesAction;
import com.kuvata.kmm.actions.DevicePropertiesAdvancedAction;
import com.kuvata.kmm.actions.DevicesAction;
import com.kuvata.kmm.actions.DevicesOverviewAction;
import com.kuvata.kmm.actions.DevicesSnapshotReportAction;
import com.kuvata.kmm.actions.DevicesStatusReportAction;
import com.kuvata.kmm.actions.DisplayareasAction;
import com.kuvata.kmm.actions.DisplayareasOverviewAction;
import com.kuvata.kmm.actions.DynamicQueryAction;
import com.kuvata.kmm.actions.ExcludeAssetsFromDevicesAction;
import com.kuvata.kmm.actions.FooterMessageAction;
import com.kuvata.kmm.actions.GetChildNodesAction;
import com.kuvata.kmm.actions.ImportContentRotationToPlaylistAction;
import com.kuvata.kmm.actions.ImportIntoContentRotationsAction;
import com.kuvata.kmm.actions.ImportIntoPlaylistsAction;
import com.kuvata.kmm.actions.LayoutsAction;
import com.kuvata.kmm.actions.LayoutsOverviewAction;
import com.kuvata.kmm.actions.ManageCampaignAction;
import com.kuvata.kmm.actions.ManageVenuePartnerAction;
import com.kuvata.kmm.actions.PerformAjaxAction;
import com.kuvata.kmm.actions.PlaylistGroupPropertiesAction;
import com.kuvata.kmm.actions.PlaylistImportAction;
import com.kuvata.kmm.actions.PlaylistMetadataEditAction;
import com.kuvata.kmm.actions.PlaylistPropertiesAction;
import com.kuvata.kmm.actions.PlaylistsAction;
import com.kuvata.kmm.actions.PlaylistsOverviewAction;
import com.kuvata.kmm.actions.PublishDetailsAction;
import com.kuvata.kmm.actions.RunContentSchedulerAction;
import com.kuvata.kmm.actions.SavedReportsAction;
import com.kuvata.kmm.actions.SegmentGroupPropertiesAction;
import com.kuvata.kmm.actions.SegmentMetadataEditAction;
import com.kuvata.kmm.actions.SegmentPropertiesAction;
import com.kuvata.kmm.actions.SegmentsAction;
import com.kuvata.kmm.actions.SegmentsOverviewAction;
import com.kuvata.kmm.actions.SelectAdvertiserAction;
import com.kuvata.kmm.actions.SelectAssetAction;
import com.kuvata.kmm.actions.SelectContentRotationAction;
import com.kuvata.kmm.actions.SelectDeviceAction;
import com.kuvata.kmm.actions.SelectPlaylistAction;
import com.kuvata.kmm.actions.SelectVenuePartnerAction;
import com.kuvata.kmm.actions.ShowAssetsAction;
import com.kuvata.kmm.actions.TargetContentRotationAction;
import com.kuvata.kmm.actions.VenuePartnersOverviewAction;
import com.kuvata.kmm.tags.TreeNodes;


/**
 * NOTE: When adding a new ActionType, be sure to add the new ActionType to the appropriate filters in FilterType.
 * 
 * @author Jeff Randesi
 */
public class ActionType 
{		
	/*
	 * Unique actions
	 */
	public static final ActionType SEGMENTS_OVERVIEW = new ActionType( SegmentsOverviewAction.class.getName() );
	public static final ActionType CREATE_SEGMENT_GROUP = new ActionType( CreateSegmentGroupAction.class.getName() );
	public static final ActionType SEGMENT_GROUP_PROPERTIES = new ActionType( SegmentGroupPropertiesAction.class.getName() );
	public static final ActionType SEGMENT_PROPERTIES = new ActionType( SegmentPropertiesAction.class.getName() );
	public static final ActionType SEGMENT_METADATA_EDIT = new ActionType( SegmentMetadataEditAction.class.getName() );
	public static final ActionType CREATE_SEGMENT = new ActionType( CreateSegmentAction.class.getName() );
	public static final ActionType COPY_SEGMENT = new ActionType( CopySegmentAction.class.getName() );
	public static final ActionType PLAYLISTS_OVERVIEW = new ActionType( PlaylistsOverviewAction.class.getName() );
	public static final ActionType PLAYLIST_PROPERTIES = new ActionType( PlaylistPropertiesAction.class.getName() );
	public static final ActionType PLAYLIST_METADATA_EDIT = new ActionType( PlaylistMetadataEditAction.class.getName() );	
	public static final ActionType CREATE_PLAYLIST = new ActionType( CreatePlaylistAction.class.getName() );
	public static final ActionType CREATE_PLAYLIST_GROUP = new ActionType( CreatePlaylistGroupAction.class.getName() );
	public static final ActionType PLAYLIST_GROUP_PROPERTIES = new ActionType( PlaylistGroupPropertiesAction.class.getName() );
	public static final ActionType PLAYLIST_IMPORT = new ActionType( PlaylistImportAction.class.getName() );
	public static final ActionType PLAYLIST_IMPORTER = new ActionType( PlaylistImporter.class.getName() );
	public static final ActionType CONTENT_ROTATIONS_OVERVIEW = new ActionType( ContentRotationsOverviewAction.class.getName() );
	public static final ActionType CONTENT_ROTATION_GROUP_PROPERTIES = new ActionType( ContentRotationGroupPropertiesAction.class.getName() );
	public static final ActionType CONTENT_ROTATION_PROPERTIES = new ActionType( ContentRotationPropertiesAction.class.getName() );
	public static final ActionType SHOW_ASSETS = new ActionType( ShowAssetsAction.class.getName() );
	public static final ActionType ASSETS = new ActionType( AssetsAction.class.getName() );
	public static final ActionType DEVICES_OVERVIEW = new ActionType( DevicesOverviewAction.class.getName() );
	public static final ActionType CREATE_DEVICE = new ActionType( CreateDeviceAction.class.getName() );
	public static final ActionType CREATE_DEVICE_GROUP = new ActionType( CreateDeviceGroupAction.class.getName() );
	public static final ActionType COPY_DEVICE = new ActionType( CopyDeviceAction.class.getName() );	
	public static final ActionType DEVICE_GROUP_PROPERTIES = new ActionType( DeviceGroupPropertiesAction.class.getName() );
	public static final ActionType DEVICE_PROPERTIES = new ActionType( DevicePropertiesAction.class.getName() );
	public static final ActionType DEVICE_PROPERTIES_ADVANCED = new ActionType( DevicePropertiesAdvancedAction.class.getName() );
	public static final ActionType DEVICE_METADATA_EDIT = new ActionType( DeviceMetadataEditAction.class.getName() );	
	public static final ActionType LAYOUTS_OVERVIEW = new ActionType( LayoutsOverviewAction.class.getName() );
	public static final ActionType LAYOUTS = new ActionType( LayoutsAction.class.getName() );
	public static final ActionType CREATE_LAYOUT = new ActionType( CreateLayoutAction.class.getName() );
	public static final ActionType CREATE_DISPLAYAREA = new ActionType( CreateDisplayareaAction.class.getName() );
	public static final ActionType DISPLAYAREAS_OVERVIEW = new ActionType( DisplayareasOverviewAction.class.getName() );
	public static final ActionType DISPLAYAREAS = new ActionType( DisplayareasAction.class.getName() );
	public static final ActionType TREENODE_DEVICES = new ActionType( TreeNodes.class.getName() +"Devices" );
	public static final ActionType TREENODE_PLAYLISTS = new ActionType( TreeNodes.class.getName() +"Playlists" );
	public static final ActionType TREENODE_SEGMENTS = new ActionType( TreeNodes.class.getName() +"Segments" );
	public static final ActionType TREENODE_CONTENT_ROTATIONS = new ActionType( TreeNodes.class.getName() +"ContentRotations" );
	public static final ActionType TREEVIEW_DEVICES = new ActionType( DevicesAction.class.getName() +"Devices" );
	public static final ActionType TREEVIEW_PLAYLISTS = new ActionType( PlaylistsAction.class.getName() +"Playlists" );
	public static final ActionType TREEVIEW_SEGMENTS = new ActionType( SegmentsAction.class.getName() +"Segments" );
	public static final ActionType TREEVIEW_CONTENT_ROTATIONS = new ActionType( ContentRotationsAction.class.getName() +"ContentRotations" );	
	public static final ActionType SELECT_ASSET = new ActionType( SelectAssetAction.class.getName() );
	public static final ActionType SELECT_DEVICE = new ActionType( SelectDeviceAction.class.getName() );
	public static final ActionType SELECT_PLAYLIST = new ActionType( SelectPlaylistAction.class.getName() );
	public static final ActionType SELECT_CONTENT_ROTATION = new ActionType ( SelectContentRotationAction.class.getName() );
	public static final ActionType FOOTER_MESSAGE = new ActionType( FooterMessageAction.class.getName() );
	public static final ActionType PUBLISH_DETAILS = new ActionType( PublishDetailsAction.class.getName() );
	public static final ActionType GET_CHILD_NODES = new ActionType( GetChildNodesAction.class.getName() );
	public static final ActionType RUN_CONTENT_SCHEDULER = new ActionType( RunContentSchedulerAction.class.getName() );
	public static final ActionType EXCLUDE_ASSETS_FROM_DEVICES = new ActionType( ExcludeAssetsFromDevicesAction.class.getName() );
	public static final ActionType CREATE_CONTENT_ROTATION = new ActionType( CreateContentRotationAction.class.getName() );
	public static final ActionType CREATE_CONTENT_ROTATION_GROUP = new ActionType( CreateContentRotationGroupAction.class.getName() );
	public static final ActionType CREATE_CONTENT_ROTATION_ASSET = new ActionType( CreateContentRotationAssetAction.class.getName() );
	public static final ActionType ADD_ASSET_TO_PLAYLIST = new ActionType( AddAssetToPlaylistAction.class.getName() );
	public static final ActionType ADD_ASSET_TO_PLAYLIST_GROUP = new ActionType( AddAssetToPlaylistGroupAction.class.getName() );
	public static final ActionType TARGET_CONTENT_ROTATION = new ActionType( TargetContentRotationAction.class.getName() );
	public static final ActionType ADD_CONTENT_ROTATION_TARGETING = new ActionType( AddContentRotationTargetingAction.class.getName() );
	public static final ActionType CREATE_ASSET = new ActionType( CreateAssetAction.class.getName() );
	public static final ActionType ASSET_PROPERTIES = new ActionType( AssetPropertiesAction.class.getName() );
	public static final ActionType ASSET_METADATA_EDIT = new ActionType( AssetMetadataEditAction.class.getName() );	
	public static final ActionType DEVICES_STATUS_REPORT = new ActionType( DevicesStatusReportAction.class.getName() );
	public static final ActionType DEVICE_PLAYBACK_REPORT = new ActionType( DevicePlaybackReportAction.class.getName() );
	public static final ActionType DEVICE_SNAPSHOT_REPORT = new ActionType( DevicesSnapshotReportAction.class.getName() );
	public static final ActionType ASSET_SUMMARY_REPORT = new ActionType( AssetSummaryReportAction.class.getName() );
	public static final ActionType ASSET_AIRING_REPORT = new ActionType( AssetAiringReportAction.class.getName() );
	public static final ActionType CONTENT_SCHEDULER_STATUS_OVERVIEW = new ActionType( ContentSchedulerStatusOverviewAction.class.getName() );
	public static final ActionType IMPORT_CONTENT_ROTATION_TO_PLAYLIST = new ActionType( ImportContentRotationToPlaylistAction.class.getName() );
	public static final ActionType CONTENT_ROTATION_IMPORTER = new ActionType( ContentRotationImporter.class.getName() );
	public static final ActionType CREATE_MCM = new ActionType( CreateMcmAction.class.getName() );
	public static final ActionType ADD_TARGETED_ASSET_MEMBER = new ActionType( AddTargetedAssetMemberAction.class.getName() );
	public static final ActionType PERFORM_AJAX = new ActionType( PerformAjaxAction.class.getName() );	
	public static final ActionType ASSET_INGESTER = new ActionType( AssetIngesterAction.class.getName() );
	public static final ActionType DEVICE_INGESTER = new ActionType( DeviceIngesterAction.class.getName() );
	public static final ActionType CREATE_PAIRED_ASSET = new ActionType ( CreatePairedAssetAction.class.getName() );
	public static final ActionType ADD_MIRROR_SOURCE_DEVICES = new ActionType ( AddMirrorSourceDevicesAction.class.getName() );
	public static final ActionType DYNAMIC_QUERY = new ActionType ( DynamicQueryAction.class.getName() );
	public static final ActionType SVC_ASSET = new ActionType ( com.kuvata.kmm.svc.Asset.class.getName() );
	public static final ActionType SVC_ASSETS = new ActionType ( com.kuvata.kmm.svc.Assets.class.getName() );
	public static final ActionType SVC_DEVICE = new ActionType ( com.kuvata.kmm.svc.Device.class.getName() );
	public static final ActionType SVC_DEVICE_COMMAND = new ActionType ( com.kuvata.kmm.svc.DeviceCommand.class.getName() );
	public static final ActionType SVC_DEVICE_GROUPS = new ActionType ( com.kuvata.kmm.svc.DeviceGroups.class.getName() );
	public static final ActionType SVC_DEVICES = new ActionType ( com.kuvata.kmm.svc.Devices.class.getName() );
	public static final ActionType SVC_DEVICE_SCREENSHOT = new ActionType ( com.kuvata.kmm.svc.DeviceScreenshot.class.getName() );
	public static final ActionType SVC_METADATA = new ActionType ( com.kuvata.kmm.svc.Metadata.class.getName() );
	public static final ActionType CAMPAIGNS_OVERVIEW = new ActionType ( CampaignsOverviewAction.class.getName() );
	public static final ActionType MANAGE_CAMPAIGN = new ActionType ( ManageCampaignAction.class.getName() );
	public static final ActionType IMPORT_INTO_PLAYLISTS = new ActionType ( ImportIntoPlaylistsAction.class.getName() );
	public static final ActionType SAVED_REPORTS = new ActionType ( SavedReportsAction.class.getName() );
	public static final ActionType ASSET_EXPIRATION_EDIT = new ActionType ( AssetExpirationEditAction.class.getName() );
	public static final ActionType DEVICES = new ActionType ( DevicesAction.class.getName() );
	public static final ActionType ADVERTISERS_OVERVIEW = new ActionType ( AdvertisersOverviewAction.class.getName() );
	public static final ActionType SELECT_ADVERTISER = new ActionType ( SelectAdvertiserAction.class.getName() );
	public static final ActionType VENUE_PARTNERS_OVERVIEW = new ActionType ( VenuePartnersOverviewAction.class.getName() );
	public static final ActionType SELECT_VANUE_PARTNER = new ActionType ( SelectVenuePartnerAction.class.getName() );
	public static final ActionType MANAGE_VANUE_PARTNER = new ActionType ( ManageVenuePartnerAction.class.getName() );
	public static final ActionType CONTENT_UPDATE_OVERVIEW = new ActionType ( ContentUpdateOverviewAction.class.getName() );
	public static final ActionType CONTENT_ROTATION_METADATA_EDIT = new ActionType ( ContentRotationMetadataEditAction.class.getName() );
	public static final ActionType IMPORT_INTO_CONTENT_ROTATIONS = new ActionType ( ImportIntoContentRotationsAction.class.getName() );
	
	// Web service methods
	public static final ActionType CREATE_OR_UPDATE_ASSET = new ActionType( "createOrUpdateAsset" );
	public static final ActionType CREATE_OR_UPDATE_DEVICE = new ActionType( "createOrUpdateDevice" );
	public static final ActionType GET_DEVICES = new ActionType( "getDevices" );
	public static final ActionType CREATE_OR_UPDATE_PLAYLIST = new ActionType( "createOrUpdatePlaylist" );
	public static final ActionType CREATE_OR_UPDATE_SEGMENT = new ActionType( "createOrUpdateSegment" );
	public static final ActionType CREATE_OR_UPDATE_CONTENTROTATION = new ActionType( "createOrUpdateContentRotation" );
	
	public static final Map INSTANCES = new HashMap();
	private String name;	
		
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put( SEGMENTS_OVERVIEW.getName(), SEGMENTS_OVERVIEW );
		INSTANCES.put( CREATE_SEGMENT_GROUP.getName(), CREATE_SEGMENT_GROUP );
		INSTANCES.put( SEGMENT_GROUP_PROPERTIES.getName(), SEGMENT_GROUP_PROPERTIES );
		INSTANCES.put( SEGMENT_PROPERTIES.getName(), SEGMENT_PROPERTIES );
		INSTANCES.put( SEGMENT_METADATA_EDIT.getName(), SEGMENT_METADATA_EDIT );		
		INSTANCES.put( CREATE_SEGMENT.getName(), CREATE_SEGMENT );
		INSTANCES.put( COPY_SEGMENT.getName(), COPY_SEGMENT );
		INSTANCES.put( PLAYLISTS_OVERVIEW.getName(), PLAYLISTS_OVERVIEW );
		INSTANCES.put( PLAYLIST_PROPERTIES.getName(), PLAYLIST_PROPERTIES );
		INSTANCES.put( CREATE_PLAYLIST.getName(), CREATE_PLAYLIST );
		INSTANCES.put( CREATE_PLAYLIST_GROUP.getName(), CREATE_PLAYLIST_GROUP );
		INSTANCES.put( PLAYLIST_GROUP_PROPERTIES.getName(), PLAYLIST_GROUP_PROPERTIES );
		INSTANCES.put( PLAYLIST_METADATA_EDIT.getName(), PLAYLIST_METADATA_EDIT );		
		INSTANCES.put( PLAYLIST_IMPORT.getName(), PLAYLIST_IMPORT );
		INSTANCES.put( PLAYLIST_IMPORTER.getName(), PLAYLIST_IMPORTER );
		INSTANCES.put( CONTENT_ROTATIONS_OVERVIEW.getName(), CONTENT_ROTATIONS_OVERVIEW );
		INSTANCES.put( CONTENT_ROTATION_GROUP_PROPERTIES.getName(), CONTENT_ROTATION_GROUP_PROPERTIES );
		INSTANCES.put( CONTENT_ROTATION_PROPERTIES.getName(), CONTENT_ROTATION_PROPERTIES );
		INSTANCES.put( CREATE_CONTENT_ROTATION.getName(), CREATE_CONTENT_ROTATION );
		INSTANCES.put( CREATE_CONTENT_ROTATION_GROUP.getName(), CREATE_CONTENT_ROTATION_GROUP );
		INSTANCES.put( SHOW_ASSETS.getName(), SHOW_ASSETS );
		INSTANCES.put( ASSETS.getName(), ASSETS );		
		INSTANCES.put( LAYOUTS_OVERVIEW.getName(), LAYOUTS_OVERVIEW );
		INSTANCES.put( LAYOUTS.getName(), LAYOUTS );
		INSTANCES.put( CREATE_DISPLAYAREA.getName(), CREATE_DISPLAYAREA );
		INSTANCES.put( DISPLAYAREAS_OVERVIEW.getName(), DISPLAYAREAS_OVERVIEW );
		INSTANCES.put( DISPLAYAREAS.getName(), DISPLAYAREAS );
		INSTANCES.put( DEVICES_OVERVIEW.getName(), DEVICES_OVERVIEW );		
		INSTANCES.put( CREATE_DEVICE.getName(), CREATE_DEVICE );
		INSTANCES.put( CREATE_DEVICE_GROUP.getName(), CREATE_DEVICE_GROUP );
		INSTANCES.put( COPY_DEVICE.getName(), COPY_DEVICE );		
		INSTANCES.put( DEVICE_GROUP_PROPERTIES.getName(), DEVICE_GROUP_PROPERTIES );
		INSTANCES.put( DEVICE_PROPERTIES.getName(), DEVICE_PROPERTIES );
		INSTANCES.put( DEVICE_PROPERTIES_ADVANCED.getName(), DEVICE_PROPERTIES_ADVANCED );		
		INSTANCES.put( DEVICE_METADATA_EDIT.getName(), DEVICE_METADATA_EDIT );		
		INSTANCES.put( TREENODE_DEVICES.getName(), TREENODE_DEVICES );	
		INSTANCES.put( TREENODE_PLAYLISTS.getName(), TREENODE_PLAYLISTS );	
		INSTANCES.put( TREENODE_SEGMENTS.getName(), TREENODE_SEGMENTS );	
		INSTANCES.put( TREENODE_CONTENT_ROTATIONS.getName(), TREENODE_CONTENT_ROTATIONS );
		INSTANCES.put( TREEVIEW_DEVICES.getName(), TREEVIEW_DEVICES );
		INSTANCES.put( TREEVIEW_PLAYLISTS.getName(), TREEVIEW_PLAYLISTS );	
		INSTANCES.put( TREEVIEW_SEGMENTS.getName(), TREEVIEW_SEGMENTS );	
		INSTANCES.put( TREEVIEW_CONTENT_ROTATIONS.getName(), TREEVIEW_CONTENT_ROTATIONS );		
		INSTANCES.put( SELECT_ASSET.getName(), SELECT_ASSET );
		INSTANCES.put( SELECT_DEVICE.getName(), SELECT_DEVICE );
		INSTANCES.put( SELECT_PLAYLIST.getName(), SELECT_PLAYLIST );
		INSTANCES.put( FOOTER_MESSAGE.getName(), FOOTER_MESSAGE );
		INSTANCES.put( PUBLISH_DETAILS.getName(), PUBLISH_DETAILS );
		INSTANCES.put( GET_CHILD_NODES.getName(), GET_CHILD_NODES );		
		INSTANCES.put( RUN_CONTENT_SCHEDULER.getName(), RUN_CONTENT_SCHEDULER );
		INSTANCES.put( EXCLUDE_ASSETS_FROM_DEVICES.getName(), EXCLUDE_ASSETS_FROM_DEVICES );
		INSTANCES.put( CREATE_CONTENT_ROTATION_ASSET.getName(), CREATE_CONTENT_ROTATION_ASSET );
		INSTANCES.put( ADD_ASSET_TO_PLAYLIST.getName(), ADD_ASSET_TO_PLAYLIST );
		INSTANCES.put( ADD_ASSET_TO_PLAYLIST_GROUP.getName(), ADD_ASSET_TO_PLAYLIST_GROUP );
		INSTANCES.put( CREATE_LAYOUT.getName(), CREATE_LAYOUT );
		INSTANCES.put( CREATE_ASSET.getName(), CREATE_ASSET );
		INSTANCES.put( ASSET_PROPERTIES.getName(), ASSET_PROPERTIES );
		INSTANCES.put( ASSET_METADATA_EDIT.getName(), ASSET_METADATA_EDIT );		
		INSTANCES.put( TARGET_CONTENT_ROTATION.getName(), TARGET_CONTENT_ROTATION );
		INSTANCES.put( ADD_CONTENT_ROTATION_TARGETING.getName(), ADD_CONTENT_ROTATION_TARGETING );	
		INSTANCES.put( DEVICES_STATUS_REPORT.getName(), DEVICES_STATUS_REPORT );
		INSTANCES.put( DEVICE_PLAYBACK_REPORT.getName(), DEVICE_PLAYBACK_REPORT );
		INSTANCES.put( DEVICE_SNAPSHOT_REPORT.getName(), DEVICE_SNAPSHOT_REPORT );		
		INSTANCES.put( ASSET_SUMMARY_REPORT.getName(), ASSET_SUMMARY_REPORT );
		INSTANCES.put( ASSET_AIRING_REPORT.getName(), ASSET_AIRING_REPORT );
		INSTANCES.put( CONTENT_SCHEDULER_STATUS_OVERVIEW.getName(), CONTENT_SCHEDULER_STATUS_OVERVIEW );
		INSTANCES.put( IMPORT_CONTENT_ROTATION_TO_PLAYLIST.getName(), IMPORT_CONTENT_ROTATION_TO_PLAYLIST );
		INSTANCES.put( CONTENT_ROTATION_IMPORTER.getName(), CONTENT_ROTATION_IMPORTER );
		INSTANCES.put( CREATE_MCM.getName(), CREATE_MCM );
		INSTANCES.put( CREATE_OR_UPDATE_ASSET.getName(), CREATE_OR_UPDATE_ASSET );
		INSTANCES.put( CREATE_OR_UPDATE_DEVICE.getName(), CREATE_OR_UPDATE_DEVICE );
		INSTANCES.put( GET_DEVICES.getName(), GET_DEVICES );
		INSTANCES.put( CREATE_OR_UPDATE_PLAYLIST.getName(), CREATE_OR_UPDATE_PLAYLIST );
		INSTANCES.put( CREATE_OR_UPDATE_SEGMENT.getName(), CREATE_OR_UPDATE_SEGMENT );
		INSTANCES.put( CREATE_OR_UPDATE_CONTENTROTATION.getName(), CREATE_OR_UPDATE_CONTENTROTATION );
		INSTANCES.put( ADD_TARGETED_ASSET_MEMBER.getName(), ADD_TARGETED_ASSET_MEMBER );
		INSTANCES.put( PERFORM_AJAX.getName(), PERFORM_AJAX );
		INSTANCES.put( ASSET_INGESTER.getName(), ASSET_INGESTER );
		INSTANCES.put( DEVICE_INGESTER.getName(), DEVICE_INGESTER );
		INSTANCES.put( CREATE_PAIRED_ASSET.getName(), CREATE_PAIRED_ASSET );
		INSTANCES.put( ADD_MIRROR_SOURCE_DEVICES.getName(), ADD_MIRROR_SOURCE_DEVICES );
		INSTANCES.put( DYNAMIC_QUERY.getName(), DYNAMIC_QUERY );	
		INSTANCES.put( SVC_ASSET.getName(), SVC_ASSET);
		INSTANCES.put( SVC_ASSETS.getName(), SVC_ASSETS);
		INSTANCES.put( SVC_DEVICE.getName(), SVC_DEVICE);
		INSTANCES.put( SVC_DEVICE_COMMAND.getName(), SVC_DEVICE_COMMAND);
		INSTANCES.put( SVC_DEVICE_GROUPS.getName(), SVC_DEVICE_GROUPS);
		INSTANCES.put( SVC_DEVICE_SCREENSHOT.getName(), SVC_DEVICE_SCREENSHOT);
		INSTANCES.put( SVC_DEVICES.getName(), SVC_DEVICES);
		INSTANCES.put( SVC_METADATA.getName(), SVC_METADATA);
		INSTANCES.put( CAMPAIGNS_OVERVIEW.getName(), CAMPAIGNS_OVERVIEW);
		INSTANCES.put( MANAGE_CAMPAIGN.getName(), MANAGE_CAMPAIGN);
		INSTANCES.put( IMPORT_INTO_PLAYLISTS.getName(), IMPORT_INTO_PLAYLISTS);
		INSTANCES.put( SAVED_REPORTS.getName(), SAVED_REPORTS);
		INSTANCES.put( ASSET_EXPIRATION_EDIT.getName(), ASSET_EXPIRATION_EDIT);
		INSTANCES.put( DEVICES.getName(), DEVICES);
		INSTANCES.put( ADVERTISERS_OVERVIEW.getName(), ADVERTISERS_OVERVIEW);
		INSTANCES.put( SELECT_ADVERTISER.getName(), SELECT_ADVERTISER);
		INSTANCES.put( VENUE_PARTNERS_OVERVIEW.getName(), VENUE_PARTNERS_OVERVIEW);
		INSTANCES.put( SELECT_VANUE_PARTNER.getName(), SELECT_VANUE_PARTNER);
		INSTANCES.put( MANAGE_VANUE_PARTNER.getName(), MANAGE_VANUE_PARTNER);
		INSTANCES.put( CONTENT_UPDATE_OVERVIEW.getName(), CONTENT_UPDATE_OVERVIEW);
		INSTANCES.put( CONTENT_ROTATION_METADATA_EDIT.getName(), CONTENT_ROTATION_METADATA_EDIT);
		INSTANCES.put( SELECT_CONTENT_ROTATION.getName(), SELECT_CONTENT_ROTATION);
		INSTANCES.put( IMPORT_INTO_CONTENT_ROTATIONS.getName(), IMPORT_INTO_CONTENT_ROTATIONS);
	}
	
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public ActionType(String name) {
		this.name = name;
	}
	
	public static ActionType getActionType(String name){
		return (ActionType)INSTANCES.get( name );
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
