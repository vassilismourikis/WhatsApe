package com.example.whatsape;

import java.io.Serializable;

public class TextValue extends Value implements Serializable {
    private String message;

    public TextValue(String name,String text){
        super(name);
        this.message=text;
    }

    @Override
    public String getMessage(){
        return message;
    }
}