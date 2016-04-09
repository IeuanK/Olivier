package dev.pgtm;

import java.io.OutputStream;
import java.io.PrintStream;

class Interceptor extends PrintStream
{
    public Interceptor(OutputStream out)
    {
        super(out, true);
    }
    @Override
    public void print(String s)
    {//do what ever you like
    	//NetworkLib.getURLContents("http://dev.ruun.nl/bot/bot_log.php?d=" + URLEncoder.encode(s));
        super.print(s);
    }
}