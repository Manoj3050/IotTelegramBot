/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.bot;

import com.leotech.telegrambot.dbAccess.sqlConnection;
import com.leotech.telegrambot.iotdevice.MqttListner;
import com.leotech.telegrambot.iotdevice.MqttVars;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 * @author Anusha
 */
public class TelegramBot extends org.telegram.telegrambots.bots.TelegramLongPollingBot {

    private MqttListner _mqttClient = null;

    public TelegramBot() {
        System.out.println("Bot Started");
        Logger.getLogger(TelegramBot.class.getName()).setLevel(Level.ALL);
    }

    @Override
    public String getBotToken() {
        return botVars.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String rec_message = update.getMessage().getText();
        Long chatID = update.getMessage().getChatId();
        handleMessage(rec_message, chatID);
    }

    @Override
    public String getBotUsername() {
        return botVars.BOT_USERNAME;
    }

    public void handleMessage(String message, Long chatID) {

        Logger.getLogger(TelegramBot.class.getName()).log(Level.INFO, message + " " + chatID);
        System.out.println(message + " " + chatID);
        if (message.startsWith("/start")) {
            //handle user registration
            String cmd_split[] = message.split(" ");
            if (cmd_split.length == 2) {
                //this is a deep linked registration. Get the unique_id and try to register the user
                if (sqlConnection.isUserExistsByUID(cmd_split[1].trim())) {
                    // if user exiting by UID, add to the telegram_bot and set inactive until we receive correct password

                }
            } else {
                //ask user to send the username via /usr command
                sendMessage("Welcome to Smart PID service. Please enter your email address using /usr command.", chatID);
            }
        } else if (message.startsWith("/user")) {
            String cmd_split[] = message.split(" ");
            if (cmd_split.length == 2) {
                //this is a deep linked registration. Get the unique_id and try to register the user
                String uniqueID = sqlConnection.isUserExists(cmd_split[1].trim());
                if ((uniqueID != null) && sqlConnection.isChatIDexists(chatID) == null) {
                    // if user exiting by UID, add to the telegram_bot and set inactive until we receive correct password
                    sqlConnection.setChatID(uniqueID, chatID, false);
                    sendMessage("Please enter your password using /pwd command.", chatID);
                } else {
                    //user name/email is invalid
                    sendMessage("Invalid user name. Please try again.", chatID);
                }
            } else if (cmd_split.length == 3) {
                if (cmd_split[2].equals("unsubscribe")) {
                    List<String> devices = sqlConnection.getDevicesWithChatID(chatID);
                    for (String device : devices) {
                        removeDevice(device, chatID, false);
                    }
                    if (sqlConnection.removeUser(chatID) != 0) {
                        sendMessage("You have successfully unsubscribed this user", chatID);
                    } else {
                        sendMessage("An error occured. Please contact your service provider", chatID);
                    }
                } else {
                    //invalid command
                    sendMessage("Invalid Command", chatID);
                }
            } else {
                //ignore and wait for /usr command
                sendMessage("Please enter your email address using /usr command.", chatID);
            }
        } else if (message.startsWith("/pwd")) {
            String cmd_split[] = message.trim().split(" ");
            if (cmd_split.length == 2) {
                String uniqueID = sqlConnection.isChatIDexists(chatID);
                if (uniqueID != null) {
                    //user has sent his email already and it's in the telegram_bot table
                    if (sqlConnection.isChatIDActive(chatID) == false) { // if chatID is not already active then, check the password
                        if (sqlConnection.isPasswordCorrect(uniqueID, cmd_split[1])) {
                            //password is correct
                            sqlConnection.activateChatID(chatID);
                            sendMessage("You are all set. You can use /serial to subscribe to your devices", chatID);
                        } else {
                            //password is incorrect. Please try agian.
                            sendMessage("Please enter correct password", chatID);
                        }
                    } else {
                        //user already successfully subscribed to the bot
                        sendMessage("Nothing to be done. You are already subscribed", chatID);
                    }
                } else {
                    //user need to send his user id/email first.
                    sendMessage("Please enter your email address using /usr command first.", chatID);
                }
            } else {
                //ignore and wait for /pwd command
                sendMessage("Please enter your password using /pwd command.", chatID);
            }

        } else if (message.startsWith("/serial")) {
            String cmd_split[] = message.trim().split(" ");
            if (cmd_split.length == 2) {
                if (sqlConnection.isChatIDActive(chatID)) {
                    //subscribe to mqtt channel
                    if (sqlConnection.isSerialExists(cmd_split[1])) {
                        //serial existing. subscribe to the device topic.

                        if (sqlConnection.setChatIDForSerial(cmd_split[1], chatID, sqlConnection.getUserIDbyChatID(chatID)) != 0) {
                            //_mqttClient.subscribe("smartpidM5/mini/"+cmd_split[1]+"/dynamic");
                            //sqlConnection.setAlarmForSerialandChatID(cmd_split[1], chatID, true);
                            sendMessage("You have registered the device. Use /alarm on to turn on Alarms", chatID);
                        } else {
                            sendMessage("Device not found or you are not registered.Please try again.", chatID);
                        }

                    } else {
                        sendMessage("Device not found", chatID);
                    }
                } else {
                    //user has not successfully subscribed
                    sendMessage("First you need to subscribe to the service using username and password", chatID);
                }
            } else if (cmd_split.length == 3) {
                //remove device command
                if (cmd_split[2].equals("off")) {
                    removeDevice(cmd_split[1], chatID, true);

                }

            } else {
                sendMessage("Please enter your device serial using /serial command.", chatID);
            }
        } else if (message.startsWith("/alarm")) {
            String cmd_split[] = message.trim().split(" ");
            if (cmd_split.length == 2) {
                if (cmd_split[1].equals("on")) {
                    List<String> devices = sqlConnection.getDevicesWithChatID(chatID);
                    String messageToSend = "You have successfully activated alarms for ";

                    for (String device : devices) {
                        String deviceHash = SerialNoHash(device);
                        sqlConnection.setAlarmForSerialandChatID(device, chatID, true, deviceHash);
                        try {
                            _mqttClient.subscribe("smartpidM5/mini/" + deviceHash + "/events/standard");
                            _mqttClient.subscribe("smartpidM5/mini/" + deviceHash + "/events/advanced");
                        } catch (MqttException ex) {
                            Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
                            sendMessage("An error occured. Please contact your service provider", chatID);
                        }
                        messageToSend += device + " , ";
                    }
                    sendMessage(messageToSend, chatID);
                } else if (cmd_split[1].equals("off")) {
                    List<String> devices = sqlConnection.getDevicesWithChatID(chatID);
                    String messageToSend = "You have successfully deactivated alarms for ";
                    for (String device : devices) {
                        String deviceHash = SerialNoHash(device);
                        sqlConnection.setAlarmForSerialandChatID(device, chatID, false, deviceHash);
                        if (sqlConnection.isGoodtoUnsubscribeMQTT(device)) {
                            try {
                                _mqttClient.unsubscribe("smartpidM5/mini/" + deviceHash + "/events/standard");
                                _mqttClient.unsubscribe("smartpidM5/mini/" + deviceHash + "/events/advanced");
                            } catch (MqttException ex) {
                                Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
                                sendMessage("An error occured. Please contact your service provider", chatID);
                            }
                        }
                        messageToSend += device + " , ";
                    }
                    sendMessage(messageToSend, chatID);
                } else {
                    sendMessage("Incorrect command for alram. Please check your command", chatID);
                }
            } else {
                sendMessage("Incorrect command for alram. Please check your command", chatID);
            }
        } else if (message.startsWith("/data")) {
            String cmd_split[] = message.trim().split(" ");
            if (cmd_split.length == 2) {
                List<String> devices = sqlConnection.getDevicesWithChatID(chatID);
                if (devices.contains(cmd_split[1])) {
                    new Thread(new dynamicCMDHandler(cmd_split[1], chatID)).start();
                }
            } else if (cmd_split.length == 1) {
                List<String> devices = sqlConnection.getDevicesWithChatID(chatID);
                for (String device : devices) {
                    new Thread(new dynamicCMDHandler(device, chatID)).start();
                }
            } else {
                sendMessage("Incorrec command for data. Please check your command", chatID);
            }
        } else if (message.startsWith("/help")) {
            String messagetoSend = "Available Commands are\n";
            messagetoSend += "/start - Will help you get started\n";
            messagetoSend += "/user - Use this along with username, ex to Subscribe /user myusername, to unsubscribe /user myusername unsibscribe\n";
            messagetoSend += "/pwd - To Enter password\n";
            messagetoSend += "/serial - To enable/disable a device. Ex to enable /serial deviceid, to disable /serial deviceid off\n";
            messagetoSend += "/alarm - To enable/disable alarms for enabled devices. Ex to enable /alarm on, to disable /alamrm off\n";
            messagetoSend += "/data - To display current device status. Ex to get status of all enabled devices use /data.\n"
                    + "To get status of specific device, use /data deviceid\n";
            messagetoSend += "/help - Display this help\n";
            sendMessage(messagetoSend, chatID);
        } else {
            sendMessage("Wrong Command", chatID);

        }
    }

