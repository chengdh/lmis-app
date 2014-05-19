package com.lmis.widgets.message;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class MessageRemoteViewService extends RemoteViewsService {

	public static final String TAG = "MessageRemoteViewService";

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		Log.d(TAG, "MessageRemoteViewService->onGetViewFactory()");
		MessageRemoteViewFactory rvFactory = new MessageRemoteViewFactory(
				this.getApplicationContext(), intent);
		return rvFactory;
	}

}
