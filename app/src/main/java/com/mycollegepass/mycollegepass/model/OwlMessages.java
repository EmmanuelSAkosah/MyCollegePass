package com.mycollegepass.mycollegepass.model;

import java.util.Random;

public class OwlMessages {

   static String[] messages = {"You are amazing",
            "Owl! what a Lovely day!",
            "Donut worry, my dear one",
            "Alright Homie, enjoy some sweet discounts",
            "Save on purchases, the Smart Choice :)"
    };
    public static String getRandomMessage(){
        Random random = new Random();
        int index = (int)(Math.random()*(messages.length)) ;
        String message = messages[index];
        return message;
    }

}
