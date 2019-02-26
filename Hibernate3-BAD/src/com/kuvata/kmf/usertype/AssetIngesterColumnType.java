package com.kuvata.kmf.usertype;

import java.util.HashMap;
import java.util.Map;

import com.kuvata.kmf.usertype.PersistentStringEnum;

public class AssetIngesterColumnType extends PersistentStringEnum 
{	
	public static final AssetIngesterColumnType ASSET_TYPE = new AssetIngesterColumnType("asset_type", "asset_type");
	public static final AssetIngesterColumnType NAME = new AssetIngesterColumnType("name", "name");
	public static final AssetIngesterColumnType ASSET = new AssetIngesterColumnType("asset", "asset");
	public static final AssetIngesterColumnType HEIGHT = new AssetIngesterColumnType("height", "height");
	public static final AssetIngesterColumnType WIDTH = new AssetIngesterColumnType("width", "width");
	public static final AssetIngesterColumnType DEFAULT_LAYOUT = new AssetIngesterColumnType("default_layout", "default_layout");
	public static final AssetIngesterColumnType DEFAULT_DISPLAYAREA = new AssetIngesterColumnType("default_displayarea", "default_displayarea");
	public static final AssetIngesterColumnType DEFAULT_LENGTH = new AssetIngesterColumnType("default_length", "default_length");
	public static final AssetIngesterColumnType START_PAGE = new AssetIngesterColumnType("start_page", "start_page");	
	public static final AssetIngesterColumnType AUTH_USERNAME = new AssetIngesterColumnType("auth_username", "auth_username");
	public static final AssetIngesterColumnType AUTH_PASSWORD = new AssetIngesterColumnType("auth_password", "auth_password");
	public static final AssetIngesterColumnType FIT_TO_SIZE = new AssetIngesterColumnType("fit_to_display_area", "fit_to_display_area");
	public static final AssetIngesterColumnType TRANSPARENT_BACKGROUND = new AssetIngesterColumnType("transparent_background", "transparent_background");
	public static final AssetIngesterColumnType PAIRED_NAME = new AssetIngesterColumnType("paired_name", "paired_name");
	public static final AssetIngesterColumnType PAIRED_ASSET_TYPE = new AssetIngesterColumnType("paired_asset_type", "paired_asset_type");
	public static final AssetIngesterColumnType PAIRED_DISPLAYAREA = new AssetIngesterColumnType("paired_displayarea", "paired_displayarea");
	public static final AssetIngesterColumnType PAIRED_LENGTH = new AssetIngesterColumnType("paired_length", "paired_length");
	public static final AssetIngesterColumnType PLAY_ASSET_AUDIO = new AssetIngesterColumnType("play_asset_audio", "play_asset_audio");
	public static final AssetIngesterColumnType MEMBER_NAME = new AssetIngesterColumnType("member_name", "member_name");
	public static final AssetIngesterColumnType MEMBER_ASSET_TYPE = new AssetIngesterColumnType("member_asset_type", "member_asset_type");
	public static final AssetIngesterColumnType MEMBER_DEVICES = new AssetIngesterColumnType("member_devices", "member_devices");
	public static final AssetIngesterColumnType DYNAMIC_ASSET_LENGTH = new AssetIngesterColumnType("dynamic_asset_length", "dynamic_asset_length");
	public static final AssetIngesterColumnType NO_TARGETING_RULE = new AssetIngesterColumnType("no_targeting_rule", "no_targeting_rule");
	public static final AssetIngesterColumnType MULTIPLE_TARGETING_RULE = new AssetIngesterColumnType("multiple_targeting_rule", "multiple_targeting_rule");
	public static final AssetIngesterColumnType PAIRING_RULE = new AssetIngesterColumnType("pairing_rule", "pairing_rule");
	public static final AssetIngesterColumnType ROLES = new AssetIngesterColumnType("roles", "roles");
	public static final AssetIngesterColumnType START_DATE = new AssetIngesterColumnType("start_date", "start_date");
	public static final AssetIngesterColumnType END_DATE = new AssetIngesterColumnType("end_date", "end_date");
	public static final AssetIngesterColumnType DEFAULT_ASSET = new AssetIngesterColumnType("default_asset", "default_asset");
	public static final AssetIngesterColumnType DEFAULT_ASSET_TYPE = new AssetIngesterColumnType("default_asset_type", "default_asset_type");
	public static final AssetIngesterColumnType DUPLICATE_ASSETS = new AssetIngesterColumnType("duplicate_assets", "duplicate_assets");
	public static final AssetIngesterColumnType CAMPAIGN = new AssetIngesterColumnType("campaign", "campaign");
	public static final AssetIngesterColumnType ANAMORPHIC_WIDESCREEN = new AssetIngesterColumnType("anamorphic_widescreen", "anamorphic_widescreen");
	public static final AssetIngesterColumnType NEW_NAME = new AssetIngesterColumnType("new_name", "new_name");
	public static final AssetIngesterColumnType REMOVE_ASSET = new AssetIngesterColumnType("remove_asset", "remove_asset");
	public static final AssetIngesterColumnType OPACITY = new AssetIngesterColumnType("opacity", "opacity");
	public static final AssetIngesterColumnType DISPLAY_SUBTITLES = new AssetIngesterColumnType("display_subtitles", "display_subtitles");
	public static final AssetIngesterColumnType FRAMESYNC = new AssetIngesterColumnType("framesync", "framesync");
	public static final AssetIngesterColumnType HTML5_HARDWARE_ACCELERATION = new AssetIngesterColumnType("html5_hardware_acceleration", "html5_hardware_acceleration");
	public static final Map INSTANCES = new HashMap();
	/**
	 * 
	 */	    
	static
	{
		INSTANCES.put(ASSET_TYPE.toString(), ASSET_TYPE);
		INSTANCES.put(NAME.toString(), NAME);
		INSTANCES.put(ASSET.toString(), ASSET);
		INSTANCES.put(HEIGHT.toString(), HEIGHT);
		INSTANCES.put(WIDTH.toString(), WIDTH);
		INSTANCES.put(DEFAULT_LAYOUT.toString(), DEFAULT_LAYOUT);
		INSTANCES.put(DEFAULT_DISPLAYAREA.toString(), DEFAULT_DISPLAYAREA);
		INSTANCES.put(DEFAULT_LENGTH.toString(), DEFAULT_LENGTH);
		INSTANCES.put(START_PAGE.toString(), START_PAGE);
		INSTANCES.put(AUTH_USERNAME.toString(), AUTH_USERNAME);
		INSTANCES.put(AUTH_PASSWORD.toString(), AUTH_PASSWORD);
		INSTANCES.put(FIT_TO_SIZE.toString(), FIT_TO_SIZE);
		INSTANCES.put(TRANSPARENT_BACKGROUND.toString(), TRANSPARENT_BACKGROUND);
		INSTANCES.put(PAIRED_NAME.toString(), PAIRED_NAME);
		INSTANCES.put(PAIRED_ASSET_TYPE.toString(), PAIRED_ASSET_TYPE);
		INSTANCES.put(PAIRED_DISPLAYAREA.toString(), PAIRED_DISPLAYAREA);
		INSTANCES.put(PAIRED_LENGTH.toString(), PAIRED_LENGTH);
		INSTANCES.put(PLAY_ASSET_AUDIO.toString(), PLAY_ASSET_AUDIO);
		INSTANCES.put(MEMBER_NAME.toString(), MEMBER_NAME);
		INSTANCES.put(MEMBER_ASSET_TYPE.toString(), MEMBER_ASSET_TYPE);
		INSTANCES.put(MEMBER_DEVICES.toString(), MEMBER_DEVICES);
		INSTANCES.put(DYNAMIC_ASSET_LENGTH.toString(), DYNAMIC_ASSET_LENGTH);
		INSTANCES.put(NO_TARGETING_RULE.toString(), NO_TARGETING_RULE);
		INSTANCES.put(MULTIPLE_TARGETING_RULE.toString(), MULTIPLE_TARGETING_RULE);
		INSTANCES.put(ROLES.toString(), ROLES);
		INSTANCES.put(PAIRING_RULE.toString(), PAIRING_RULE);
		INSTANCES.put(START_DATE.toString(), START_DATE);
		INSTANCES.put(END_DATE.toString(), END_DATE);
		INSTANCES.put(DEFAULT_ASSET.toString(), DEFAULT_ASSET);
		INSTANCES.put(DEFAULT_ASSET_TYPE.toString(), DEFAULT_ASSET_TYPE);
		INSTANCES.put(DUPLICATE_ASSETS.toString(), DUPLICATE_ASSETS);
		INSTANCES.put(CAMPAIGN.toString(), CAMPAIGN);
		INSTANCES.put(ANAMORPHIC_WIDESCREEN.toString(), ANAMORPHIC_WIDESCREEN);
		INSTANCES.put(NEW_NAME.toString(), NEW_NAME);
		INSTANCES.put(REMOVE_ASSET.toString(), REMOVE_ASSET);
		INSTANCES.put(OPACITY.toString(), OPACITY);
		INSTANCES.put(DISPLAY_SUBTITLES.toString(), DISPLAY_SUBTITLES);
		INSTANCES.put(FRAMESYNC.toString(), FRAMESYNC);
		INSTANCES.put(HTML5_HARDWARE_ACCELERATION.toString(), HTML5_HARDWARE_ACCELERATION);
	}
	/**
	 * 
	 *
	 */
	public AssetIngesterColumnType() {}
	/**
	 * 
	 * @param name
	 * @param persistentValue
	 */
	protected AssetIngesterColumnType(String name, String persistentValue) {
		super(name, persistentValue);
	}
	/**
	 * 
	 */
	public String toString()
	{
		return this.name;
	}
	/**
	 * 
	 * @return
	 */
	public String getAssetIngesterColumnTypeName()
	{
		return this.name;
	}
	/**
	 * 
	 * @param dirtyTypeName
	 * @return
	 */
	public static AssetIngesterColumnType getAssetIngesterColumnType(String assetIngesterColumnName)
	{
		return (AssetIngesterColumnType) INSTANCES.get( assetIngesterColumnName );
	}	
}
