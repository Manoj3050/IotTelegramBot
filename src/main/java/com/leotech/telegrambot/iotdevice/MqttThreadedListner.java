/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.iotdevice;

import com.leotech.telegrambot.bot.TelegramBot;
import com.leotech.telegrambot.dbAccess.sqlConnection;
import com.leotech.smartpid.updateHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 * @author Anusha
 */
public class MqttThreadedListner implements IMqttMessageListener {

    private ExecutorService pool;
    private TelegramBot bot;

    public MqttThreadedListner(TelegramBot _b) {
        this.bot = _b;
        pool = Executors.newFixedThreadPool(10);
    }

    class MessageHandler implements Runnable {

        MqttMessage message;
        String topic;

        public MessageHandler(String topic, MqttMessage message) {
            this.message = message;
            this.topic = topic;
        }

        public void run() {
            Logger.getLogger(MqttThreadedListner.class.getName()).log(Level.INFO, "Thread [ " + Thread.currentThread().getName()
                    + "], Topic[ " + topic + "],  Message [" + message + "] ");
            System.out.println("Thread [ " + Thread.currentThread().getName()
                    + "], Topic[ " + topic + "],  Message [" + message + "] ");
            String topic_split[] = topic.split("/");
            if (topic_split.length > 3) {
                String deviceSerialIDHash = topic_split[2];
                Long getChatIDToSend = sqlConnection.getChatID(deviceSerialIDHash);
                if (getChatIDToSend != 0) {
                    SendMessage updateMessage = new SendMessage();
                    updateMessage.setChatId(getChatIDToSend);
                    updateMessage.setText(new String(message.getPayload()));
                    try {
                        bot.execute(updateMessage);
                    } catch (TelegramApiException ex) {
                        Logger.getLogger(updateHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Logger.getLogger(MqttThreadedListner.class.getName()).log(Level.INFO,"Message received. : " + message);
        System.out.println("Message received. : " + message);
        pool.execute(new MessageHandler(topic, message));
    }

}
