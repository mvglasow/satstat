/*
 * Copyright © 2014 Michael von Glasow.
 * Portions copyright © 2007 The Android Open Source Project
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

import android.net.wifi.ScanResult;

public abstract class WifiCapabilities {

    // Constants used for different security types
    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

    public static final String[] EAP_METHOD = { "PEAP", "TLS", "TTLS" };

    /** String present in capabilities if the scan result is ad-hoc */
    private static final String ADHOC_CAPABILITY = "[IBSS]";
    /** String present in capabilities if the scan result is enterprise secured */
    private static final String ENTERPRISE_CAPABILITY = "-EAP-";

    public static final String BSSID_ANY = "any";
    public static final int NETWORK_ID_NOT_SET = -1;
    /** This should be used with care! */
    static final int NETWORK_ID_ANY = -2;
   
    public static final int MATCH_NONE = 0;
    public static final int MATCH_WEAK = 1;
    public static final int MATCH_STRONG = 2;
    public static final int MATCH_EXACT = 3;

    /* Enterprise Fields */
    public static final int IDENTITY = 0;
    public static final int ANONYMOUS_IDENTITY = 1;
    public static final int CLIENT_CERT = 2;
    public static final int CA_CERT = 3;
    public static final int PRIVATE_KEY = 4;
    public static final int MAX_ENTRPRISE_FIELD = 5;
    
    /**
     * @return The security of a given {@link ScanResult}.
     */
    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { WEP, PSK, EAP };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
       
        return OPEN;
    }
   
    /**
     * @return Whether the given ScanResult represents an adhoc network.
     */
    public static boolean isAdhoc(ScanResult scanResult) {
        return scanResult.capabilities.contains(ADHOC_CAPABILITY);
    }
   
    /**
     * @return Whether the given ScanResult has enterprise security.
     */
    public static boolean isEnterprise(ScanResult scanResult) {
        return scanResult.capabilities.contains(ENTERPRISE_CAPABILITY);
    }
}
