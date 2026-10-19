package com.mosaiccast.tv;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONObject;
public class MainActivity extends Activity {
 WebView web; ServerSocket server; volatile boolean active=true;
 final String pin=String.format(Locale.US,"%06d",new SecureRandom().nextInt(1000000));
 volatile String id="",offer="",answer=""; volatile long created=0;
 final ExecutorService pool=Executors.newCachedThreadPool();
 static final int PORT=8765; boolean okDown=false,okLong=false;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_FULLSCREEN);
  web=new WebView(this); web.setBackgroundColor(0xff050a13);
  web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setMediaPlaybackRequiresUserGesture(false);
  web.setWebViewClient(new WebViewClient());
  web.setWebChromeClient(new WebChromeClient(){@Override public void onPermissionRequest(PermissionRequest p){runOnUiThread(()->p.grant(p.getResources()));}});
  web.addJavascriptInterface(new Object(){
   @JavascriptInterface public String ip(){return localIp();}
   @JavascriptInterface public String pin(){return pin;}
  },"TVNative");
  setContentView(web);web.requestFocus();
  pool.execute(()->{try{server=new ServerSocket(PORT);while(active){Socket s=server.accept();pool.execute(()->handle(s));}}catch(Exception e){android.util.Log.e("MosaicCast","Server",e);}});
  web.loadUrl("http://127.0.0.1:"+PORT+"/");
 }
 String localIp(){try{Enumeration<NetworkInterface> all=NetworkInterface.getNetworkInterfaces();while(all.hasMoreElements()){NetworkInterface n=all.nextElement();if(!n.isUp()||n.isLoopback())continue;Enumeration<InetAddress> a=n.getInetAddresses();while(a.hasMoreElements()){InetAddress x=a.nextElement();if(x instanceof Inet4Address&&x.isSiteLocalAddress())return x.getHostAddress();}}}catch(Exception ignored){}return "Check Wi-Fi";}
 String line(InputStream in)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();int c;while((c=in.read())>=0){if(c==10)break;if(c!=13)b.write(c);if(b.size()>8192)throw new IOException("Request too large");}return b.toString("UTF-8");}
 void reply(OutputStream out,int code,String type,String value)throws IOException{byte[] data=value.getBytes(StandardCharsets.UTF_8);String h="HTTP/1.1 "+code+" "+(code==200?"OK":"Error")+"\r\nContent-Type: "+type+"\r\nAccess-Control-Allow-Origin: *\r\nAccess-Control-Allow-Headers: Content-Type\r\nAccess-Control-Allow-Methods: GET, POST, OPTIONS\r\nContent-Length: "+data.length+"\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n";out.write(h.getBytes(StandardCharsets.UTF_8));out.write(data);}
 void handle(Socket s){try(Socket sock=s){sock.setSoTimeout(12000);InputStream in=sock.getInputStream();OutputStream out=sock.getOutputStream();
  String[] req=line(in).split(" ");if(req.length<2)return;String method=req[0],path=req[1];int size=0;
  for(int i=0;i<64;i++){String h=line(in);if(h.isEmpty())break;if(h.toLowerCase(Locale.US).startsWith("content-length:"))size=Integer.parseInt(h.substring(15).trim());}
  if(method.equals("OPTIONS")){reply(out,200,"application/json","{}");return;}
  if(size<0||size>1500000){reply(out,413,"application/json","{}");return;}
  byte[] bytes=new byte[size];int n=0;while(n<size){int k=in.read(bytes,n,size-n);if(k<0)break;n+=k;}
  JSONObject obj=null;if(method.equals("POST"))obj=new JSONObject(new String(bytes,0,n,StandardCharsets.UTF_8));
  boolean loop=sock.getInetAddress().isLoopbackAddress();
  if(method.equals("GET")&&path.equals("/")){try(InputStream f=getAssets().open("receiver.html")){ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[8192];int m;while((m=f.read(buf))>0)b.write(buf,0,m);reply(out,200,"text/html; charset=utf-8",b.toString("UTF-8"));}return;}
  if(method.equals("GET")&&path.equals("/api/status")){reply(out,200,"application/json","{\"ready\":true}");return;}
  if(method.equals("POST")&&path.equals("/api/offer")){if(!pin.equals(obj.optString("pin"))){reply(out,403,"application/json","{\"error\":\"Wrong pairing PIN\"}");return;}String sdp=obj.optString("sdp");if(sdp.length()<50){reply(out,400,"application/json","{}");return;}id=UUID.randomUUID().toString();offer=sdp;answer="";created=System.currentTimeMillis();reply(out,200,"application/json",new JSONObject().put("id",id).toString());return;}
  if(method.equals("GET")&&path.equals("/api/next")&&loop){reply(out,200,"application/json",offer.isEmpty()||System.currentTimeMillis()-created>120000?"{}":new JSONObject().put("id",id).put("sdp",offer).toString());return;}
  if(method.equals("POST")&&path.equals("/api/answer")&&loop){if(!id.equals(obj.optString("id"))){reply(out,403,"application/json","{}");return;}answer=obj.optString("sdp");reply(out,200,"application/json","{\"ok\":true}");return;}
  if(method.equals("GET")&&path.startsWith("/api/result?id=")){String key=URLDecoder.decode(path.substring(15),"UTF-8");if(!id.equals(key)){reply(out,404,"application/json","{}");return;}reply(out,200,"application/json",answer.isEmpty()?"{}":new JSONObject().put("sdp",answer).toString());return;}
  reply(out,404,"application/json","{}");
 }catch(Exception e){android.util.Log.w("MosaicCast","HTTP",e);}}
 void send(String key){if(web!=null)web.evaluateJavascript("window.MosaicReceiver&&window.MosaicReceiver.onKey("+JSONObject.quote(key)+")",null);}
 @Override public boolean dispatchKeyEvent(KeyEvent e){int c=e.getKeyCode();if(c==KeyEvent.KEYCODE_DPAD_CENTER||c==KeyEvent.KEYCODE_ENTER){if(e.getAction()==KeyEvent.ACTION_DOWN){if(!okDown){okDown=true;okLong=false;}else if(e.getRepeatCount()>0&&!okLong){okLong=true;send("LONG_OK");}}else if(e.getAction()==KeyEvent.ACTION_UP){if(okDown&&!okLong)send("OK");okDown=false;}return true;}
  if(e.getAction()==KeyEvent.ACTION_DOWN){String k=null;switch(c){case KeyEvent.KEYCODE_DPAD_UP:k="UP";break;case KeyEvent.KEYCODE_DPAD_DOWN:k="DOWN";break;case KeyEvent.KEYCODE_DPAD_LEFT:k="LEFT";break;case KeyEvent.KEYCODE_DPAD_RIGHT:k="RIGHT";break;case KeyEvent.KEYCODE_BACK:k="BACK";break;case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:k="PLAY";break;case KeyEvent.KEYCODE_MENU:k="MIX";break;}if(k!=null){send(k);return true;}}return super.dispatchKeyEvent(e);}
 @Override protected void onDestroy(){active=false;try{server.close();}catch(Exception ignored){}pool.shutdownNow();if(web!=null)web.destroy();super.onDestroy();}
}
