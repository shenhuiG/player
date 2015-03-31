package com.vst.LocalPlayer.component.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.vst.LocalPlayer.component.activity.screen.DeviceScreen;
import com.vst.LocalPlayer.component.activity.IFragmentJump;
import com.vst.LocalPlayer.component.activity.screen.MainScreen;
import com.vst.LocalPlayer.R;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;

public class BA extends FragmentActivity implements IFragmentJump {
    FragmentManager fm;
    MainScreen mainScreen;
    DeviceScreen deviceScreen;
    final static int CONTAINAL_ID = android.R.id.home;

    private ArrayList<DeviceDisplay> listAdapter = new ArrayList<DeviceDisplay>();
    private AndroidUpnpService upnpService;
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            // Clear the list
            // listAdapter.clear();
            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }
            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, Bservice.class), serviceConnection, Context.BIND_AUTO_CREATE);
        View root = getWindow().getDecorView();
        root.setId(CONTAINAL_ID);
        root.setBackgroundResource(R.drawable.wallpaper_1);
        mainScreen = new MainScreen();
        deviceScreen = new DeviceScreen();
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(CONTAINAL_ID, mainScreen, "mainScreen");
        ft.commitAllowingStateLoss();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void fragmentJump(String tag, Bundle args) {
        if ("device".equals(tag)) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(CONTAINAL_ID, deviceScreen, "device");
            ft.addToBackStack(null);
            ft.commitAllowingStateLoss();
        }
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(
                            BA.this,
                            "Discovery failed of '" + device.getDisplayString() + "': "
                                    + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                            Toast.LENGTH_LONG).show();
                }
            });
            deviceRemoved(device);
        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            System.out.println("deviceAdded:" + device);
            runOnUiThread(new Runnable() {
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);
                    int position = listAdapter.indexOf(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same
                        // position
                        listAdapter.remove(d);
                        listAdapter.add(position, d);
                    } else {
                        listAdapter.add(d);
                    }
                }
            });
        }

        public void deviceRemoved(final Device device) {
            System.out.println("deviceRemoved:" + device);
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    protected class DeviceDisplay {

        Device device;

        public DeviceDisplay(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }

        // DOC:DETAILS
        public String getDetailsMessage() {
            StringBuilder sb = new StringBuilder();
            if (getDevice().isFullyHydrated()) {
                sb.append(getDevice().getDisplayString());
                sb.append("\n\n");
                for (Service service : getDevice().getServices()) {
                    sb.append(service.getServiceType()).append("\n");
                }
            } else {
                sb.append(getString(R.string.deviceDetailsNotYetAvailable));
            }
            return sb.toString();
        }

        // DOC:DETAILS

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public String toString() {
            String name = getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null ? getDevice()
                    .getDetails().getFriendlyName() : getDevice().getDisplayString();
            // Display a little star while the device is being loaded (see
            // performance optimization earlier)
            return device.isFullyHydrated() ? name : name + " *";
        }
    }
}
