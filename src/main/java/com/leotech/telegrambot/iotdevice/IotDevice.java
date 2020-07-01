/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.iotdevice;

import com.leotech.telegrambot.bases.BasicDevice;
import java.time.LocalDate;

/**
 *
 * @author Anusha
 */
public class IotDevice {
    /*
    
    "time": <timestamp>,
    "SP": <temp>, // only in heating/cooling/thermostatic mode
    "temp": <temp>, // only in heating/cooling/thermostatic mode
    "unit": <unit>, // only in heating/cooling/thermostatic mode
    "RH SP": <rh>, // only in humidify/dehumidify/hygrostatic mode
    "humidity": <rh>, // only in humidify/dehumidify/hygrostatic mode
    "mode": <mode>, "relay": <relay>, "runmode" :<run> }
    ●   <timestamp> is an integer number of seconds
    ● <temp> is a floating-point number expressing a temperature value
    ● <unit> is a string reporting measurement unit  {"C", "F"}
    ● <rh> is a floating-point number expressing a relative humidity value
    ● <mode> is a string whose possible values are {"heating", "cooling", "humidify", "dehumidify","off"}
    ● <relay> is a string whose possible values are {"on", "off"}
    ● <run> is a string reporting the run mode type  ({"standard", "advanced"} when in heating/cooling/thermostatic mode, {"humidity"} when in humidify/dehumidify/hygrostatic mode)

    */
    private String _deviceParameters[] = {"time","SP","temp","unit","RH SP","humidity","mode","relay","runmode"};
    BasicDevice _iotDevice;
    
    enum MODE {HEATING, COOLING, HUMIDIFY, DEHUMIDIFY};
    enum RUN {STANDARD, ADVANCED};
    
    public IotDevice(){
        _iotDevice = new BasicDevice();
        _iotDevice.addDeviceParam(_deviceParameters[0], LocalDate.now());
        _iotDevice.addDeviceParam(_deviceParameters[1], 0.0);
        _iotDevice.addDeviceParam(_deviceParameters[2], 0.0);
        _iotDevice.addDeviceParam(_deviceParameters[3], 'C');
        _iotDevice.addDeviceParam(_deviceParameters[4], 0.0);
        _iotDevice.addDeviceParam(_deviceParameters[5], 0.0);
        _iotDevice.addDeviceParam(_deviceParameters[6], MODE.COOLING);
        _iotDevice.addDeviceParam(_deviceParameters[7], false);
        _iotDevice.addDeviceParam(_deviceParameters[8], RUN.STANDARD);
    }
    
    public void setTimeStamp(LocalDate t){
        _iotDevice.setDeviceParam(_deviceParameters[0], t);
    }
    
    public LocalDate getTimeStamp(){
        return _iotDevice.getDeviceParam(_deviceParameters[0]);
    }
    
    public void setSP(float value){
        _iotDevice.setDeviceParam(_deviceParameters[1], value);
    }
    
    public float getSP(){
        return _iotDevice.getDeviceParam(_deviceParameters[1]);
    }
    
    
}
