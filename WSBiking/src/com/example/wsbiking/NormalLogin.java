package com.example.wsbiking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.facebook.Session;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NormalLogin extends Activity implements android.view.View.OnClickListener {

	EditText etUser, etPass;
	//Button bLogin;
	ImageView bLogin;
	
	//strings to save username and password
	String username, password;
	
	//http client as form container
	HttpClient httpclient;
	
	//use http post method
	HttpPost httppost;
	
	//create arraylist for input data
	ArrayList<NameValuePair> nameValuePairs;
	
	//create htpp response and entity
	HttpResponse response;
	HttpEntity entity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_normal_login);
		
		initialize();
	}

	private void initialize() {
		// TODO Auto-generated method stub
		etUser = (EditText) findViewById(R.id.etUser);
		etPass = (EditText) findViewById(R.id.etPass);
		bLogin = (ImageView) findViewById(R.id.bSubmit);
		
		Log.i("pratik","inside initialize");
		bLogin.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.normal_login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
		httpclient = new DefaultHttpClient();
		
		//create a new http post with url to php file as param
		httppost = new HttpPost("http://10.0.2.2/android/index.php");
		
		Log.i("pratik","connection done");
		//assign input text to strings
		username = etUser.getText().toString();
		password = etPass.getText().toString();
			
		new longOperation().execute("");
		
		Log.i("pratik", "hello" + Main.isLogin);
		if(Main.isLogin) {
			callHome();
		}
	}
	
	public void callHome()
	{
		Intent intent = new Intent(this,RecordActivity.class);
		intent.putExtra("com.login.username", username);
		startActivity(intent);
	}
	
	 private static String convertStreamToString(InputStream is) {
	        /*
	         * To convert the InputStream to String we use the BufferedReader.readLine()
	         * method. We iterate until the BufferedReader return null which means
	         * there's no more data to read. Each line will appended to a StringBuilder
	         * and returned as String.
	         */
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        StringBuilder sb = new StringBuilder();
	 
	        String line = null;
	        try {
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                is.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return sb.toString();
	    }//End of convertStreamToString
	 
	 private class longOperation extends AsyncTask<String, Void, Long>
	 {

			@Override
			protected Long doInBackground(String... params) {
				// TODO Auto-generated method stub
				long result = -1;
				try {
					
					//create new arraylist
					nameValuePairs = new ArrayList<NameValuePair>();
					
					//place them in arraylist
					nameValuePairs.add(new BasicNameValuePair("username", username));
					nameValuePairs.add(new BasicNameValuePair("password", password));
					
					Log.i("pratik","inside try");
					//add array to http post
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					
					Log.i("pratik","urlcrap");
					//assign executed  from container to response
					response = httpclient.execute(httppost);
					
					Log.i("pratik","responsecrap");
					//need to check if status code is 200
					if(response.getStatusLine().getStatusCode() == 200)
					{
					
						Log.i("pratik","inside status loop");
						
						
						//assign response entity to http entity
						entity = response.getEntity();
						
						//check if entity is non null
						if(entity != null) {
							
							//create a new input stream with received data
							InputStream instream = entity.getContent();
							
							//create a new json object and assign converted data as params
							JSONObject jsonresponse = new JSONObject(convertStreamToString(instream));
							
							//assign json responses to local var
							String retUser = jsonresponse.getString("user"); //mysql field
							String retPass = jsonresponse.getString("pass");
							
							//validate login credentials
							if(username.equals(retUser) && password.equals(retPass)) {
							//if(username.equals("pratik") && password.equals("qwerty")) {
								
								//create a new shared preference  by getting the preference
								//give the shared preference nay name you like
								SharedPreferences sp = getSharedPreferences("logindetails", 0);
															
								//edit shared preference
								SharedPreferences.Editor spedit  = sp.edit();
								
								//put login details as strings
								spedit.putString("user", username);
								spedit.putString("pass", password);
								
								//close the editor
								spedit.commit();
								Log.i("pratik","before toast");
								//display toast
								//Toast.makeText(getBaseContext(), "Success", Toast.LENGTH_SHORT).show();
								result = 0;
								
							} else {
								//Display message
								//Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT).show();
								result = 1;
							}
						}
							
					}
					
				} catch(Exception e) {
					Log.i("pratik",e.toString());
					//Toast.makeText(getBaseContext(), "Connected failed!", Toast.LENGTH_SHORT).show();
					result = 2;
				}
				return result;
			}

			protected void onPostExecute(Long result)
			{
				switch(result.intValue()) {
				case 0:
					Toast.makeText(getBaseContext(), "Success", Toast.LENGTH_SHORT).show();
					Main.isLogin = true;
					callHome();
					break;
				case 1:
					Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT).show();
					Main.isLogin = false;
					break;
				case 2:
					Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT).show();
					Main.isLogin = false;
					break;
				}
				
			}
	 }
	 
/*		@Override
		public void onBackPressed() {
			Main.isLogin = false;
			Session session = Session.getActiveSession();
			if (!session.isClosed()) {
	            session.closeAndClearTokenInformation();
	        }
			callMain();
		
		}*/
		public void callMain()
		{
			Intent intent = new Intent(this,Main.class);
			
			startActivity(intent);
		}
		
}
