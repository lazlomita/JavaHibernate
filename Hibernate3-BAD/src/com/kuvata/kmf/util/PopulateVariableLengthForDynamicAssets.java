package com.kuvata.kmf.util;

import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.ContentRotationAsset;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class PopulateVariableLengthForDynamicAssets {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SchemaDirectory.initialize( "kuvata", "PopulateVariableLengthForDynamicAssets", null, false, true );
		
		String hql = "SELECT cra FROM ContentRotationAsset cra WHERE cra.variableLength IS NULL AND "
					+ "cra.asset.class IN ('TargetedAsset', 'AdServer')";
		
		List<ContentRotationAsset> cras = HibernateSession.currentSession().createQuery(hql).list();
		
		HibernateSession.startBulkmode();
		
		for(ContentRotationAsset cra : cras){
			Asset a = Asset.convert(cra.getAsset());
			System.out.println("Updating: " + cra.getContentRotationAssetId() + " -- " + a.getAssetName());
			cra.setVariableLength(a.getAssetPresentation().getVariableLength());
			cra.update();
		}
		
		HibernateSession.stopBulkmode();
	}

}
