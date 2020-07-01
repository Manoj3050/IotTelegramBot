/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.smartpid;

import com.leotech.telegrambot.bot.TelegramBot;
import com.leotech.telegrambot.bot.botVars;
import com.leotech.telegrambot.dbAccess.DBvars;
import com.leotech.telegrambot.dbAccess.sqlConnection;
import com.leotech.telegrambot.iotdevice.MqttListner;
import com.leotech.telegrambot.iotdevice.MqttVars;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 * @author Anusha
 */
public class Kernel {

    /**
     * @param args the command line arguments
     */
    private TelegramBot bot = null;
    private sqlConnection dbConnection = null;
    private MqttListner mqttListner = null;

    public Kernel() {
        Logger.getLogger(Kernel.class.getName()).setLevel(Level.ALL);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        dbConnection = new sqlConnection();
        bot = new TelegramBot();
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        mqttListner = new MqttListner(bot);
        bot.setMqttClient(mqttListner);
    }

    public static void main(String[] args) {
        // TODO code application logic here
        ApiContextInitializer.init();
        PropertiesLoader prop;
        try {
            prop = new PropertiesLoader(new File("botConfig.xml"));
            MqttVars.MQTT_HOST = prop.lookupSymbol("MQTT_HOST");
            MqttVars.MQTT_USER = prop.lookupSymbol("MQTT_USER");
            MqttVars.MQTT_PASSWORD = prop.lookupSymbol("MQTT_PASSWORD");
            botVars.BOT_TOKEN = prop.lookupSymbol("BOT_TOKEN");
            botVars.BOT_USERNAME = prop.lookupSymbol("BOT_USERNAME");
            DBvars.HOST = prop.lookupSymbol("DB_HOST");
            DBvars.PORT = prop.lookupSymbol("DB_PORT");
            DBvars.USERNAME = prop.lookupSymbol("DB_USER");
            DBvars.PASSWORD = prop.lookupSymbol("DB_PASSWORD");
            DBvars.DBNAME = prop.lookupSymbol("DB_NAME");
        } catch (IOException ex) {
            Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        new Kernel();
    }

}
