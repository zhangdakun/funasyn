// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package cn.eben.android.util;


public class SmsMessage
{
    private String MA_TYPE="";
    private String MESSAGE_TYPE="";
    private String TEL="";
    private String VBODY="";
    private String VTIME="";

    public String getMA_TYPE()
    {
        return MA_TYPE;
    }

    public String getMESSAGE_TYPE()
    {
        return MESSAGE_TYPE;
    }

    public String getTEL()
    {
        return TEL;
    }

    public String getVBODY()
    {
        return VBODY;
    }

    public String getVTIME()
    {
        return VTIME;
    }

    public void setMA_TYPE(String s)
    {
        MA_TYPE = s;
    }

    public void setMESSAGE_TYPE(String s)
    {
        MESSAGE_TYPE = s;
    }

    public void setTEL(String s)
    {
        TEL = s;
    }

    public void setVBODY(String s)
    {
        VBODY = s;
    }

    public void setVTIME(String s)
    {
        VTIME = s;
    }


}
