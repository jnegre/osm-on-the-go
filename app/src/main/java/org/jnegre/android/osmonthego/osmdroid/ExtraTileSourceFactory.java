package org.jnegre.android.osmonthego.osmdroid;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

public class ExtraTileSourceFactory {
	//FIXME what's the use of ResourceProxy.string.mapnik?

	//Base
	public final static OnlineTileSourceBase OSM_FR = new XYTileSource("OSMfr",
			ResourceProxy.string.mapnik, 0, 19, 256, ".png", new String[]{
			"http://a.tile.openstreetmap.fr/osmfr/",
			"http://b.tile.openstreetmap.fr/osmfr/",
			"http://c.tile.openstreetmap.fr/osmfr/"}
	);

	//Overlay
	public final static OnlineTileSourceBase BANO = new XYTileSource("BANO",
			ResourceProxy.string.mapnik, 0, 17, 256, ".png", new String[]{
			"http://a.layers.openstreetmap.fr/bano/",
			"http://b.layers.openstreetmap.fr/bano/",
			"http://c.layers.openstreetmap.fr/bano/"}
	);
	public final static OnlineTileSourceBase NO_NAME = new XYTileSource("NoName",
			ResourceProxy.string.mapnik, 0, 19, 256, ".png", new String[]{
			"http://a.layers.openstreetmap.fr/noname/",
			"http://b.layers.openstreetmap.fr/noname/",
			"http://c.layers.openstreetmap.fr/noname/"}
	);
}
