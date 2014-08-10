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

package org.jnegre.android.osmonthego.util;

import android.os.Handler;
import android.os.Message;

/**
 * To be called from the main (UI) thread only.
 * Initial state is paused.
 */
public abstract class DelayedPausable {

	private final static int MESSAGE_CODE_PAUSE = 1;

	private final long delayMS;
	private boolean paused = true;
	private final Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			if(message.what == MESSAGE_CODE_PAUSE) {
				paused = true;
				onPause();
				return true;
			} else {
				return false;
			}
		}
	});

	protected DelayedPausable(long delayMS) {
		this.delayMS = delayMS;
	}

	public void pause() {
		handler.sendEmptyMessageDelayed(MESSAGE_CODE_PAUSE, delayMS);
	}

	public void resume() {
		handler.removeMessages(MESSAGE_CODE_PAUSE);
		if(paused) {
			paused = false;
			onResume();
		}

	}

	protected abstract void onPause();
	protected abstract void onResume();

}
