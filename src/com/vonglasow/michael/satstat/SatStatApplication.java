package com.vonglasow.michael.satstat;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.util.Log;

public class SatStatApplication extends Application implements OnRequestPermissionsResultCallback {

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		boolean isGranted = false;
		for (int i = 0; i < grantResults.length; i++)
			if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && (grantResults[i] == PackageManager.PERMISSION_GRANTED))
				isGranted = true;
		if (isGranted)
			sendBroadcast(new Intent(Const.AGPS_DATA_EXPIRED));
		else
			Log.w("PasvLocListenerService", "ACCESS_FINE_LOCATION permission not granted. AGPS data could not be updated.");
	}

}
