package org.jnegre.android.osmonthego.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;
import org.jnegre.android.osmonthego.AddressExporter;
import org.jnegre.android.osmonthego.ClearCacheTask;
import org.jnegre.android.osmonthego.R;
import org.jnegre.android.osmonthego.osmdroid.AddressOverlay;
import org.jnegre.android.osmonthego.osmdroid.ControlOverlay;
import org.jnegre.android.osmonthego.osmdroid.ExtraTileSourceFactory;
import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class MapActivity extends Activity {

	private final static boolean DEBUG = false;
	private final static String TAG = "MapActivity";

	private final static String PREF_ZOOM_LEVEL = "ZOOM_LEVEL";
	private final static String PREF_SCROLL_X = "SCROLL_X";
	private final static String PREF_SCROLL_Y = "SCROLL_Y";
	private final static String PREF_OVERLAY_BANO_ENABLED = "OVERLAY_BANO_ENABLED";
	private final static String PREF_OVERLAY_NO_NAME_ENABLED = "OVERLAY_NO_NAME_ENABLED";
	private final static String PREF_BASE_LAYER = "BASE_LAYER";
	private final static BaseLayer DEFAULT_BASE_LAYER = BaseLayer.OSM_FR;

	private final static int LOADER_ADDRESS_LAYER = 1;

	private static enum BaseLayer {
		OSM_FR(R.id.action_show_base_osmfr, ExtraTileSourceFactory.OSM_FR),
		MAPNIK(R.id.action_show_base_mapnik, TileSourceFactory.MAPNIK),
		MAPQUESTOSM(R.id.action_show_base_mapquestosm, TileSourceFactory.MAPQUESTOSM);

		private final int action;
		private final ITileSource tileSource;

		BaseLayer(int action, ITileSource tileSource) {
			this.action = action;
			this.tileSource = tileSource;
		}

		ITileSource getTileSource() {
			return tileSource;
		}

		public int getAction() {
			return action;
		}
	}


	private MapView mapView;
	private TilesOverlay banoOverlay;
	private TilesOverlay noNameOverlay;
	private MyLocationNewOverlay myLocationOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		final Context context = this.getApplicationContext();
		final DisplayMetrics dm = context.getResources().getDisplayMetrics();

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setMultiTouchControls(true);

		banoOverlay = addOverlay(ExtraTileSourceFactory.BANO);
		noNameOverlay = addOverlay(ExtraTileSourceFactory.NO_NAME);

		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(context);
		scaleBarOverlay.setCentred(true);
		scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

		myLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context), mapView);

		AddressOverlay addressOverlay = new AddressOverlay(context, mapView);
		getLoaderManager().initLoader(LOADER_ADDRESS_LAYER, null, addressOverlay);

		mapView.getOverlays().add(this.myLocationOverlay);
		mapView.getOverlays().add(addressOverlay);
		mapView.getOverlays().add(scaleBarOverlay);
		mapView.getOverlays().add(new ControlOverlay(context)); //must be the last one

		Log.d(TAG, "restoring saved state");
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		useBaseLayer(getSavedBaseLayer(pref), null);
		mapView.getController().setZoom(pref.getInt(PREF_ZOOM_LEVEL, 1));
		mapView.scrollTo(pref.getInt(PREF_SCROLL_X, 0), pref.getInt(PREF_SCROLL_Y, 0));
		banoOverlay.setEnabled(pref.getBoolean(PREF_OVERLAY_BANO_ENABLED, false));
		noNameOverlay.setEnabled(pref.getBoolean(PREF_OVERLAY_NO_NAME_ENABLED, false));

		if(DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build());

			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build());
		}

	}

	private BaseLayer getSavedBaseLayer(SharedPreferences pref) {
		String saved = pref.getString(PREF_BASE_LAYER, DEFAULT_BASE_LAYER.name());
		try {
			return Enum.valueOf(BaseLayer.class, saved);
		} catch (IllegalArgumentException e) {
			//the saved BaseLayer does not exist anymore
			return DEFAULT_BASE_LAYER;
		}
	}


	private TilesOverlay addOverlay(OnlineTileSourceBase source) {
		Context context = this.getApplicationContext();
		TilesOverlay tilesOverlay = new TilesOverlay(new MapTileProviderBasic(context, source), context);
		tilesOverlay.setLoadingBackgroundColor(Color.argb(20, 128, 0, 0));
		mapView.getOverlayManager().add(tilesOverlay);
		return tilesOverlay;
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "saving state");
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		pref.edit()
				.putInt(PREF_SCROLL_X, mapView.getScrollX())
				.putInt(PREF_SCROLL_Y, mapView.getScrollY())
				.putInt(PREF_ZOOM_LEVEL, mapView.getZoomLevel())
				.putBoolean(PREF_OVERLAY_BANO_ENABLED, banoOverlay.isEnabled())
				.putBoolean(PREF_OVERLAY_NO_NAME_ENABLED, noNameOverlay.isEnabled())
				.commit();
		this.myLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.myLocationOverlay.enableMyLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		//base layer
		menu.findItem(getSavedBaseLayer(pref).getAction()).setChecked(true);
		//overlays
		menu.findItem(R.id.action_show_layer_bano).setChecked(pref.getBoolean(PREF_OVERLAY_BANO_ENABLED, false));
		menu.findItem(R.id.action_show_layer_noname).setChecked(pref.getBoolean(PREF_OVERLAY_NO_NAME_ENABLED, false));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_add_address:
				IGeoPoint position = mapView.getMapCenter();
				Intent intent = new Intent(this, AddressActivity.class);
				intent.putExtra(AddressActivity.EXTRA_LATITUDE, position.getLatitude());
				intent.putExtra(AddressActivity.EXTRA_LONGITUDE, position.getLongitude());
				this.startActivity(intent);
				return true;
			case R.id.action_clear_cache:
				clearCache();
				return true;
			case R.id.action_clear_all:
				clearAllData();
				return true;
			case R.id.action_share:
				shareData();
				return true;
			case R.id.action_my_location:
				GeoPoint myLocation = myLocationOverlay.getMyLocation();
				if (myLocation != null) {
					mapView.getController().animateTo(myLocation);
				}
				return true;
			case R.id.action_show_base_osmfr:
				useBaseLayer(BaseLayer.OSM_FR, item);
				return true;
			case R.id.action_show_base_mapnik:
				useBaseLayer(BaseLayer.MAPNIK, item);
				return true;
			case R.id.action_show_base_mapquestosm:
				useBaseLayer(BaseLayer.MAPQUESTOSM, item);
				return true;
			case R.id.action_show_layer_bano:
				item.setChecked(!item.isChecked());
				banoOverlay.setEnabled(item.isChecked());
				mapView.invalidate();
				return true;
			case R.id.action_show_layer_noname:
				item.setChecked(!item.isChecked());
				noNameOverlay.setEnabled(item.isChecked());
				mapView.invalidate();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void useBaseLayer(BaseLayer layer, MenuItem menuItem) {
		if (menuItem != null) {
			menuItem.setChecked(true);
		}

		mapView.setTileSource(layer.getTileSource());

		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		pref.edit()
				.putString(PREF_BASE_LAYER, layer.name())
				.commit();

	}

	private void shareData() {
		AddressExporter exporter = new AddressExporter(getApplicationContext());

		if (exporter.export()) {
			//FIXME i18n the subject and content
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType(HTTP.OCTET_STREAM_TYPE);
			//emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"johndoe@exemple.com"});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OSM On The Go");
			emailIntent.putExtra(Intent.EXTRA_TEXT, "Your last survey.");
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exporter.getDestinationFile()));
			startActivity(emailIntent);
		} else {
			//FIXME i18n
			Toast.makeText(getApplicationContext(), "Could not export data.", Toast.LENGTH_SHORT).show();
		}
	}

	private void clearAllData() {
		Log.d(TAG, "Clearing all data");
		ContentResolver cr = getContentResolver();
		cr.delete(SurveyProviderMetaData.AddressTableMetaData.CONTENT_URI, null, null);
	}

	private void clearCache() {
		//ugly, but gets the job done...
		new ClearCacheTask(getApplicationContext()).execute();
	}
}
