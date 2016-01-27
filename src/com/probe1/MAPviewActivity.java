package com.probe1;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MAPviewActivity extends Activity {

	  protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.map);
		    WebView webView = (WebView) findViewById(R.id.webView1);
		    webView.getSettings().setJavaScriptEnabled(true);	    
		    webView.setWebViewClient(new WebViewClient());
		    webView.loadUrl("http://telecontrol.net.in/gps/markerexample.php");
		    //webView.addJavascriptInterface(new JavaScriptInterface(), "android");
		    //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		    
		    
		    
		    
		  }
	
	
}
 
 
 


