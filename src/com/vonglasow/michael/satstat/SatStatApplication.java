/*
 * Copyright © 2013–2016 Michael von Glasow.
 * 
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

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
