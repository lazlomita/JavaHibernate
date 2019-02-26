package com.kuvata.kmf.util;

import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.PermissionEntry;
import com.kuvata.kmf.Role;
import com.kuvata.kmf.SchemaDirectory;

public class CreateNoRolePermissionEntries {

	public static void main(String[] args) throws Exception{
		
		// Initialize the schema
		SchemaDirectory.initialize("kuvata", "CreateNoRolePermissionEntries", null, false, true);
		
		// Get all assets
		List<Asset> assets = HibernateSession.currentSession().createQuery("from Asset").list();
		
		// Get the *NoRoles role
		Role role = Role.getRole(0l);
		
		// Start bulk mode
		HibernateSession.startBulkmode();
		
		// Create the permission entries
		for(Asset a : assets){
			PermissionEntry.create(a.getAssetId(), role);
		}
		
		// Stop bulk mode
		HibernateSession.stopBulkmode();
	}
}