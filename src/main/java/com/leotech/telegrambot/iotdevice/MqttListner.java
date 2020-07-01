/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.iotdevice;

import com.leotech.telegrambot.bot.TelegramBot;
import com.leotech.smartpid.updateHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Anusha
 */
public class MqttListner implements MqttCallback{
    
    private MqttAsyncClient client = null;
    private TelegramBot bot = null;
    
    
    public MqttListner(TelegramBot b){
        try {
            client = new MqttAsyncClient(MqttVars.MQTT_HOST, MqttVars.MQTT_CLIENT_ID);
            MqttConnectOptions connOpts = setUpConnectionOptions(MqttVars.MQTT_USER, MqttVars.MQTT_PASSWORD);
            client.connect(connOpts);
            client.setCallback(this);
            /*MqttMessage message = new MqttMessage();
            message.setPayload("A single message from my computer fff"
                    .getBytes());
            client.publish("foo", message);*/
        } catch (MqttException e) {
            e.printStackTrace();
        }
        while(client.isConnected() == false){}
        this.bot = b;
    }
    
    private  MqttConnectOptions setUpConnectionOptions(String username, String password) {
       MqttConnectOptions connOpts = new MqttConnectOptions();
       connOpts.setCleanSession(true);
       connOpts.setUserName(username);
       connOpts.setPassword(password.toCharArray());
       return connOpts;
   }  

    @Override
    public void connectionLost(Throwable thrwbl) {
        
    }

    @Override
    public void messageArrived(String topic, MqttMessage mm) throws Exception {
        String message = new String(mm.getPayload());
        new updateHandler(topic,message,bot).run();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        
    }
    
    public void subscribe(String topic){
        try {
            client.subscribe(topic,0,new MqttThreadedListner(bot));
            Logger.getLogger(MqttListner.class.getName()).log(Level.INFO, "Subscribed to topic: "+ topic);
            System.out.println("Subscribed to topic: "+ topic);
        } catch (MqttException ex) {
            Logger.getLogger(MqttListner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void unsubscribe(String topic){
        try {
            client.unsubscribe(topic);
            Logger.getLogger(MqttListner.class.getName()).log(Level.INFO,"Unsubscribed from topic: "+ topic);
            System.out.println("Unsubscribed from topic: "+ topic);
        } catch (MqttException ex) {
            Logger.getLogger(MqttListner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
