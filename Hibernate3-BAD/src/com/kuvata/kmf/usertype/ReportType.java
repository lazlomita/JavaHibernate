package com.kuvata.kmf.usertype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.kuvata.kmf.reports.AdvertiserBillingReport;
import com.kuvata.kmf.reports.AssetAiringReport;
import com.kuvata.kmf.reports.AssetSummaryReport;
import com.kuvata.kmf.reports.BillableDeviceReport;
import com.kuvata.kmf.reports.DailyCampaignReport;
import com.kuvata.kmf.reports.DevicePlaybackReport;
import com.kuvata.kmf.reports.RevenueSharingReport;
import com.kuvata.kmf.usertype.PersistentStringEnum;

public class ReportType extends PersistentStringEnum{
	public static final ReportType ASSET_AIRING = new ReportType("Asset Airing", "Asset Airing", AssetAiringReport.class);	
	public static final ReportType ASSET_SUMMARY = new ReportType("Asset Summary", "Asset Summary", AssetSummaryReport.class);
	public static final ReportType DAILY_CAMPAIGN = new ReportType("Daily Campaign", "Daily Campaign", DailyCampaignReport.class);
	public static final ReportType DEVICE_PLAYBACK = new ReportType("Device Playback", "Device Playback", DevicePlaybackReport.class);
	public static final ReportType BILLABLE_DEVICE_REPORT = new ReportType("Billable Device", "Billable Device", BillableDeviceReport.class);
	public static final ReportType ADVERTISER_BILLING = new ReportType("Advertiser Billing", "Advertiser Billing", AdvertiserBillingReport.class);
	public static final ReportType REVENUE_SHARING = new ReportType("Revenue Sharing", "Revenue Sharing", RevenueSharingReport.class);
	public static final Map INSTANCES = new HashMap();	

	private Class reportClass;
	
	static
	{
		INSTANCES.put(ASSET_AIRING.toString(), ASSET_AIRING);
		INSTANCES.put(ASSET_SUMMARY.toString(), ASSET_SUMMARY);
		INSTANCES.put(DAILY_CAMPAIGN.toString(), DAILY_CAMPAIGN);
		INSTANCES.put(DEVICE_PLAYBACK.toString(), DEVICE_PLAYBACK);
		INSTANCES.put(BILLABLE_DEVICE_REPORT.toString(), BILLABLE_DEVICE_REPORT);
		INSTANCES.put(ADVERTISER_BILLING.toString(), ADVERTISER_BILLING);
		INSTANCES.put(REVENUE_SHARING.toString(), REVENUE_SHARING);
	}
	
	public ReportType(){}
	
	protected ReportType(String name, String persistentValue, Class reportClass) {
		super(name, persistentValue);
		this.reportClass = reportClass;
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public static List getReportTypes()
	{
		List l = new LinkedList();
		Iterator i = INSTANCES.values().iterator();
		while(i.hasNext())
		{
			l.add(i.next());
		}
		
		// Sort the list in alphabetical order
		Collections.sort(l);
		
		return l;
	}
	
	public static ReportType getReportType(String reportTypeName)
	{
		return (ReportType) INSTANCES.get( reportTypeName );
	}

	public Class getReportClass() {
		return reportClass;
	}
}