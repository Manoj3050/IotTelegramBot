/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.bases;

/**
 *
 * @author Anusha
 */
public class BasicDeviceParam<T> {
    private T param;
    
    public BasicDeviceParam(){
        
    }
    
    public BasicDeviceParam(T t){
        param = t;
    }
    
    public void setParam(T t){
        param = t;
    }
    
    public T getParam(){
        return param;
    }
}
