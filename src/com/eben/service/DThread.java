package com.eben.service;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;

public class DThread extends Thread{
	  private int a = 3999;
	  private ThreadPoolExecutor b;
	  private boolean c = false;
	  private ServerSocket d;
	  private final Context e;


	  public DThread(Context paramContext, int paramInt)
	  {
	    this.a = paramInt;
	    this.e = paramContext;

	  }

	  public void b()
	  {
	    if (this.c)
	      return;
	    this.c = true;
	    try
	    {
	      if (this.d != null)
	        this.d.close();
	      join(3000L);

	    }
	    catch (Exception localException)
	    {
//	      while (true)
	    	

	    }
	  }

	  public void run()
	  {
//	    aaz localaaz;
	    try
	    {
	      this.c = false;
	      this.d = new ServerSocket();
	      this.d.bind(new InetSocketAddress("127.0.0.1", this.a));
	      this.d.setReuseAddress(true);
	      this.d.setPerformancePreferences(100, 100, 1);
	      this.b = new ThreadPoolExecutor(10, 100, 60000L, TimeUnit.MICROSECONDS, new ArrayBlockingQueue(10), new ThreadPoolExecutor.CallerRunsPolicy());
//	      if (this.c)
//	        break label248;
	      Socket localSocket = this.d.accept();
	      localSocket.setPerformancePreferences(10, 100, 1);
	      localSocket.setKeepAlive(true);
	      localSocket.setSoLinger(true, 30);
//	      label201: label248: localaaz = new aaz(this, localSocket);
	    }
	    catch (Throwable localThrowable)
	    {
	      if (this.b != null); 
	    }
	    finally
	    {
	      try
	      {
	        this.b.shutdown();
	        this.b = null;
	      } catch(Exception e) {
	    	  e.printStackTrace();
	      }

	    }
	  }
}
