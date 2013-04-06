package com.example.wsbiking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.facebook.Session;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SignUp extends Activity implements OnClickListener {

	EditText username, password, cpassword;
	ImageView signup;
	
	//strings to save username and password
	String uname, passwd, cpasswd;
	
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
		setContentView(R.layout.activity_sign_up);
		
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		cpassword = (EditText) findViewById(R.id.cpassword);
		signup = (ImageView) findViewById(R.id.signup);
		
		signup.setOnClickListener(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		httpclient = new DefaultHttpClient();
		
		//create a new http post with url to php file as param
		httppost = new HttpPost("http://10.0.2.2/android/signup.php");
		
		Log.i("pratik","connection done");
		//assign input text to strings
		uname = username.getText().toString();
		passwd = password.getText().toString();
		cpasswd = cpassword.getText().toString();
		
		if(uname.isEmpty() || passwd.isEmpty() || cpasswd.isEmpty()) {
			Toast.makeText(getBaseContext(), "Fields cannot be empty !", Toast.LENGTH_SHORT).show();
		}
		if(passwd.equals(cpasswd)) {
			new longOperation().execute("");
		} else {
			Toast.makeText(getBaseContext(), "Passwords do not match !", Toast.LENGTH_SHORT).show();
			password.setText("");
			cpassword.setText("");
		}
		
	}
	
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
					nameValuePairs.add(new BasicNameValuePair("uname", uname));
					nameValuePairs.add(new BasicNameValuePair("passwd", passwd));
					
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
							if(jsonresponse.has("ERROR")) {
								String error = jsonresponse.getString("ERROR");
								Log.i("pratik", error.toString());	
								if(error.contains("Duplicate")) {
									result = 3;
								}
							} else {
													
								String retUser = jsonresponse.getString("user"); //mysql field
								String retPass = jsonresponse.getString("pass");
							
								//validate login credentials
								if(uname.equals(retUser) && passwd.equals(retPass)) {
									result = 0;
								
								} else {
									//Display message
									//Toast.makeText(getBaseContext(), "Invalid username and/or password", Toast.LENGTH_SHORT).show();
									result = 1;
								}
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
					Toast.makeText(getBaseContext(), "SignUp successful. Please login", Toast.LENGTH_SHORT).show();
					callMain();
					break;
				case 1:
					Toast.makeText(getBaseContext(), "Something went wrong! Please enter again", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(getBaseContext(), "Connection Failed! Please try again", Toast.LENGTH_SHORT).show();
					break;
				case 3:
					Toast.makeText(getBaseContext(), "Username already exists! Please choose a different one", Toast.LENGTH_SHORT).show();
					break;
				}
				
			}
	 }

		public void callMain()
		{
			Intent intent = new Intent(this,Main.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	 
	/*	@Override
		public void onBackPressed() {
			Main.isLogin = false;
			Session session = Session.getActiveSession();
			if (!session.isClosed()) {
	            session.closeAndClearTokenInformation();
	        }
			callMain();
		
		}*/
}
