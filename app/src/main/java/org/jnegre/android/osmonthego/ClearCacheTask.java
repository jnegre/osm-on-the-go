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

package org.jnegre.android.osmonthego;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import java.io.File;

public class ClearCacheTask extends AsyncTask<Void, Void, Void> {

	private final Context context;

	public ClearCacheTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... voids) {
		deleteFiles(OpenStreetMapTileProviderConstants.TILE_PATH_BASE);
		return null;
	}

	@Override
	protected void onPostExecute(Void whatever) {
		Toast.makeText(context, R.string.msg_cache_cleared, Toast.LENGTH_SHORT).show();
	}

	private void deleteFiles(File dir) {
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) {
				deleteFiles(file);
			} else if (file.isFile()) {
				file.delete();
			}
		}
	}
}
