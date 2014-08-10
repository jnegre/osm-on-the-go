/*
 * Copyright 2014  Jérôme Nègre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jnegre.android.osmonthego.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class SurveyProviderMetaData {
	public final static String AUTHORITY = "org.jnegre.provider.osm.SurveyProvider";
	public final static String DATABASE_NAME = "survey.db";
	public final static int DATABASE_VERSION = 2;

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

	public final static class FixmeTableMetaData implements BaseColumns {
		public final static String TABLE_NAME = "fixme";
		//uri and MIME type definitions
		public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/fixme");

		public final static String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jnegre.osm.survey.fixme";
		public final static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jnegre.osm.survey.fixme";

		//Additional Columns start here.
		//REAL type
		public final static String LATITUDE = "lat";
		//REAL type
		public final static String LONGITUDE = "lng";
		//string type
		public final static String COMMENT = "comment";
	}


}
