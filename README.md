![alt text](/images/icon.png)

 # Unipool

Unipool is an carpooling app for Android developed to allow members of Strathmore to have an easier and 
stress free means of coming to the institution.  

The project was submitted to the [Strathmore University](http://www.strathmore.edu/) Faculty of Information Technology as a second year project.

You can find the proposal and documentation in the `docs` folder.  

[Backend API](https://github.com/MarkNjunge/Unipool-backend)

Developed by [Mark Njung'e](https://github.com/MarkNjunge) and [Mecolela Sichangi](https://github.com/Sichangime)

**NB:** Unipool is not used in production.

# Installation
1. Clone the repository.
2. Add a `keystore.properties` file to the root directory containing the following details.
```
storePassword=your-keystore-password
keyPassword=your-keys-password
keyAlias=your-keys-alias
storeFile=path/to/your/keystore.jks
```
3. Modify the gradle.properties file to include your own`GOOGLE_MAPS_API_KEY, REQUEST_TOKEN_ID, MQTT_BROKER`. The provided keys are restricted.  
You can get the Google Maps Api key and request token id from [your credentials page](https://console.developers.google.com/apis/credentials) on the Google Developers Dashboard. Other guides: [1](https://developers.google.com/maps/documentation/android-api/signup#detailed-guides), [2](https://developers.google.com/identity/sign-in/android/start-integrating#get_your_backend_servers_oauth_20_client_id)  
For an MQTT broker you can host your own instance of [my sample broker](https://github.com/MarkNjunge/mqtt-broker), [RabbitMQ](https://www.rabbitmq.com) or any MQTT broker provider. 
4. Create a Firebase project and enable Google as a sign-in method. Note that you will need to provide a SHA1 fingerprint for the app.  
You get this from the Keystore file used to sign your app (The same one as specified in step 2). Here is a command to get the details of the default debug keystore details. `path\to\jdk\bin\keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android`
5. Go to the [Google Cloud Developer console](https://console.cloud.google.com/apis/library) and enable **Maps SDK for Android** and **Directions API**. Note that you will need to set up billing for Maps SDK for Android to work.
6. Open and build the project in Android Studio (3.0+). 

# Screenshots
### Rider
![alt text](/images/rider.png)

### Driver
![alt text](/images/driver.png)

# Demo
[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/pY5Utpqnvd8/0.jpg)](https://www.youtube.com/watch?v=pY5Utpqnvd8)]
