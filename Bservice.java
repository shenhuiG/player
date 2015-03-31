package com.vst.LocalPlayer.component.service;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.types.ServiceType;

/**
 * Created by shenh on 2015/3/6.
 */
public class Bservice extends AndroidUpnpServiceImpl{

    @Override
    protected UpnpServiceConfiguration createConfiguration() {
        return new AndroidUpnpServiceConfiguration(){
            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return 7000;
            }

            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return super.getExclusiveServiceTypes();
            }
        };
    }



}
