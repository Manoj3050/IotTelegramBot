/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.bases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Anusha
 */
public class BasicDevice {
    private Map<String,BasicDeviceParam> _deviceParameters; // where device parameters are kept
    
    public BasicDevice(){
        _deviceParameters = new HashMap<String,BasicDeviceParam>();
    }
    
    //Add a device parameter sent with param Name and parameter 
    public <T> void addDeviceParam(String paramName, T t){
        _deviceParameters.put(paramName, new BasicDeviceParam<T>(t));
    }
    
    //return device parameter when requested by parameter name
    public <T> T getDeviceParam(String paramname){
        return (T)_deviceParameters.get(paramname).getParam();
    }
    
    //update/set device parameter when send with new value
    public <T> void setDeviceParam(String paramName, T value ){
        BasicDeviceParam t = _deviceParameters.get(paramName);
        t.setParam(value);
    }
    
    public void removeDeviceParam(String paramName){
        _deviceParameters.remove(paramName);
    }
    
}
