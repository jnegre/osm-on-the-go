package org.jnegre.android.osmonthego.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.AddressTableMetaData;
import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.FixmeTableMetaData;

import java.util.HashMap;
import java.util.Map;

public class SurveyProvider extends ContentProvider {

	private final static String TAG = "SurveyProvider";

	//projection maps
	private static Map<String, String> addressProjectionMap;
	static {
		addressProjectionMap = new HashMap<String, String>();
		addressProjectionMap.put(AddressTableMetaData._ID, AddressTableMetaData._ID);
		addressProjectionMap.put(AddressTableMetaData.LATITUDE, AddressTableMetaData.LATITUDE);
		addressProjectionMap.put(AddressTableMetaData.LONGITUDE, AddressTableMetaData.LONGITUDE);
		addressProjectionMap.put(AddressTableMetaData.NUMBER, AddressTableMetaData.NUMBER);
		addressProjectionMap.put(AddressTableMetaData.STREET, AddressTableMetaData.STREET);
	}

	private static Map<String, String> fixmeProjectionMap;
	static {
		fixmeProjectionMap = new HashMap<String, String>();
		fixmeProjectionMap.put(FixmeTableMetaData._ID, FixmeTableMetaData._ID);
		fixmeProjectionMap.put(FixmeTableMetaData.LATITUDE, FixmeTableMetaData.LATITUDE);
		fixmeProjectionMap.put(FixmeTableMetaData.LONGITUDE, FixmeTableMetaData.LONGITUDE);
		fixmeProjectionMap.put(FixmeTableMetaData.COMMENT, FixmeTableMetaData.COMMENT);
	}


	// URI matcher
	private final static UriMatcher URI_MATCHER;
	private final static int URI_INDICATOR_ADDRESS_COLLECTION = 1;
	private final static int URI_INDICATOR_SINGLE_ADDRESS = 2;
	private final static int URI_INDICATOR_FIXME_COLLECTION = 3;
	private final static int URI_INDICATOR_SINGLE_FIXME = 4;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(SurveyProviderMetaData.AUTHORITY, "addresses", URI_INDICATOR_ADDRESS_COLLECTION);
		URI_MATCHER.addURI(SurveyProviderMetaData.AUTHORITY, "addresses/#", URI_INDICATOR_SINGLE_ADDRESS);
		URI_MATCHER.addURI(SurveyProviderMetaData.AUTHORITY, "fixme", URI_INDICATOR_FIXME_COLLECTION);
		URI_MATCHER.addURI(SurveyProviderMetaData.AUTHORITY, "fixme/#", URI_INDICATOR_SINGLE_FIXME);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {


		private DatabaseHelper(Context context) {
			super(context, SurveyProviderMetaData.DATABASE_NAME, null, SurveyProviderMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "inner oncreate called");
			db.execSQL("CREATE TABLE " + AddressTableMetaData.TABLE_NAME + " ("
					+ AddressTableMetaData._ID + " INTEGER PRIMARY KEY,"
					+ AddressTableMetaData.LATITUDE + " REAL,"
					+ AddressTableMetaData.LONGITUDE + " REAL,"
					+ AddressTableMetaData.NUMBER + " TEXT,"
					+ AddressTableMetaData.STREET + " TEXT"
					+ ");");
			db.execSQL("CREATE TABLE " + FixmeTableMetaData.TABLE_NAME + " ("
					+ FixmeTableMetaData._ID + " INTEGER PRIMARY KEY,"
					+ FixmeTableMetaData.LATITUDE + " REAL,"
					+ FixmeTableMetaData.LONGITUDE + " REAL,"
					+ FixmeTableMetaData.COMMENT + " TEXT"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "inner onupgrade called");
			Log.w(TAG, "Upgrading database from version "
					+ oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + AddressTableMetaData.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + FixmeTableMetaData.TABLE_NAME);
			onCreate(db);

		}
	}

	private DatabaseHelper databaseHelper;


	@Override
	public boolean onCreate() {
		Log.d(TAG, "main onCreate called");
		databaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {

		Log.d(TAG, "query " + uri);

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (URI_MATCHER.match(uri)) {
			case URI_INDICATOR_ADDRESS_COLLECTION:
				qb.setTables(AddressTableMetaData.TABLE_NAME);
				qb.setProjectionMap(addressProjectionMap);
				break;
			case URI_INDICATOR_SINGLE_ADDRESS:
				qb.setTables(AddressTableMetaData.TABLE_NAME);
				qb.setProjectionMap(addressProjectionMap);
				qb.appendWhere(AddressTableMetaData._ID + "="
						+ uri.getPathSegments().get(1));
				break;
			case URI_INDICATOR_FIXME_COLLECTION:
				qb.setTables(FixmeTableMetaData.TABLE_NAME);
				qb.setProjectionMap(fixmeProjectionMap);
				break;
			case URI_INDICATOR_SINGLE_FIXME:
				qb.setTables(FixmeTableMetaData.TABLE_NAME);
				qb.setProjectionMap(fixmeProjectionMap);
				qb.appendWhere(AddressTableMetaData._ID + "="
						+ uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = databaseHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);

		// Tell the cursor what uri to watch,
		// so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;

	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		String tableName;
		String nullableColumn;

		switch (URI_MATCHER.match(uri)) {
			case URI_INDICATOR_ADDRESS_COLLECTION:
				tableName = AddressTableMetaData.TABLE_NAME;
				nullableColumn = AddressTableMetaData.STREET;
				break;
			case URI_INDICATOR_FIXME_COLLECTION:
				tableName = FixmeTableMetaData.TABLE_NAME;
				nullableColumn = FixmeTableMetaData.COMMENT;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		long rowId = db.insert(tableName, nullableColumn, contentValues);
		if (rowId > 0) {
			Uri insertedUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(insertedUri, null);
			return insertedUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		int count;
		String rowId;
		switch (URI_MATCHER.match(uri)) {
			case URI_INDICATOR_ADDRESS_COLLECTION:
				count = db.delete(AddressTableMetaData.TABLE_NAME,
						where, whereArgs);
				break;
			case URI_INDICATOR_FIXME_COLLECTION:
				count = db.delete(FixmeTableMetaData.TABLE_NAME,
						where, whereArgs);
				break;
			case URI_INDICATOR_SINGLE_ADDRESS:
				rowId = uri.getPathSegments().get(1);
				count = db.delete(AddressTableMetaData.TABLE_NAME,
						AddressTableMetaData._ID + "=" + rowId //FIXME sqli???
								+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
						whereArgs
				);
				break;
			case URI_INDICATOR_SINGLE_FIXME:
				rowId = uri.getPathSegments().get(1);
				count = db.delete(FixmeTableMetaData.TABLE_NAME,
						FixmeTableMetaData._ID + "=" + rowId //FIXME sqli???
								+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
						whereArgs
				);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;

	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
		throw new IllegalStateException("not yet implemented");
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case URI_INDICATOR_ADDRESS_COLLECTION:
				return AddressTableMetaData.CONTENT_TYPE;
			case URI_INDICATOR_SINGLE_ADDRESS:
				return AddressTableMetaData.CONTENT_ITEM_TYPE;
			case URI_INDICATOR_FIXME_COLLECTION:
				return FixmeTableMetaData.CONTENT_TYPE;
			case URI_INDICATOR_SINGLE_FIXME:
				return FixmeTableMetaData.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
}
