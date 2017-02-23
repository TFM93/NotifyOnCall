# NotifyOnCall
Simple Android app to send udp package with the nearest beacon id when an call is answered


The purpose of this application is to send a signal to the network when the user is viewing a video and simultaneously answers a call. This signal contains an ID of the nearest viewing device, like a TV.
To achieve this goal, we designed an Android application that reads bluetooth signals from beacon devices, constantly checks the nearest one, updates the UI and upon a call is received sends an UDP packet to the network.

![alt tag](https://drive.google.com/file/d/0B_aCjcINiHMBdlV0MVRia2ZTQ1U/view?usp=sharing)

Fig 1- mobile application architecture

Upon the first time the application is opened, a broadcast receiver is created and is expecting to receive intents signaling telephony advices from the Android system (1) or advices from the Beacon Service (3). The Beacon Service is launched by the NotifyOnCall main activity (2) and it relies on the Android Beacon Library to read the bluetooth signals that are sent by the Beacons in range. The power of the received signal is the basis of the distance estimations between the mobile device and the beacon. When the broadcast receiver is signaled with an update, updates his internal information and if the app is not running on background in that time, updates the main activity (4). When the broadcast receiver detects an answered incoming call and if there is, at least, one beacon in range, one UDP package is sent to the network signaling the ID of the nearest beacon(5).

This application uses Android-Beacon-Library, source code available at: https://github.com/AltBeacon/android-beacon-library