    public void sendMessage(String message, Long chatID) {
        SendMessage sendback = new SendMessage();

        sendback.setText(message);
        sendback.setChatId(chatID);
        try {
            execute(sendback);
        } catch (TelegramApiException ex) {
            Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeDevice(String deviceID, Long chatID, boolean sendMsg) {
        if (sqlConnection.isChatIDActive(chatID)) {
            //chat ID is active
            if (sqlConnection.isSerialExists(deviceID)) {
                String deviceHash = SerialNoHash(deviceID);
                //remove alarms for the device first
                if (sqlConnection.setAlarmForSerialandChatID(deviceID, chatID, false, deviceHash) != 0) {
                    //set chatID for that serial  as 0
                    if (sqlConnection.setChatIDForSerial(deviceID, 0, sqlConnection.getUserIDbyChatID(chatID)) != 0) {
                        if (sqlConnection.isGoodtoUnsubscribeMQTT(deviceID)) {
                            try {
                                _mqttClient.unsubscribe("smartpidM5/mini/" + deviceHash + "/events/standard");
                                _mqttClient.unsubscribe("smartpidM5/mini/" + deviceHash + "/events/advanced");

                            } catch (MqttException ex) {
                                Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
                                sendMessage("An error occured. Please contact your service provider", chatID);
                            }
                        }
                        if (sendMsg) {
                            sendMessage("You have successfully removed device " + deviceID, chatID);
                        }
                    } else {
                        sendMessage("An error occured. Please contact your service provider", chatID);
                    }
                } else {
                    sendMessage("An error occured. Please contact your service provider", chatID);
                }
            } else {
                sendMessage("Device Not found", chatID);
            }
        } else {
            sendMessage("You have not subscribed to use this service. Please subsribe first", chatID);
        }
    }

    public void setMqttClient(MqttListner client) {
        if (client != null) {
            _mqttClient = client;
            List<String> activeDeviceList = sqlConnection.getAllDevicesWithAlarm();
            System.out.println(activeDeviceList.toString());
            for (String device : activeDeviceList) {
                try {
                    _mqttClient.subscribe("smartpidM5/mini/" + SerialNoHash(device) + "/events/standard");
                    _mqttClient.subscribe("smartpidM5/mini/" + SerialNoHash(device) + "/events/advanced");
                } catch (MqttException ex) {
                    Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

    byte bitOrderInvert(byte bytein) {
        byte invertedByte = 0;
        int i;

        for (i = 0; i < 4; i++) {
            invertedByte |= (bytein & (1 << i)) << (7 - 2 * i);
        }
        for (i = 4; i < 8; i++) {
            invertedByte |= (bytein & (1 << i)) >> (2 * i - 7);
        }
        return invertedByte;
    }

    public String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String SerialNoHash(String serialNo) {
        byte[] serNo = hexToByteData(serialNo);
        byte[] scrambledSerNo = new byte[7];

        scrambledSerNo[0] = (byte) (bitOrderInvert(serNo[6 - 1]) ^ 0x6E);
        scrambledSerNo[1] = (byte) (bitOrderInvert(serNo[1 - 1]) ^ 0x14);
        scrambledSerNo[2] = (byte) (bitOrderInvert(serNo[3 - 1]) ^ 0xDE);
        scrambledSerNo[3] = (byte) (bitOrderInvert(serNo[2 - 1]) ^ 0xE5);
        scrambledSerNo[4] = (byte) (bitOrderInvert(serNo[5 - 1]) ^ 0xAF);
        scrambledSerNo[5] = (byte) (bitOrderInvert(serNo[7 - 1]) ^ 0x30);
        scrambledSerNo[6] = (byte) (bitOrderInvert(serNo[4 - 1]) ^ 0x04);
        return bytesToHex(scrambledSerNo);
    }

    private byte[] hexToByteData(String hex) {
        byte[] convertedByteArray = new byte[hex.length() / 2];
        int count = 0;

        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output;
            output = hex.substring(i, (i + 2));
            int decimal = (int) (Integer.parseInt(output, 16));
            convertedByteArray[count] = (byte) (decimal & 0xFF);
            count++;
        }
        return convertedByteArray;
    }

    private class dynamicCMDHandler implements Runnable, MqttCallback, IMqttActionListener {

        protected String deviceID;
        protected Long dynamicchatID;

        MqttAsyncClient mqttClient_dynamic;

        private boolean statusReceived;
        private int counter;

        protected dynamicCMDHandler(String device, Long chatID) {
            deviceID = device;
            dynamicchatID = chatID;
            statusReceived = false;
            counter = 0;
            try {
                mqttClient_dynamic = new MqttAsyncClient(MqttVars.MQTT_HOST, MqttVars.MQTT_CLIENT_ID + SerialNoHash(deviceID));
                MqttConnectOptions connOpts = setUpConnectionOptions(MqttVars.MQTT_USER, MqttVars.MQTT_PASSWORD);
                mqttClient_dynamic.connect(connOpts);
                while (mqttClient_dynamic.isConnected() == false) {

                }
                mqttClient_dynamic.setCallback(this);
                try {
                    mqttClient_dynamic.subscribe("smartpidM5/mini/" + SerialNoHash(deviceID) + "/dynamic", 0);
                    MqttMessage message = new MqttMessage();
                    message.setPayload("{}".getBytes());
                    mqttClient_dynamic.publish("smartpidM5/mini/" + SerialNoHash(deviceID) + "/dynamic", message);
                } catch (MqttException ex) {
                    Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (MqttException ex) {
                Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private MqttConnectOptions setUpConnectionOptions(String username, String password) {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            return connOpts;
        }

        @Override
        public void run() {
            synchronized (this) {
                while (statusReceived == false) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            try {
                mqttClient_dynamic.unsubscribe("smartpidM5/mini/" + SerialNoHash(deviceID) + "/dynamic");
                mqttClient_dynamic.disconnect();
            } catch (MqttException ex) {
                Logger.getLogger(TelegramBot.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Done");

        }

        @Override
        public void connectionLost(Throwable thrwbl) {

        }

        @Override
        public void messageArrived(String string, MqttMessage mm) throws Exception {
            counter++;

            if (counter == 2) {
                JSONObject obj = new JSONObject(new String(mm.getPayload()));
                /*
            {
                "time": <timestamp>,
                "SP": <temp>, // only in heating/cooling/thermostatic mode
                "temp": <temp>, // only in heating/cooling/thermostatic mode
                "unit": <unit>, // only in heating/cooling/thermostatic mode
                "RH SP": <rh>, // only in humidify/dehumidify/hygrostatic mode
                "humidity": <rh>, // only in humidify/dehumidify/hygrostatic mode
                "mode": <mode>,
                "relay": <relay>,
                "runmode" :<run>
            }
            
                 */
                String messageToSend = "";
                messageToSend = "Device " + deviceID + "\nTime:";
                messageToSend += obj.optString("time");
                if (obj.optString("SP").isEmpty() == false) {
                    messageToSend += "\nSP : " + obj.optString("SP");
                }
                if (obj.optString("temp").isEmpty() == false) {
                    messageToSend += "\ntemp : " + obj.optString("temp");
                }
                if (obj.optString("unit").isEmpty() == false) {
                    messageToSend += "\nunit : " + obj.optString("unit");
                }
                if (obj.optString("RH SP").isEmpty() == false) {
                    messageToSend += "\nRH SP : " + obj.optString("RH SP");
                }
                if (obj.optString("humidity").isEmpty() == false) {
                    messageToSend += "\nhumidity : " + obj.optString("humidity");
                }
                if (obj.optString("mode").isEmpty() == false) {
                    messageToSend += "\nmode : " + obj.optString("mode");
                }
                if (obj.optString("relay").isEmpty() == false) {
                    messageToSend += "\nrelay : " + obj.optString("relay");
                }
                if (obj.optString("runmode").isEmpty() == false) {
                    messageToSend += "\nrunmode : " + obj.optString("runmode");
                }

                sendMessage(messageToSend, dynamicchatID);
                statusReceived = true;

            }
            synchronized (this) {
                notify();
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {

        }

        @Override
        public void onSuccess(IMqttToken imt) {
            System.out.println("Connected " + mqttClient_dynamic.getClientId());

        }

        @Override
        public void onFailure(IMqttToken imt, Throwable thrwbl) {

        }

    }

}
