package com.hien.schoolnotescan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WifiSharingFragment extends RootFragment{
	
	private WebServer mServer;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.wifi_sharing_frag, container);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	
		super.onActivityCreated(savedInstanceState);
		
		final ToggleButton btnWifi = (ToggleButton) getView().findViewById(R.id.btnWifi);
		final TextView txtMessage = (TextView) getView().findViewById(R.id.txtMessage);
		btnWifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			
				// Create web server if not yet
				if (mServer == null)
					mServer = new WebServer(getResources(), 
							((DocumentFragment) getFragmentManager().findFragmentById(R.id.fragDocument))
							.mDocManager, getActivity());
				
				if (isChecked) {
					try {
						if (!wifiIsReady()) {
							Toast.makeText(getActivity(), "Please turn on Wifi!", Toast.LENGTH_LONG).show();
							throw new Exception();
						}
							
						try {
							mServer.start();
						} catch (Exception e) {
							Toast.makeText(getActivity(), "Cannot open Wifi sharing", Toast.LENGTH_LONG).show();
							throw e;
						}
						
						txtMessage.setText("Stay on this page and visit http://" + getIPAddress(true) + 
								":8080 on your computer with your web browser to access your files");
					} catch (Exception e) {
						btnWifi.setChecked(false);
					}
				} else {
					mServer.stop();
					txtMessage.setText("Using Wifi sharing you can quickly access your documents from your computer.");
				}
			}
		});
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	private boolean wifiIsReady() {
		
		ConnectivityManager connManager = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return mWifi.isConnected();
	}
	
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}

