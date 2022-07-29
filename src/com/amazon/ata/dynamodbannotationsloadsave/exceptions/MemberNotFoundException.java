package com.amazon.ata.dynamodbannotationsloadsave.exceptions;

public class MemberNotFoundException extends Exception{
    private static final long serialVersionUID = -1378588467028195764L;

    public MemberNotFoundException(){
        super();
    }

    public MemberNotFoundException(String message){
        super(message);
    }

    public MemberNotFoundException(Throwable cause){
        super(cause);
    }

    public MemberNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
