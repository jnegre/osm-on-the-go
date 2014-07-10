package org.jnegre.android.osmonthego.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class SurveyProviderMetaData {
	public final static String AUTHORITY = "org.jnegre.provider.osm.SurveyProvider";
	public final static String DATABASE_NAME = "survey.db";
	public final static int DATABASE_VERSION = 1;

	public final static class AddressTableMetaData implements BaseColumns {
		public final static String TABLE_NAME = "addresses";
		//uri and MIME type definitions
		public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/addresses");

		public final static String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jnegre.osm.survey.address";
		public final static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jnegre.osm.survey.address";

		//Additional Columns start here.
		//REAL type
		public final static String LATITUDE = "lat";
		//REAL type
		public final static String LONGITUDE = "lng";
		//string type
		public final static String STREET = "street";
		//string type
		public final static String NUMBER = "number";


	}


}
