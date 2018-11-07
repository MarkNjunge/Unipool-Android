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
3. Create a Firebase project and enable Google as a sign in method. You will need to provide the SHA-1 hash for your keystore.
4. Go to the [Google Cloud Developer console](https://console.cloud.google.com/apis/library) and 
enable **Maps SDK for Android**, **Places SDK for Android** and **Directions API** for the project.  
Note that you will need to set up billing for Maps SDK for Android to work.
6. Add a `keys.properties` file to the root directory containing the following details.  
```
GOOGLE_MAPS_API_KEY=AIza...
REQUEST_TOKEN_ID=***.apps.googleusercontent.com
MQTT_BROKER=mqtt://mqtt.example.com
```
- You can get the `GOOGLE_MAPS_API_KEY` and `REQUEST_TOKEN_ID` from the [credentials page]- (https://console.cloud.google.com/apis/credentials).  
- The `GOOGLE_MAPS_API_KEY` is any of the API keys.  
- For the `REQUEST_TOKEN_ID` it **needs** to be and OAuth 2.0 client ID of type **Web application***, not **Android**.  
- For an MQTT broker you can host your own instance of [my sample broker](https://github.com/MarkNjunge/mqtt-broker), [RabbitMQ](https://www.rabbitmq.com) or any MQTT broker provider.  
6. Open and build the project in Android Studio (3.0+). 

# Screenshots
### Rider
![alt text](/images/rider.png)

### Driver
![alt text](/images/driver.png)

# Demo
[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/pY5Utpqnvd8/0.jpg)](https://www.youtube.com/watch?v=pY5Utpqnvd8)]
