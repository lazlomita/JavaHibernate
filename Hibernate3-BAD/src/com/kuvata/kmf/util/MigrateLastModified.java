package com.kuvata.kmf.util;

import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.EntityInstance;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class MigrateLastModified {

	public static void main(String[] args) {
		
		SchemaDirectory.initialize("kuvata", "MigrateLastModified", null, false, true);
		
		// Devices
		String hql = "SELECT d from Device d WHERE d.lastModified IS NULL ORDER BY UPPER(d.deviceName)";
		List<Device> devices = HibernateSession.currentSession().createQuery(hql).list();
		for(Device d : devices){
			System.out.println("Updating device " + d.getDeviceName());
			EntityInstance ei = EntityInstance.getEntityInstance(d.getDeviceId());
			if(ei != null){
				d.setLastModified(ei.getLastModified());
				d.update(false, false);
			}
		}
		
		// Assets
		hql = "SELECT a from Asset a WHERE a.lastModified IS NULL ORDER BY UPPER(a.assetName)";
		List<Asset> assets = HibernateSession.currentSession().createQuery(hql).list();
		for(Asset a : assets){
			System.out.println("Updating asset " + a.getAssetName());
			EntityInstance ei = EntityInstance.getEntityInstance(a.getAssetId());
			if(ei != null){
				a.setLastModified(ei.getLastModified());
				a.update(false, false);
			}
		}
	}

}
