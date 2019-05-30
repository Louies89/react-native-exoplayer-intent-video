package com.herudi.exovideo;
import com.facebook.react.bridge.Callback;

public class RegisterCallBack {
	 private static Callback errorCallback;

   public void setCallBack(Callback callback) {
		this.errorCallback = callback;
   }

   public void invokeCallBack(String error,String url) {
		 if(this.errorCallback!=null){
			 this.errorCallback.invoke(error,url);
			 this.errorCallback = null;
		 }
   }
}