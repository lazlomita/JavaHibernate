/*
 * Created on Aug 11, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public final class Constants  
{		
	/**
	 * The session variable used to store the application name for currently logged on user
	 */
	public static String APP_NAME = "";	
	
	/**
	 * Default value of the session variable used to store the max number of child nodes the treeview should display.
	 * Optionally set using KuvataConfig in KMMServlet.setup().
	 */
	public static int MAX_CHILD_NODES = 100;		
	
	/**
	 * Default value of the session variable used to store the max number of child nodes the treeview should display.
	 * Optionally set using KuvataConfig in KMMServlet.setup().
	 */
	public static int MAX_MENU_TREEVIEW_CHILD_NODES = 1000;
	
	/**
	 * The session variable used to store the id of the currently logged on user
	 */
	public static final String USER_ID = "userId";
	
	/**
	 * The session variable used to store the string value for not applicable for places where null wouldnt work
	 */
	public static final String NOT_APPLICABLE = "N/A";
	
	/**
	 * The session variable used to store the name of the currently logged on user
	 */
	public static final String USERNAME = "username";
	
	/**
	 * The session variable used to store the schema for the currently logged on user
	 */
	public static final String SCHEMA = "schema";
	
	/**
	 * The session variable used to store the contentManagerAccess property for the currently logged on user
	 */
	public static final String CONTENT_MANAGER_ACCESS = "contentManagerAccess";
	
	/**
	 * The error message used to denote that the currently logged in user does not have access to the Sign Center 
	 */
	public static final String SIGN_CENTER_ACCESS_DENIED = "Sign Center access denied.";
	
	/**
	 * The session variable used to store the deviceId 
	 */
	public static final String DEVICE_ID = "deviceId";	
	
	/**
	 * The session variable used to store the isAdmin flag for the currently logged on user
	 */
	public static final String IS_ADMIN = "isAdmin";		
	
	/**
	 * The variable used to identify both the schemas directory and
	 * the /schemas virtual directory as defined in apache's httpd.conf
	 */
	public static final String SCHEMAS = "schemas";		
	
	/**
	 * The request parameter used to store the password to login the user
	 */
	public static final String PASSWORD = "password";	
	
	/**
	 * Represents the success outcome in an ActionForward
	 */
	public static final String SUCCESS = "success";
	
	/**
	 * Represents the failure outcome in an ActionForward
	 */
	public static final String FAILURE = "failure";	
	
	/**
	 * Represents the next outcome in an ActionForward
	 */
	public static final String NEXT = "next";
	
	/**
	 * Represents the back outcome in an ActionForward
	 */
	public static final String BACK = "back";	
	
	/**
	 * Represents the finish outcome in an ActionForward
	 */
	public static final String FINISH = "finish";
	
	/**
	 * Represents the refresh outcome in an ActionForward
	 */
	public static final String REFRESH = "refresh";	
	
	/**
	 * Represents the error outcome in an ActionForward
	 */
	public static final String ERROR = "error";	
	
	/**
	 * Represents the page1 outcome in an ActionForward
	 */
	public static final String PAGE1 = "page1";	
	
	/**
	 * Represents the page2 outcome in an ActionForward
	 */
	public static final String PAGE2 = "page2";		
	
	/**
	 * Represents the page3 outcome in an ActionForward
	 */
	public static final String PAGE3 = "page3";		
	
	/**
	 * Represents the page4 outcome in an ActionForward
	 */
	public static final String PAGE4 = "page4";		
	
	/**
	 * Represents the page5 outcome in an ActionForward
	 */
	public static final String PAGE5 = "page5";		

	/**
	 * Token to denote the closing of a wizard
	 */
	public static final String CLOSE = "close";	
	
	/**
	 * Represents the save outcome in an ActionForward
	 */
	public static final String SAVE = "save";	

	/**
	 * Represents the cancel outcome in an ActionForward
	 */
	public static final String CANCEL = "cancel";		
	
	/**
	 * Represents the test outcome in an ActionForward
	 */
	public static final String TEST = "test";	
	
	/**
	 * Represents the remove outcome in an ActionForward
	 */
	public static final String REMOVE = "remove";
	
	/**
	 * Represents the remove outcome in an ActionForward
	 */
	public static final String REFRESH_LEFT_PANEL = "refreshLeftPanel";
	
	/**
	 * Represents the nextPage action to perform
	 */
	public static final String NEXT_PAGE = "nextPage";	
	
	/**
	 * Represents the previousPage action to perform
	 */
	public static final String PREVIOUS_PAGE = "previousPage";		
	
	/**
	 * Represents the update action to perform
	 */
	public static final String UPDATE = "update";
	
	/**
	 * Represents the redirect action to perform
	 */
	public static final String REDIRECT = "redirect";	
	
	/**
	 * Represents the create action to perform
	 */
	public static final String CREATE = "create";	
	
	/**
	 * Represents the login outcome in an ActionForward
	 */
	public static final String LOGIN = "login";		
	
	/**
	 * Token used to pass true value back from Action
	 */
	public static final String TRUE = "true";
	
	/**
	 * Token used to pass false value back from Action
	 */
	public static final String FALSE = "false";	

	/**
	 * Token used to access base KMF schema
	 */
	public static final String BASE_SCHEMA = "kmf";

	/**
	 * Token used to identify Device Groups 
	 */
	public static final String DEVICE_GROUPS = "Device Groups";

	/**
	 * Token used to identify Devices 
	 */
	public static final String DEVICES = "Devices";	

	/**
	 * Token used to identify Playlists 
	 */
	public static final String PLAYLISTS = "Playlists";
	
	/**
	 * Token used to identify Content Rotations 
	 */
	public static final String CONTENT_ROTATIONS = "ContentRotations";
	
	/**
	 * Token used to identify Playlist Groups
	 */
	public static final String PLAYLIST_GROUPS = "Playlist Groups";	
	
	/**
	 * Token used to identify SEGMENTS 
	 */
	public static final String SEGMENTS = "Segments";		
	
	/**
	 * Token used to identify Scheduling Groups
	 */
	public static final String SCHEDULING_GROUPS = "Scheduling Groups";		
	
	/**
	 * Token used to identify Segment Parts 
	 */
	public static final String SEGMENT_PARTS = "Segment Parts";	

	/**
	 * Token used to identify Displayareas 
	 */
	public static final String DISPLAYAREAS = "Displayareas";
	/**
	 * Token used to identify Asset Types 
	 */
	public static final String ASSET_TYPES = "Asset Types";	
	
	/**
	 * Token used to identify Asset Type 
	 */
	public static final String ASSET_TYPE = "Asset Type";		
	
	/**
	 * Token used to identify Asset Types 
	 */
	public static final String SCHEDULED_DEVICES = "Scheduled Devices";	
	
	/**
	 * Token used to identify Schedule Groups 
	 */
	public static final String SEGMENT_GROUPS = "Schedule Groups";	
	
	/**
	 * Token used to identify Content Rotation Groups 
	 */
	public static final String CONTENT_ROTATION_GROUPS = "Content Rotation Groups";		
	
	/**
	 * Token used to identify request attribute named "id"
	 */
	public static final String ID = "id";		
	
	/**
	 * Token used to identify request attribute named "searchCriteria"
	 */
	public static final String SEARCH_CRITERIA = "searchCriteria";
	
	/**
	 * Token used to identify request attribute named "treeviewRefreshRequired"
	 */
	public static final String TREEVIEW_REFRESH_REQUIRED = "treeviewRefreshRequired";		
		
	/**
	 * Token used as a common string delimiter 
	 */
	public static final String DELIMITER = "|";	
	
	/**
	 * Token used as a common string delimiter 
	 */
	public static final String UNIQUE_DELIMITER = "~~";
	
	/**
	 * Value to indicate debug logging
	 */
	public static final int DEBUG = 1;
	
	/**
	 * Value to indicate debug normal
	 */
	public static final int NORMAL = 0;
	
	/**
	 * Token used to identify the maximum number of result to return in a datagrid
	 */
	public static final int MAX_RESULTS = 10;	
	
	/**
	 * Used to identify a segment part of type playlist
	 */
	public static final String PLAYLIST_SUFFIX = "_playlist";
	
	/**
	 * Used to identify a dynamic content part of type content rotation
	 */
	public static final String CONTENT_ROTATION_SUFFIX = "_content_rotation";

	/**
	 * Used to identify a segment part of type asset
	 */
	public static final String ASSET_SUFFIX = "_asset";
	
	/**
	 * Used to identify a device group within an add/remove box
	 */
	public static final String DEVICE_GROUP_SUFFIX = "_device_group";
	
	/**
	 * Used to identify an attr definition within an add/remove box
	 */
	public static final String ATTR_DEFINITION_SUFFIX = "_attr_definition";	
	
	/**
	 * Used to identify a playlist group within an add/remove box
	 */
	public static final String PLAYLIST_GROUP_SUFFIX = "_playlist_group";
	
	/**
	 * Used to identify a segment group within an add/remove box
	 */
	public static final String SEGMENT_GROUP_SUFFIX = "_segment_group";	
	
	/**
	 * Used to identify a content rotation group within an add/remove box
	 */
	public static final String CONTENT_ROTATION_GROUP_SUFFIX = "_content_rotation_group";		
	
	/**
	 * Used to identify a playlist group within the treeview control
	 */
	public static final String TYPE_PLAYLIST_GROUP = "type_playlist_group";		
	
	/**
	 * Used to identify a device group within the treeview control
	 */
	public static final String TYPE_DEVICE_GROUP = "type_device_group";
	
	/**
	 * Token used to identify the folder in which thumbnails reside (relative to the assets directory)
	 */
	public static final String THUMBS_DIR = "thumbs";	
	
	/**
	 * Token used to identify the alias to the assets folder as defined by apache
	 */
	public static final String ASSETS_DIR = "assets";		
	
	/**
	 * Token used to identify the alias to the conf folder
	 */
	public static final String CONF_DIR = "conf";	
	
	/**
	 * Token used to identify the alias to the presentation folder
	 */
	public static final String PRESENTATIONS = "presentations";			
	
	/**
	 * Token used to identify the alias to the schedules folder
	 */
	public static final String SCHEDULES = "schedules";	
	
	/**
	 * Token used to identify the alias to the schedules folder
	 */
	public static final String CONTENT_UPDATES = "content_updates";			
	
	/**
	 * Token used to identify which frame to create a thumbnail from video assets
	 */
	public static final long FRAME_TO_CAPTURE = 100;			
	
	/**
	 * Used to identify the title parameter passed in the request
	 */
	public static final String TITLE = "title";			
	
	/**
	 * Used to determine the type or search to be performed
	 */
	public static final String SEARCH_ASSET_TYPE = "assetType";
	
	/**
	 * Used to determine the type or search to be performed
	 */
	public static final String SEARCH_ASSET_NAME = "assetName";

	/**
	 * Date format used to create the filename of a Content Schedule 
	 */
	public static final String OUTPUT_DATE_FORMAT = "yyyy-MM-dd-HH.mm.ss";
	
	/**
	 * Package of the ReferencedFile class 
	 */
	public static final String REFERENCED_FILE_CLASS = "com.kuvata.kmf.presentation.ReferencedFile";
	
	/*
	 * Used to denote a checked item on a form
	 */
	public static final String ON = "on";
	
	/*
	 * Used to denote an unchecked item on a form
	 */
	public static final String OFF = "off";	
	
	/**
	 * Token used to identify Users 
	 */
	public static final String USERS = "Users";		
	
	/**
	 * Token used to identify the name of the itemsPerPage cookie
	 */
	public static final String COOKIE_SELECTED_ITEMS_PER_PAGE = "selectedItemsPerPage";	
	
	/**
	 * Token used to identify the name of the layoutPreviewMagnification cookie
	 */
	public static final String COOKIE_LAYOUT_PREVIEW_MAGNIFICATION = "layoutPreviewMagnification";		
	
	/**
	 * Token used to identify the default age of a cookie (30 days)
	 */
	public static final int COOKIE_MAX_AGE = 86400 * 30;		
	
	/**
	 * Date/Time format used to for display purposes 
	 */
	public static final String DATE_TIME_FORMAT_DISPLAYABLE = "MM/dd/yyyy hh:mm:ss a";
	
	/**
	 * Date/Time format with timezone used to for display purposes 
	 */
	public static final String DATE_FORMAT_TIMEZONE = "MM/dd/yyyy hh:mm:ss a zzz";	
	
	/**
	 * Date format used to for display purposes 
	 */
	public static final String DATE_FORMAT_DISPLAYABLE = "MM/dd/yyyy";		
	
	/**
	 * Token used to identify the alias to the ingester folder
	 */
	public static final String INGESTER_DIR = "ingester";	
	
	/**
	 * Token used to identify the alias to the playlist folder
	 */
	public static final String PLAYLISTS_DIR = "playlists";	
	
	/**
	 * Token used to identify the alias to the playlist folder
	 */
	public static final String PLAYLIST_CSV_DIR = "playlist_csv";		
	
	/*
	 * Token used to identify the display named "custom"
	 */
	public static final String CUSTOM_DISPLAY_NAME = "(custom)";
	
	/*
	 * Token used in the Asset Ingester to identify metadata fields
	 */
	public static final String METADATA_PREFIX = "METADATA:";		
	
	/*
	 * Token used in the identify a "null" string
	 */
	public static final String NULL = "null";		
	
	/*
	 * Token used in the identify the "All Asset Types" select box option
	 */
	public static final String ALL_ASSET_TYPES = "All Asset Types";

	/*
	 * Token used in the identify the "Ad Server" select box option
	 */
	public static final String ASSET_TYPE_AD_SERVER = "AdServer";

	/*
	 * Token used in the identify the "Audio" select box option
	 */
	public static final String ASSET_TYPE_AUDIO = "Audio";

	/*
	 * Token used in the identify the "Auxiliary Input" select box option
	 */
	public static final String ASSET_TYPE_AUXILIARY_INPUT = "AuxInput";

	/*
	 * Token used in the identify the "Device Volume" select box option
	 */
	public static final String ASSET_TYPE_DEVICE_VOLUME = "DeviceVolume";

	/*
	 * Token used in the identify the "Flash Animated" select box option
	 */
	public static final String ASSET_TYPE_FLASH = "Flash";

	/*
	 * Token used in the identify the "HTML" select box option
	 */
	public static final String ASSET_TYPE_HTML = "Html";

	/*
	 * Token used in the identify the "Image" select box option
	 */
	public static final String ASSET_TYPE_IMAGE = "Image";

	/*
	 * Token used in the identify the "Live Video" select box option
	 */
	public static final String ASSET_TYPE_LIVE_VIDEO = "StreamingClient";

	/*
	 * Token used in the identify the "Power Off" select box option
	 */
	public static final String ASSET_TYPE_POWER_OFF = "PowerOff";

	/*
	 * Token used in the identify the "Targeted Asset" select box option
	 */
	public static final String ASSET_TYPE_TARGETED_ASSET = "TargetedAsset";	

	/*
	 * Token used in the identify the "Ticker" select box option
	 */
	public static final String ASSET_TYPE_TICKER = "Ticker";

	/*
	 * Token used in the identify the "URL" select box option
	 */
	public static final String ASSET_TYPE_URL = "Url";

	/*
	 * Token used in the identify the "Video" select box option
	 */
	public static final String ASSET_TYPE_VIDEO = "Video";

	/*
	 * Token used in the identify the "Web Application" select box option
	 */
	public static final String ASSET_TYPE_WEB_APPLICATION = "Webapp";	
	
	/*
	 * Token used in the identify the "No Metadata" search option
	 */
	public static final String NO_METADATA = "(Select field)";	
	
	/*
	 * Token used in the identify the "Date Modified" search option
	 */
	public static final String DATE_MODIFIED = "Date Modified";
	
	/*
	 * Token used in the identify the "Auto Update" search option
	 */
	public static final String AUTO_UPDATE = "Auto Update";
	
	/*
	 * Token used in the identify the "Width" property
	 */
	public static final String WIDTH = "width";
	
	/*
	 * Token used in the identify the "Height" property
	 */
	public static final String HEIGHT = "height";	
	
	/*
	 * Token used in the identify the playback_events.xml file
	 */
	public static final String PLAYBACK_EVENTS_FILENAME = "playback_events";		
	
	/*
	 * Date format used within the content schedule
	 */	
	public static final String SCHEDULE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
	
	/*
	 * Date format used within the content schedule with timezone
	 */	
	public static final String SCHEDULE_FORMAT_TIMEZONE = "yyyy/MM/dd HH:mm:ss.SSS z";
	
	/*
	 * Date format used in the heartbeat upload xml
	 */	
	public static final String HEARTBEAT_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS Z";
	
	/*
	 * Token used to identify the "All Devices" select box option
	 */
	public static final String ALL_DEVICES = "All Devices";
	
	/*
	 * Default table name used to generate reports
	 */
	public static final String REPORTS_SOURCE_TABLE_DEFAULT = "PlaybackEvent";
	
	/*
	 * Table name used to generate reports
	 */
	public static final String REPORTS_SOURCE_TABLE_SUMMARY = "PlaybackEventSummary";
	
	/*
	 * Table name used to generate reports
	 */
	public static final String REPORTS_SOURCE_TABLE_CONTENT_SCHEDULE_EVENT = "ContentScheduleEvent";	
	
	/*
	 * Token used to identify the name of the reportsSourceTable property
	 */
	public static final String REPORTS_SOURCE_TABLE = "Application.reportsSourceTable";
	
	/*
	 * Token used to identify the name of the contractFile property
	 */
	public static final String CONTRACT_FILE = "Application.contractFile";
	
	/*
	 * Token used to identify the form_name attribute to be stored in the request object
	 */
	public static final String FORM_NAME = "formName";	
	
	/*
	 * Token used to identify the Default entry on a form
	 */
	public static final String DEFAULT = "Default";
	
	/*
	 * Token used to identify the Actual entry on a form
	 */
	public static final String ACTUAL = "Actual";	
	
	/*
	 * Token used to identify the alias to the client_uploads folder
	 */
	public static final String CLIENT_UPLOADS_DIR = "client_uploads";	
	
	/*
	 * Token used in the identify the "None" option
	 */
	public static final String NONE = "(None)";
	
	/*
	 * Token used in the identify the "Unnamed" option
	 */
	public static final String UNNAMED = "(Unnamed)";	
	
	/*
	 * Token used to identify the name of the enablePermissions property
	 */
	public static final String ENABLE_PERMISSIONS = "Application.enablePermissions";
	
	/*
	 * Token used to identify the default font size of a ticker (in pixels)
	 */
	public static final String TICKER_FONT_SIZE_DEFAULT = "24";	
	
	/*
	 * Token used to identify the default color of a ticker (black)
	 */
	public static final String TICKER_COLOR_DEFAULT = "#000000";
	
	/*
	 * Token used to identify the default background color of a ticker (white)
	 */
	public static final String TICKER_BACKGROUND_COLOR_DEFAULT = "#FFFFFF";
	
	/*
	 * Token used to identify the text in case of RSS
	 */
	public static final String TICKER_RSS_TEXT = "{RSS feed}";
	
	/*
	 * Token used to convert boolean "true" to "Yes"
	 */
	public static final String YES = "Yes";
	
	/*
	 * Token used to convert boolean "false" to "No"
	 */
	public static final String NO = "No";

	/*
	 * Token used to convert boolean "true" to "enabled"
	 */
	public static final String ENABLED = "enabled";
	
	/*
	 * Token used to convert boolean "false" to "disabled"
	 */
	public static final String DISBLED = "disabled";
	
	/*
	 * Token used to identify "Up"
	 */
	public static final String UP = "Up";
	
	/*
	 * Token used to identify "Down"
	 */
	public static final String DOWN = "Down";	
	
	/*
	 * Token used to identify a named cacheIdManager for devices 
	 */
	public static final String ENTITY_PAGE_NUM_MANAGER = "entityPageNumManager";
	
	/*
	 * Token used to identify a named cacheIdManager for devices 
	 */
	public static final String DEVICE_ID_MANAGER = "deviceIdManager";
	
	/*
	 * Token used to identify a named cacheIdManager for devices 
	 */
	public static final String DEVICE_PAGE_NUM_MANAGER = "devicePageNumManager";
	
	/*
	 * Token used to identify a named cacheIdManager for playlists 
	 */
	public static final String PLAYLIST_ID_MANAGER = "playlistIdManager";
	
	/*
	 * Token used to identify a named cacheIdManager for playlists 
	 */
	public static final String PLAYLIST_PAGE_NUM_MANAGER = "playlistPageNumManager";
	
	/*
	 * Token used to identify a named cacheIdManager for assets
	 */
	public static final String ASSET_ID_MANAGER = "assetIdManager";
	
	/*
	 * Token used to identify a named cachePageManager for contentRotations
	 */
	public static final String ASSET_PAGE_NUM_MANAGER = "assetPageNumManager";
	
	/*
	 * Token used to identify a named cacheIdManager for contentRotations
	 */
	public static final String CONTENT_ROTATION_ID_MANAGER = "contentRotationIdManager";	
	
	/*
	 * Token used to identify a named cachePageManager for contentRotations
	 */
	public static final String CONTENT_ROTATION_PAGE_NUM_MANAGER = "contentRotationPageNumManager";	
	
	/*
	 * Default length of timeout when pushing device commands or retrieving assets
	 */
	public static final int CONNECTION_TIMEOUT_SECONDS = 5;	
	
	/*
	 * Default length of timeout when making web service requests
	 */
	public static final int HTTP_CONNECTION_TIMEOUT_SECONDS = 60;
	
	/*
	 * Token used to identify the numRoles session variable
	 */
	public static final String NUM_ROLES = "numRoles";	
	
	/*
	 * Format string used with inserting timestamps into oracle
	 */
	public static final String ORACLE_DATE_FORMAT_STRING = "YYYY/MM/DD HH24:MI:SS";		
	
	/*
	 * Token used as a placeholder for the playLength when creating an asset whose length will be calculated asynchronously
	 */
	public static final String PLAY_LENGTH_PLACEHOLDER = "15.001";
	
	/*
	 * Token used as a placeholder for the intrinsic length when creating an asset whose length will be calculated asynchronously
	 */
	public static final String INTRINSIC_LENGTH_PLACEHOLDER = "-1";	
	
	/*
	 * Token used to represent the number of bytes in a gigabyte.
	 */
	public static final long  GIGABYTE = 1024L * 1024L * 1024L;
	
	/*
	 * Token used to represent the number of bytes in a megabyte.
	 */
	public static final long  MEGABYTE = 1024L * 1024L;
	
	/*
	 * Token used to represent the number of bytes in a kilobyte.
	 */
	public static final long  KILOBYTE = 1024L;	
	
	/*
	 * Token used to the represent the default additional content amount (in seconds) for a device
	 */
	public static final String ADDITIONAL_CONTENT_AMOUNT_DEFAULT = "86400";
	
	/*
	 * Token used to the represent the default additional content amount (in seconds) for a device
	 */
	public static final String SCHEDULING_HORIZON_DEFAULT = "3600";	
	
	/*
	 * Token used to the identify the Application.autoRegister property
	 */
	public static final String AUTO_REGISTER_PROPERTY = "Application.autoRegister";
	
	/*
	 * Token used to the identify the Application.defaultSchema property
	 */
	public static final String DEFAULT_SCHEMA_PROPERTY = "Application.defaultSchema";
	
	/*
	 * Token used to identify the Application.openVPNClientPath property
	 */
	public static final String OPEN_VPN_CLIENT_PATH = "Application.openVPNClientPath";
	
	/*
	 * Token used to identify the Application.openVPNServerPath property
	 */
	public static final String OPEN_VPN_SERVER_PATH = "Application.openVPNServerPath";	
	
	/*
	 * Token used to the identify the screenshots directory
	 */
	public static final String SCREENSHOTS_DIRECTORY = "screenshots";		
	
	/*
	 * Date Time format used for multicast tests
	 */
	public static final SimpleDateFormat MULTICAST_DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	/*
	 * String used in the multicast test file
	 */
	public static final String MULTICAST_TEST = "multicast_test";
	
	/*
	 * String used to determine whether to enable/disable the volume updater
	 */
	public static final String ENABLE_VOLUME_UPDATER = "Application.enableVolumeUpdater";
	
	/*
	 * Token used to the identify the time format of the time-based properties 
	 */
	public static final String TIME_FORMAT = "h:mm a";
	public static final String EXTENDED_TIME_FORMAT = "h:mm:ss a";
	public static final String HOUR_FORMAT = "h";
	public static final String MINUTE_FORMAT = "mm";
	public static final String AMPM_FORMAT = "a";		
	
	/*
	 * Token used to the identify the basic search typ 
	 */
	public static final String SEARCH_TYPE_BASIC = "basic";
	
	/*
	 * Token used to the identify the advanced search type
	 */
	public static final String SEARCH_TYPE_ADVANCED = "advanced";
	
	/*
	 * Token used to label the variable_length=true condition
	 */
	public static final String VARIABLE_LENGTH_TRUE = "Variable as specified by selected asset";
	
	/*
	 * Token used to label the variable_length=false condition
	 */
	public static final String VARIABLE_LENGTH_FALSE = "Fixed length";	
	
	/*
	 * Token used to identify the event_dt parameter when creating the LOG_EVENT server command
	 */
	public static final String EVENT_DT = "event_dt";	
	
	/*
	 * Token used to identify the event_type parameter when creating the LOG_EVENT server command
	 */
	public static final String EVENT_TYPE = "event_type";

	/*
	 * Token used to identify the event_type parameter when creating the LOG_EVENT server command
	 */
	public static final String EVENT_DETAILS = "event_details";

	/*
	 * Represents the FAILED outcome of the smartctl process
	 */
	public static final String FAILED = "FAILED";
	
	// Token used to denote whether to show the Now Playing asset on the lcd or not
	public static final String SHOW_CURRENT_PLAYING_ASSET = "showNowPlayingAsset";
	
	/*
	 * Token used to determine save in the session whether or not to display the variable length section 
	 */
	public static final String DISPLAY_VARIABLE_LENGTH = "displayVariableLength";
	
	/*
	 * Token used to determine save in the session whether or not to display the variable length section 
	 */
	public static final String SELECTED_VARIABLE_LENGTH_ASSETS = "selectedVariableLengthAssets";
	
	/*
	 * Token used to limit the number of server commands that can be processed at a time
	 */
	public static final int SERVER_COMMAND_MAX_RESULTS = 100;
	
	/*
	 * Token used to determine the maximum number of expressions in a list (oracle constraint)
	 */
	public static final int MAX_NUMBER_EXPRESSIONS_IN_LIST = 1000;
	
	/*
	 * Token used to identify an OpenVPN user that does not provide authentication credentials
	 */
	public static final String NO_LOGIN = "nologin";
	
	/*
	 * Token used to get milliseconds in a day
	 */ 
	public static final long MILLISECONDS_IN_A_DAY = 86400000;
	
	/*
	 * Token used to get milliseconds in 15 mins
	 */ 
	public static final long MILLISECONDS_IN_15_MINS = 900000;
	
	/*
	 * Token used to identity the kuvata property for heartbeat timeout
	 */
	public static final String ALERT_HEARTBEAT_TIMEOUT = "Alert.heartbeatTimeout";
	
	/*
	 * Token used for default heartbeat timeout
	 */
	public static final String DEFAULT_HEARTBEAT_TIMEOUT = "15";	// in minutes
	
	/*
	 * Token used for the asset name of a no-content event
	 */
	public static final String NO_CONTENT_EVENT_ASSET_NAME = "noContentEvent";	// in minutes
	
	/*
	 * Token used for default control
	 */
	public static final String CONTROL_DEFAULT = "Park Media";
	
	/*
	 * Token used for Android identification
	 */
	public static final String ANDROID = "Android";
	
	/*
	 * Token used for Arch identification
	 */
	public static final String ARCH = "ARCH";
	
	/*
	 * Initialization message for Asset Ingester
	 */
	public static String ASSET_INGESTER_INITIALIZING_MESSAGE = "Initializing Asset Ingester. Please wait...";

	public static final String STATIC = "static";
	public static final String DYNAMIC = "dynamic";
	public static final String SERVER_COMMANDS_ELEMENT = "server_commands";
	public static final String SERVER_COMMAND_ELEMENT = "server_command";
	public static final String DEVICE_SERVER_COMMAND_ID_ATTRIBUTE = "device_server_command_id";
	public static final String SERVER_COMMAND_ID = "serverCommandId";
	public static final String COMMAND_ATTRIBUTE = "command";
	public static final String PARAMETERS_ATTRIBUTE = "parameters";	
	public static final String SERVER_COMMANDS_STATUS_ELEMENT = "server_commands_status";
	public static final String SERVER_COMMAND_STATUS_ELEMENT = "server_command_status";
	public static final String STATUS = "status";
	public static final String SUB_STATUS = "subStatus";
	public static final String DEVICE_COMMAND_ID = "deviceCommandId";
	public static final String FILENAME = "filename";
	public static final String INVALID_SESSION = "Invalid Session";
	
	/*
	 * Custom alert metadata fields
	 */
	public static final String HEARTBEAT_ALERT_TEXT = "Heartbeat Alert Text";
	public static final String HEARTBEAT_ALERT_TO_EMAIL = "Heartbeat Alert To Email";
	public static final String HEARTBEAT_ALERT_FROM_EMAIL = "Heartbeat Alert From Email";
	public static final String HEARTBEAT_ALERT_FROM_NAME = "Heartbeat Alert From Name";

	
}
