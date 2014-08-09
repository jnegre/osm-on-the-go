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
