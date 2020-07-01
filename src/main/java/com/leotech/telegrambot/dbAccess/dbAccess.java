/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.telegrambot.dbAccess;

/**
 *
 * @author Anusha
 */
public interface dbAccess {
    public String isUserExists(String userID);
    public boolean isPasswordCorrect(String userID,String password);
    public boolean setChatID(String userID,long chatID,boolean active);
    public boolean isSerialExists(String serialID);
    
    
}
