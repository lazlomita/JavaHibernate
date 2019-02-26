package com.kuvata.kmf.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 * 
 * @author Jeff Randesi
 */
public class FilterType 
{			
	/*
	 * Parameter names
	 */
	public static final String PARAM_ROLE_IDS = "roleIds";
	public static final String PARAM_APP_USER_ID = "appUserId";	
	public static final String PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES = "excludeEntitiesWithNoRoles";
	
	/*
	 * Step 1: Define the filters
	 */
	public static final FilterType SEGMENTS_FILTER = new FilterType("segmentsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType SEGMENTS_FILTER_ADMIN = new FilterType("segmentsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType PLAYLISTS_FILTER = new FilterType("playlistsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType PLAYLISTS_FILTER_ADMIN = new FilterType("playlistsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CONTENT_ROTATIONS_FILTER = new FilterType("contentRotationsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType CONTENT_ROTATIONS_FILTER_ADMIN = new FilterType("contentRotationsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType ASSETS_FILTER = new FilterType("assetsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType ASSETS_FILTER_ADMIN = new FilterType("assetsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType DEVICES_FILTER = new FilterType("devicesFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType DEVICES_FILTER_ADMIN = new FilterType("devicesFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType LAYOUTS_FILTER = new FilterType("layoutsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType LAYOUTS_FILTER_ADMIN = new FilterType("layoutsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType DISPLAYAREAS_FILTER = new FilterType("displayareasFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType DISPLAYAREAS_FILTER_ADMIN = new FilterType("displayareasFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType SEGMENT_GRP_MEMBER_FILTER = new FilterType("segmentGrpMemberFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType SEGMENT_GRP_MEMBER_FILTER_ADMIN = new FilterType("segmentGrpMemberFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType PLAYLIST_GRP_MEMBER_FILTER = new FilterType("playlistGrpMemberFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType PLAYLIST_GRP_MEMBER_FILTER_ADMIN = new FilterType("playlistGrpMemberFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CONTENT_ROTATION_GRP_MEMBER_FILTER = new FilterType("contentRotationGrpMemberFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType CONTENT_ROTATION_GRP_MEMBER_FILTER_ADMIN = new FilterType("contentRotationGrpMemberFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType GRP_MEMBER_FILTER = new FilterType("grpMemberFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType GRP_MEMBER_FILTER_ADMIN = new FilterType("grpMemberFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType DEVICE_GRP_MEMBER_FILTER = new FilterType("deviceGrpMemberFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType DEVICE_GRP_MEMBER_FILTER_ADMIN = new FilterType("deviceGrpMemberFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType DIRTY_FILTER = new FilterType("dirtyFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType DIRTY_FILTER_ADMIN = new FilterType("dirtyFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType PLAYBACK_EVENTS_ASSET_FILTER = new FilterType("playbackEventsAssetFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType PLAYBACK_EVENTS_ASSET_FILTER_ADMIN = new FilterType("playbackEventsAssetFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType PLAYBACK_EVENTS_DEVICE_FILTER = new FilterType("playbackEventsDeviceFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType PLAYBACK_EVENTS_DEVICE_FILTER_ADMIN = new FilterType("playbackEventsDeviceFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CONTENT_SCHEDULE_EVENTS_ASSET_FILTER = new FilterType("contentScheduleEventsAssetFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType CONTENT_SCHEDULE_EVENTS_ASSET_FILTER_ADMIN = new FilterType("contentScheduleEventsAssetFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER = new FilterType("contentScheduleEventsDeviceFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER_ADMIN = new FilterType("contentScheduleEventsDeviceFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CONTENT_SCHEDULER_STATUS_FILTER = new FilterType("contentSchedulerStatusFilter", new String[]{PARAM_APP_USER_ID} );
	public static final FilterType MCM_FILTER = new FilterType("mcmFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType MCM_FILTER_ADMIN = new FilterType("mcmFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType ATTR_DEFINITION_FILTER = new FilterType("attrDefinitionFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType ATTR_DEFINITION_FILTER_ADMIN = new FilterType("attrDefinitionFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType ASSET_INGESTER_STATUS_FILTER = new FilterType("assetIngesterStatusFilter", new String[]{PARAM_APP_USER_ID} );
	public static final FilterType DEVICE_INGESTER_STATUS_FILTER = new FilterType("deviceIngesterStatusFilter", new String[]{PARAM_APP_USER_ID} );
	public static final FilterType GRPS_FILTER = new FilterType("grpsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType GRPS_FILTER_ADMIN = new FilterType("grpsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType MIRROR_PLAYERS_FILTER = new FilterType("mirrorPlayersFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType MIRROR_PLAYERS_FILTER_ADMIN = new FilterType("mirrorPlayersFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CAMPAIGNS_FILTER = new FilterType("campaignsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType CAMPAIGNS_FILTER_ADMIN = new FilterType("campaignsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType SAVED_REPORTS_FILTER = new FilterType("savedReportsFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType SAVED_REPORTS_FILTER_ADMIN = new FilterType("savedReportsFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType ADVERTISERS_FILTER = new FilterType("advertisersFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType ADVERTISERS_FILTER_ADMIN = new FilterType("advertisersFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType VENUE_PARTNERS_FILTER = new FilterType("venuePartnersFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType VENUE_PARTNERS_FILTER_ADMIN = new FilterType("venuePartnersFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );
	public static final FilterType CONTENT_UPDATE_FILTER = new FilterType("contentUpdateFilter", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES, PARAM_APP_USER_ID} );
	public static final FilterType CONTENT_UPDATE_FILTER_ADMIN = new FilterType("contentUpdateFilterAdmin", new String[]{PARAM_ROLE_IDS, PARAM_EXCLUDE_ENTITIES_WITH_NO_ROLES} );

	private String name;
	private String[] params;
	private Set actions = new HashSet();
	public static final Map INSTANCES = new HashMap();
		
	/**
	 * 
	 */	    
	static
	{
		/*
		 * Step 2: Define which actions apply to each filter
		 */
		SEGMENTS_FILTER.getActions().add( ActionType.TREENODE_SEGMENTS );
		SEGMENTS_FILTER.getActions().add( ActionType.TREEVIEW_SEGMENTS );
		SEGMENTS_FILTER.getActions().add( ActionType.SEGMENTS_OVERVIEW );
		SEGMENTS_FILTER.getActions().add( ActionType.CREATE_SEGMENT_GROUP );
		SEGMENTS_FILTER.getActions().add( ActionType.SEGMENT_GROUP_PROPERTIES );
		SEGMENTS_FILTER.getActions().add( ActionType.COPY_SEGMENT );
		SEGMENTS_FILTER.getActions().add( ActionType.CREATE_OR_UPDATE_DEVICE );
		
		PLAYLISTS_FILTER.getActions().add( ActionType.TREENODE_PLAYLISTS );
		PLAYLISTS_FILTER.getActions().add( ActionType.TREEVIEW_PLAYLISTS );
		PLAYLISTS_FILTER.getActions().add( ActionType.PLAYLISTS_OVERVIEW );
		PLAYLISTS_FILTER.getActions().add( ActionType.CREATE_PLAYLIST );
		PLAYLISTS_FILTER.getActions().add( ActionType.CREATE_PLAYLIST_GROUP );
		PLAYLISTS_FILTER.getActions().add( ActionType.PLAYLIST_GROUP_PROPERTIES );
		PLAYLISTS_FILTER.getActions().add( ActionType.SELECT_PLAYLIST );
		PLAYLISTS_FILTER.getActions().add( ActionType.CREATE_SEGMENT );
		PLAYLISTS_FILTER.getActions().add( ActionType.GET_CHILD_NODES );
		PLAYLISTS_FILTER.getActions().add( ActionType.ADD_CONTENT_ROTATION_TARGETING );
		PLAYLISTS_FILTER.getActions().add( ActionType.PLAYLIST_IMPORTER );
		PLAYLISTS_FILTER.getActions().add( ActionType.IMPORT_INTO_PLAYLISTS );
				
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.TREENODE_CONTENT_ROTATIONS );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.TREEVIEW_CONTENT_ROTATIONS );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.CONTENT_ROTATIONS_OVERVIEW );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.CREATE_CONTENT_ROTATION );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.CREATE_CONTENT_ROTATION_GROUP );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.CONTENT_ROTATION_GROUP_PROPERTIES );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.TARGET_CONTENT_ROTATION );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.PLAYLIST_PROPERTIES );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.IMPORT_CONTENT_ROTATION_TO_PLAYLIST );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.CONTENT_ROTATION_IMPORTER );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.DYNAMIC_QUERY );
		CONTENT_ROTATIONS_FILTER.getActions().add( ActionType.GET_CHILD_NODES );
		
		ASSETS_FILTER.getActions().add( ActionType.SHOW_ASSETS );	
		ASSETS_FILTER.getActions().add( ActionType.SELECT_ASSET );	
		ASSETS_FILTER.getActions().add( ActionType.EXCLUDE_ASSETS_FROM_DEVICES );	
		ASSETS_FILTER.getActions().add( ActionType.CREATE_CONTENT_ROTATION_ASSET );	
		ASSETS_FILTER.getActions().add( ActionType.ADD_ASSET_TO_PLAYLIST );
		ASSETS_FILTER.getActions().add( ActionType.ADD_ASSET_TO_PLAYLIST_GROUP );
		ASSETS_FILTER.getActions().add( ActionType.TARGET_CONTENT_ROTATION );
		ASSETS_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		ASSETS_FILTER.getActions().add( ActionType.CREATE_ASSET );
		ASSETS_FILTER.getActions().add( ActionType.CREATE_OR_UPDATE_ASSET );
		ASSETS_FILTER.getActions().add( ActionType.PLAYLIST_IMPORTER );
		ASSETS_FILTER.getActions().add( ActionType.CONTENT_ROTATION_IMPORTER );
		ASSETS_FILTER.getActions().add( ActionType.ADD_TARGETED_ASSET_MEMBER );
		ASSETS_FILTER.getActions().add( ActionType.CREATE_PAIRED_ASSET );
		ASSETS_FILTER.getActions().add( ActionType.DYNAMIC_QUERY );
		ASSETS_FILTER.getActions().add( ActionType.MANAGE_CAMPAIGN );
		ASSETS_FILTER.getActions().add( ActionType.ASSET_EXPIRATION_EDIT );
		ASSETS_FILTER.getActions().add( ActionType.CREATE_SEGMENT );
		ASSETS_FILTER.getActions().add( ActionType.SVC_ASSET );
		ASSETS_FILTER.getActions().add( ActionType.SVC_ASSETS );
			
		DEVICES_FILTER.getActions().add( ActionType.TREENODE_DEVICES );
		DEVICES_FILTER.getActions().add( ActionType.TREEVIEW_DEVICES );
		DEVICES_FILTER.getActions().add( ActionType.DEVICES_OVERVIEW );
		DEVICES_FILTER.getActions().add( ActionType.CREATE_DEVICE );
		DEVICES_FILTER.getActions().add( ActionType.CREATE_DEVICE_GROUP );
		DEVICES_FILTER.getActions().add( ActionType.DEVICE_GROUP_PROPERTIES );
		DEVICES_FILTER.getActions().add( ActionType.SELECT_DEVICE );				
		DEVICES_FILTER.getActions().add( ActionType.CREATE_SEGMENT );
		DEVICES_FILTER.getActions().add( ActionType.GET_CHILD_NODES );
		DEVICES_FILTER.getActions().add( ActionType.RUN_CONTENT_SCHEDULER );
		DEVICES_FILTER.getActions().add( ActionType.EXCLUDE_ASSETS_FROM_DEVICES );
		DEVICES_FILTER.getActions().add( ActionType.TARGET_CONTENT_ROTATION );		
		DEVICES_FILTER.getActions().add( ActionType.ADD_CONTENT_ROTATION_TARGETING );
		DEVICES_FILTER.getActions().add( ActionType.DEVICES_STATUS_REPORT );
		DEVICES_FILTER.getActions().add( ActionType.DEVICE_PLAYBACK_REPORT );
		DEVICES_FILTER.getActions().add( ActionType.DEVICE_SNAPSHOT_REPORT );
		DEVICES_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		DEVICES_FILTER.getActions().add( ActionType.COPY_DEVICE );
		DEVICES_FILTER.getActions().add( ActionType.ADD_TARGETED_ASSET_MEMBER );
		DEVICES_FILTER.getActions().add( ActionType.CREATE_OR_UPDATE_DEVICE );
		DEVICES_FILTER.getActions().add( ActionType.DEVICE_PROPERTIES_ADVANCED );
		DEVICES_FILTER.getActions().add( ActionType.GET_DEVICES );
		DEVICES_FILTER.getActions().add( ActionType.ADD_MIRROR_SOURCE_DEVICES );
		DEVICES_FILTER.getActions().add(ActionType.SVC_DEVICE);
		DEVICES_FILTER.getActions().add(ActionType.SVC_DEVICE_COMMAND);
		DEVICES_FILTER.getActions().add(ActionType.SVC_DEVICE_SCREENSHOT);
		DEVICES_FILTER.getActions().add(ActionType.SVC_DEVICES);
					
		LAYOUTS_FILTER.getActions().add( ActionType.LAYOUTS_OVERVIEW );
		LAYOUTS_FILTER.getActions().add( ActionType.LAYOUTS );
		LAYOUTS_FILTER.getActions().add( ActionType.CREATE_LAYOUT );	
		LAYOUTS_FILTER.getActions().add( ActionType.CREATE_ASSET );	
		LAYOUTS_FILTER.getActions().add( ActionType.ADD_ASSET_TO_PLAYLIST );
		LAYOUTS_FILTER.getActions().add( ActionType.ADD_ASSET_TO_PLAYLIST_GROUP );
		LAYOUTS_FILTER.getActions().add( ActionType.PLAYLIST_IMPORT );
		LAYOUTS_FILTER.getActions().add( ActionType.PLAYLIST_IMPORTER );
		LAYOUTS_FILTER.getActions().add( ActionType.CREATE_OR_UPDATE_ASSET );
		LAYOUTS_FILTER.getActions().add( ActionType.IMPORT_CONTENT_ROTATION_TO_PLAYLIST );
		LAYOUTS_FILTER.getActions().add( ActionType.DYNAMIC_QUERY );
		LAYOUTS_FILTER.getActions().add( ActionType.IMPORT_INTO_PLAYLISTS );
						
		DISPLAYAREAS_FILTER.getActions().add( ActionType.DISPLAYAREAS_OVERVIEW );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.DISPLAYAREAS );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.CREATE_DISPLAYAREA );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.CREATE_LAYOUT );		
		DISPLAYAREAS_FILTER.getActions().add( ActionType.PLAYLIST_IMPORTER );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.PERFORM_AJAX );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.ADD_CONTENT_ROTATION_TARGETING );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.IMPORT_CONTENT_ROTATION_TO_PLAYLIST );
		DISPLAYAREAS_FILTER.getActions().add( ActionType.IMPORT_INTO_PLAYLISTS );

		SEGMENT_GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_SEGMENTS );
		SEGMENT_GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_SEGMENTS );
		SEGMENT_GRP_MEMBER_FILTER.getActions().add( ActionType.CREATE_SEGMENT_GROUP );
		SEGMENT_GRP_MEMBER_FILTER.getActions().add( ActionType.SEGMENT_GROUP_PROPERTIES );

		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_PLAYLISTS );
		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_PLAYLISTS );
		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.CREATE_PLAYLIST_GROUP );	
		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.PLAYLIST_GROUP_PROPERTIES );	
		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.GET_CHILD_NODES );
		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.ADD_CONTENT_ROTATION_TARGETING );
		PLAYLIST_GRP_MEMBER_FILTER.getActions().add( ActionType.IMPORT_INTO_PLAYLISTS );
				
		CONTENT_ROTATION_GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_CONTENT_ROTATIONS );
		CONTENT_ROTATION_GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_CONTENT_ROTATIONS );
		CONTENT_ROTATION_GRP_MEMBER_FILTER.getActions().add( ActionType.CREATE_CONTENT_ROTATION_GROUP );
		CONTENT_ROTATION_GRP_MEMBER_FILTER.getActions().add( ActionType.CONTENT_ROTATION_GROUP_PROPERTIES );
		CONTENT_ROTATION_GRP_MEMBER_FILTER.getActions().add( ActionType.GET_CHILD_NODES );
		
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_DEVICES );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_DEVICES );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICE_GROUP_PROPERTIES );		
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.RUN_CONTENT_SCHEDULER );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.EXCLUDE_ASSETS_FROM_DEVICES );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.CREATE_SEGMENT );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICES_STATUS_REPORT );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICE_PLAYBACK_REPORT );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICE_SNAPSHOT_REPORT );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.SELECT_DEVICE );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.CREATE_DEVICE_GROUP );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.GET_CHILD_NODES );
		DEVICE_GRP_MEMBER_FILTER.getActions().add( ActionType.ADD_MIRROR_SOURCE_DEVICES );
		DEVICE_GRP_MEMBER_FILTER.getActions().add(ActionType.SVC_DEVICE_GROUPS);
		DEVICE_GRP_MEMBER_FILTER.getActions().add(ActionType.SVC_DEVICES);
		DEVICE_GRP_MEMBER_FILTER.getActions().add(ActionType.SVC_DEVICE);
		DEVICE_GRP_MEMBER_FILTER.getActions().add(ActionType.SVC_DEVICE_COMMAND);
		DEVICE_GRP_MEMBER_FILTER.getActions().add(ActionType.SVC_DEVICE_SCREENSHOT);
		
		DIRTY_FILTER.getActions().add( ActionType.FOOTER_MESSAGE );
		DIRTY_FILTER.getActions().add( ActionType.PUBLISH_DETAILS );
		
		GRP_MEMBER_FILTER.getActions().add( ActionType.EXCLUDE_ASSETS_FROM_DEVICES );		
		GRP_MEMBER_FILTER.getActions().add( ActionType.CREATE_SEGMENT );
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_SEGMENTS );	
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_PLAYLISTS );
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_CONTENT_ROTATIONS );
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREENODE_DEVICES );	
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_SEGMENTS );
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_PLAYLISTS );
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_CONTENT_ROTATIONS );
		GRP_MEMBER_FILTER.getActions().add( ActionType.TREEVIEW_DEVICES );
		GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICES_STATUS_REPORT );
		GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICE_PLAYBACK_REPORT );
		GRP_MEMBER_FILTER.getActions().add( ActionType.DEVICE_SNAPSHOT_REPORT );
		GRP_MEMBER_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		GRP_MEMBER_FILTER.getActions().add( ActionType.SELECT_DEVICE );		
		GRP_MEMBER_FILTER.getActions().add( ActionType.ADD_MIRROR_SOURCE_DEVICES );
		GRP_MEMBER_FILTER.getActions().add(ActionType.SVC_DEVICE_GROUPS);
		
		GRPS_FILTER.getActions().add( ActionType.CREATE_OR_UPDATE_DEVICE );
				
		PLAYBACK_EVENTS_ASSET_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		PLAYBACK_EVENTS_ASSET_FILTER.getActions().add( ActionType.ASSET_AIRING_REPORT );
		PLAYBACK_EVENTS_ASSET_FILTER.getActions().add( ActionType.DEVICE_PLAYBACK_REPORT );
		
		PLAYBACK_EVENTS_DEVICE_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		PLAYBACK_EVENTS_DEVICE_FILTER.getActions().add( ActionType.ASSET_AIRING_REPORT );
		
		CONTENT_SCHEDULE_EVENTS_ASSET_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		CONTENT_SCHEDULE_EVENTS_ASSET_FILTER.getActions().add( ActionType.ASSET_AIRING_REPORT );
		CONTENT_SCHEDULE_EVENTS_ASSET_FILTER.getActions().add( ActionType.DEVICE_PLAYBACK_REPORT );
		
		CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER.getActions().add( ActionType.ASSET_SUMMARY_REPORT );
		CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER.getActions().add( ActionType.ASSET_AIRING_REPORT );
		
		CONTENT_SCHEDULER_STATUS_FILTER.getActions().add( ActionType.CONTENT_SCHEDULER_STATUS_OVERVIEW );
		ASSET_INGESTER_STATUS_FILTER.getActions().add( ActionType.ASSET_INGESTER );
		DEVICE_INGESTER_STATUS_FILTER.getActions().add( ActionType.DEVICE_INGESTER );
		
		MCM_FILTER.getActions().add( ActionType.CREATE_MCM );
		
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SEGMENT_PROPERTIES );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SEGMENT_METADATA_EDIT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.PLAYLIST_PROPERTIES );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.PLAYLIST_METADATA_EDIT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ASSET_PROPERTIES );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ASSET_METADATA_EDIT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.DEVICE_PROPERTIES );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.DEVICE_METADATA_EDIT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ASSETS );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SELECT_ASSET );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SELECT_DEVICE );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SELECT_PLAYLIST );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SELECT_CONTENT_ROTATION );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.CONTENT_ROTATION_PROPERTIES );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.CONTENT_ROTATION_METADATA_EDIT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.IMPORT_INTO_CONTENT_ROTATIONS );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ADD_CONTENT_ROTATION_TARGETING );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ADD_TARGETED_ASSET_MEMBER );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.TARGET_CONTENT_ROTATION );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ADD_ASSET_TO_PLAYLIST );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ADD_ASSET_TO_PLAYLIST_GROUP );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.CREATE_CONTENT_ROTATION_ASSET );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.DYNAMIC_QUERY );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.IMPORT_INTO_PLAYLISTS );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.ASSET_EXPIRATION_EDIT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.DEVICES );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.MANAGE_CAMPAIGN );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.DEVICES_STATUS_REPORT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.DEVICE_SNAPSHOT_REPORT );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SVC_ASSET );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SVC_ASSETS );
		ATTR_DEFINITION_FILTER.getActions().add( ActionType.SVC_METADATA );
		
		
		MIRROR_PLAYERS_FILTER.getActions().add( ActionType.SVC_DEVICES);
		
		CAMPAIGNS_FILTER.getActions().add( ActionType.CAMPAIGNS_OVERVIEW);
		CAMPAIGNS_FILTER.getActions().add( ActionType.MANAGE_CAMPAIGN);
		
		SAVED_REPORTS_FILTER.getActions().add( ActionType.SAVED_REPORTS);
		
		ADVERTISERS_FILTER.getActions().add( ActionType.ADVERTISERS_OVERVIEW );
		ADVERTISERS_FILTER.getActions().add( ActionType.SELECT_ADVERTISER );
		
		VENUE_PARTNERS_FILTER.getActions().add( ActionType.VENUE_PARTNERS_OVERVIEW );
		VENUE_PARTNERS_FILTER.getActions().add( ActionType.SELECT_VANUE_PARTNER );
		VENUE_PARTNERS_FILTER.getActions().add( ActionType.MANAGE_VANUE_PARTNER );
		
		CONTENT_UPDATE_FILTER.getActions().add( ActionType.CONTENT_UPDATE_OVERVIEW );
				
		/*
		 * Step 3: Add the filter to the collection of filters
		 */
		INSTANCES.put( SEGMENTS_FILTER.getName(), SEGMENTS_FILTER );
		INSTANCES.put( PLAYLISTS_FILTER.getName(), PLAYLISTS_FILTER );
		INSTANCES.put( CONTENT_ROTATIONS_FILTER.getName(), CONTENT_ROTATIONS_FILTER );
		INSTANCES.put( ASSETS_FILTER.getName(), ASSETS_FILTER );
		INSTANCES.put( DEVICES_FILTER.getName(), DEVICES_FILTER );
		INSTANCES.put( LAYOUTS_FILTER.getName(), LAYOUTS_FILTER );
		INSTANCES.put( DISPLAYAREAS_FILTER.getName(), DISPLAYAREAS_FILTER );
		INSTANCES.put( SEGMENT_GRP_MEMBER_FILTER.getName(), SEGMENT_GRP_MEMBER_FILTER );
		INSTANCES.put( PLAYLIST_GRP_MEMBER_FILTER.getName(), PLAYLIST_GRP_MEMBER_FILTER );
		INSTANCES.put( CONTENT_ROTATION_GRP_MEMBER_FILTER.getName(), CONTENT_ROTATION_GRP_MEMBER_FILTER );
		INSTANCES.put( DEVICE_GRP_MEMBER_FILTER.getName(), DEVICE_GRP_MEMBER_FILTER );
		INSTANCES.put( GRP_MEMBER_FILTER.getName(), GRP_MEMBER_FILTER );
		INSTANCES.put( DIRTY_FILTER.getName(), DIRTY_FILTER );
		INSTANCES.put( PLAYBACK_EVENTS_ASSET_FILTER.getName(), PLAYBACK_EVENTS_ASSET_FILTER );
		INSTANCES.put( PLAYBACK_EVENTS_DEVICE_FILTER.getName(), PLAYBACK_EVENTS_DEVICE_FILTER );
		INSTANCES.put( CONTENT_SCHEDULE_EVENTS_ASSET_FILTER.getName(), CONTENT_SCHEDULE_EVENTS_ASSET_FILTER );
		INSTANCES.put( CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER.getName(), CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER );
		INSTANCES.put( CONTENT_SCHEDULER_STATUS_FILTER.getName(), CONTENT_SCHEDULER_STATUS_FILTER );
		INSTANCES.put( MCM_FILTER.getName(), MCM_FILTER );
		INSTANCES.put( ATTR_DEFINITION_FILTER.getName(), ATTR_DEFINITION_FILTER );
		INSTANCES.put( ASSET_INGESTER_STATUS_FILTER.getName(), ASSET_INGESTER_STATUS_FILTER );
		INSTANCES.put( DEVICE_INGESTER_STATUS_FILTER.getName(), DEVICE_INGESTER_STATUS_FILTER );
		INSTANCES.put( GRPS_FILTER.getName(), GRPS_FILTER );
		
		INSTANCES.put( SEGMENTS_FILTER_ADMIN.getName(), SEGMENTS_FILTER_ADMIN );
		INSTANCES.put( PLAYLISTS_FILTER_ADMIN.getName(), PLAYLISTS_FILTER_ADMIN );
		INSTANCES.put( CONTENT_ROTATIONS_FILTER_ADMIN.getName(), CONTENT_ROTATIONS_FILTER_ADMIN );
		INSTANCES.put( ASSETS_FILTER_ADMIN.getName(), ASSETS_FILTER_ADMIN );
		INSTANCES.put( DEVICES_FILTER_ADMIN.getName(), DEVICES_FILTER_ADMIN );
		INSTANCES.put( LAYOUTS_FILTER_ADMIN.getName(), LAYOUTS_FILTER_ADMIN );
		INSTANCES.put( DISPLAYAREAS_FILTER_ADMIN.getName(), DISPLAYAREAS_FILTER_ADMIN );
		INSTANCES.put( SEGMENT_GRP_MEMBER_FILTER_ADMIN.getName(), SEGMENT_GRP_MEMBER_FILTER_ADMIN );
		INSTANCES.put( PLAYLIST_GRP_MEMBER_FILTER_ADMIN.getName(), PLAYLIST_GRP_MEMBER_FILTER_ADMIN );
		INSTANCES.put( CONTENT_ROTATION_GRP_MEMBER_FILTER_ADMIN.getName(), CONTENT_ROTATION_GRP_MEMBER_FILTER_ADMIN );
		INSTANCES.put( DEVICE_GRP_MEMBER_FILTER_ADMIN.getName(), DEVICE_GRP_MEMBER_FILTER_ADMIN );
		INSTANCES.put( GRP_MEMBER_FILTER_ADMIN.getName(), GRP_MEMBER_FILTER_ADMIN );
		INSTANCES.put( DIRTY_FILTER_ADMIN.getName(), DIRTY_FILTER_ADMIN );
		INSTANCES.put( PLAYBACK_EVENTS_ASSET_FILTER_ADMIN.getName(), PLAYBACK_EVENTS_ASSET_FILTER_ADMIN );
		INSTANCES.put( PLAYBACK_EVENTS_DEVICE_FILTER_ADMIN.getName(), PLAYBACK_EVENTS_DEVICE_FILTER_ADMIN );
		INSTANCES.put( CONTENT_SCHEDULE_EVENTS_ASSET_FILTER_ADMIN.getName(), CONTENT_SCHEDULE_EVENTS_ASSET_FILTER_ADMIN );
		INSTANCES.put( CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER_ADMIN.getName(), CONTENT_SCHEDULE_EVENTS_DEVICE_FILTER_ADMIN );
		INSTANCES.put( MCM_FILTER_ADMIN.getName(), MCM_FILTER_ADMIN );
		INSTANCES.put( ATTR_DEFINITION_FILTER_ADMIN.getName(), ATTR_DEFINITION_FILTER_ADMIN );
		INSTANCES.put( GRPS_FILTER_ADMIN.getName(), GRPS_FILTER_ADMIN );
		INSTANCES.put( MIRROR_PLAYERS_FILTER.getName(), MIRROR_PLAYERS_FILTER );
		INSTANCES.put( MIRROR_PLAYERS_FILTER_ADMIN.getName(), MIRROR_PLAYERS_FILTER_ADMIN );
		INSTANCES.put( CAMPAIGNS_FILTER.getName(), CAMPAIGNS_FILTER);
		INSTANCES.put( CAMPAIGNS_FILTER_ADMIN.getName(), CAMPAIGNS_FILTER_ADMIN);
		INSTANCES.put( SAVED_REPORTS_FILTER.getName(), SAVED_REPORTS_FILTER);
		INSTANCES.put( SAVED_REPORTS_FILTER_ADMIN.getName(), SAVED_REPORTS_FILTER_ADMIN);
		INSTANCES.put( ADVERTISERS_FILTER.getName(), ADVERTISERS_FILTER);
		INSTANCES.put( ADVERTISERS_FILTER_ADMIN.getName(), ADVERTISERS_FILTER_ADMIN);
		INSTANCES.put( VENUE_PARTNERS_FILTER.getName(), VENUE_PARTNERS_FILTER);
		INSTANCES.put( VENUE_PARTNERS_FILTER_ADMIN.getName(), VENUE_PARTNERS_FILTER_ADMIN);
		INSTANCES.put( CONTENT_UPDATE_FILTER.getName(), CONTENT_UPDATE_FILTER);
		INSTANCES.put( CONTENT_UPDATE_FILTER_ADMIN.getName(), CONTENT_UPDATE_FILTER_ADMIN);
	}
	/**
	 * 
	 *
	 */
	public FilterType() {}
	
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	public FilterType(String name, String[] params) {
		this.name = name;
		this.params = params;
	}	

	/**
	 * Returns a list of FilterType objects that are associated with the given action 
	 * @return
	 */
	public static HashSet getFilterTypes(ActionType actionType)
	{
		// For each filter that is defined
		HashSet result = new HashSet();
		for( Iterator i=getFilterTypes().iterator(); i.hasNext(); )
		{
			FilterType filterType = (FilterType)i.next();
			for( Iterator j=filterType.getActions().iterator(); j.hasNext(); )
			{
				// If this filter has the given actionType in it's collection -- add it to the list
				ActionType at = (ActionType)j.next();
				if( at.getName().equalsIgnoreCase( actionType.getName() ) ){
					result.add( filterType );
					break;
				}
			}			
		}		
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public static List getFilterTypes()
	{
		List l = new LinkedList();
		l.addAll( FilterType.INSTANCES.values() );
		return l;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static FilterType getFilterType(String name)
	{
		return (FilterType)INSTANCES.get( name );
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

	/**
	 * @return Returns the actions.
	 */
	public Set getActions() {
		return actions;
	}
	

	/**
	 * @param actions The actions to set.
	 */
	public void setActions(Set actions) {
		this.actions = actions;
	}

	/**
	 * @return Returns the params.
	 */
	public String[] getParams() {
		return params;
	}
	

	/**
	 * @param params The params to set.
	 */
	public void setParams(String[] params) {
		this.params = params;
	}
}
