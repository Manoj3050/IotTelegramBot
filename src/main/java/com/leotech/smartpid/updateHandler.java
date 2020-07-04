/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.smartpid;

import com.leotech.telegrambot.bot.TelegramBot;
import com.leotech.telegrambot.dbAccess.chatIDDeviceComb;
import com.leotech.telegrambot.dbAccess.sqlConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 * @author Anusha
 */
public class updateHandler implements Runnable{
    private String _topic = null;
    private String _message = null;
    private TelegramBot _bot = null;

    public updateHandler(String t,String m, TelegramBot b){
        this._topic = t;
        this._message = m;
        this._bot = b;
    }

    @Override
    public void run() {
        String topic_split[] = _topic.split("/");
        if(topic_split.length > 3){
            String deviceSerialID = topic_split[2];
            chatIDDeviceComb getChatIDToSend = sqlConnection.getChatID(deviceSerialID);
            if(getChatIDToSend._chatID != 0){
                SendMessage updateMessage = new SendMessage();
                updateMessage.setChatId(getChatIDToSend._chatID);
                updateMessage.setText(_message);
                try {
                    _bot.execute(updateMessage);
                } catch (TelegramApiException ex) {
                    Logger.getLogger(updateHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
}
