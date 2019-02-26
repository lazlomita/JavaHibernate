package com.kuvata.kmf.util;

import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.PermissionEntry;
import com.kuvata.kmf.SchemaDirectory;

public class UpdateNoRolesForAssets {
	
	public static void main(String[] args) throws Exception{
		
		SchemaDirectory.initialize("kuvata", "UpdateNoRolesForAssets", null, false, true);
		
		List<Asset> l = HibernateSession.currentSession().createQuery("FROM Asset").list();
		for(Asset a : l){
			try {
				
				List<PermissionEntry> pes = PermissionEntry.getPermissionEntries(a.getEntityId());
				if(pes.size() == 1){
					for(PermissionEntry pe : pes){
						if(pe.getRole().getRoleId().longValue() != 0l){
							System.out.println("Error: " + a.getAssetName() + " seems to be missing a NoRoles row.");
						}
					}
				}else{
					if(pes.size() == 0){
						System.out.println("Error: " + a.getAssetName() + " has no permission_entry rows.");
					}else{
						boolean found = false;
						PermissionEntry peToDelete = null;
						for(PermissionEntry pe : pes){
							if(pe.getRole().getRoleId().longValue() == 0l){
								peToDelete = pe;
								found = true;
								break;
							}
						}
						
						if(found){
							peToDelete.delete();
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("DONE. No errors found.");
	}
}
