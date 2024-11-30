Interrogating Entangled Instruments
===================================

Thank you for downloading this pack of sample code and, hopefully useful, information.

Sections in this document:
- Aims
- About the Entangled GameTrak System
- Viewing the data in a browser
- Recieveing OSC for Synthesis
- Other worlds than the defaultWorld
- Multi-layer Perceptron Worlds
- Defining your own worlds



Data Server 
====== ===

http://192.168.10.10:8000





Aims
====
This pack is to intended to 

- provde you with starter code to receive data fron the Entangled GameTrack system that is used at the workshop.
- provide some basic information about the systerm provded, how to set it up and select what data you want to receive


About the Entangled GameTrak System
==================================
This comprises of
- a GameTrack
- a RaspberryPi Zero
- a USB hub
- an ethernet cable
- a 5v UK power supply


Setting-up
----------
- plug the hub into the LEFT HAND (one nearest the HDMI) usb socket on the Pi Zero (the other is for power)
- connect the hub to your laptop with the ethernet cable
- plug the GameTrak into a USB soocket on the hub
- Plug in and turn on the power
- wait!


Starting-up and network configuration
-------------------------------------
The Pi Zero takes a few minutes to start (this can seem like for ever!).  It automatically loads the Entangled GameTrak software.  It also starts a DHCP server and will allocate your laptop the IP 192.168.10.101.

Check that you have your ethernet (or USB ethernet dongle) configured to DHCP.  This the default setting, so you will probably know if you have changed it.


About the data from the Entangled GameTrak System
-------------------------------------------------
The software on the Pi Zero takes the raw GameTrak data and processes it in different ways.  The processes that get performed are described in a configuration file (JSON format).  I often refer to this as a 'world' as it is based on structures and spaces in the real world. The system sends the data formatted as OpenSoundControl messages. 


'Core' Data
-----------
The Entangled system becomes engaged when BOTH of the GameTrak controllers are picked up. The system sends an "/engaged 1" message when this happens, and "/engaged 0" when the system is no longer engaged.

Whilst engaged the systenm will allways broadcast (around 20 times a second) data based around imagining a line between the two controllers:

/pitch {between 0 and 1.0} : the vertical angle of the line between the contriollers
/yaw {between 0 and 1.0}   : the horozontal rotation of the line over a full circle
/length float			   : the length of the line between the two controllers


The defaultWorld
----------------
The system automatically loads the 'defaultWorld' file on startup.  This defines a simple cubic hotspot (an active area) centered above the game track.  This area starts about 30cm up and continues up to 2m.  The cube extends 1m on either side and infront/behind.  This cubic hotpot has been called 'e' (for entangled but this is an arbitary name) and responds to the MID-POINT of the line between the two controllers.  

When the mid-point between the controllers enters or leaves the hotpsot then the system sends a "/e/active 1" or "/e/active 0" message.

The relative 3 dimensional position within the hotspot cube is also sent - 0 to 1.0 on each axis.  Note that the GameTrak System is orientated with the USB cable pointing away from you.  So the postition (0,0,0) is the bottom left nearest corner of the cube as you stand looking at the GameTrak.

e.g.
/rx 0.4	: the x axis runs from left to right
/ry 0.9 : the y axis is vertical
/rz 0.1 : the z axis runs from behind the GameTrak to infront

The defaultWorld is used in the Stickatron and the Eastiphone entangled instruments.



Viewing the data in a browser
=============================

Open a browser tab and navigate to 

http://192.168.10.10:8000 

You should see a very minimal text based webpage (sorry!).

You can also load a new world (see Other Worlds section below) or reload the current one (useful if you are editing your own world or want to generate random machine learning data - more later on that below.)

With the webpage open - pick up the controllers and you should see data and any OSC messages that are being sent.  This is reassuring.  Now it's time to move to your choice of synthesis software!



Recieveing OSC for Synthesis
============================

The PureData, MAX/MSP, VCVrack and SuperCollider folders all have a file called "defaultWorld".  This is your starting point.

PD and MAX/MSP
--------------
The examples use core objects so no further installation is required - just go and make music together!

VCVRack notes
-------------
You need to have the cvOSCcv modules installed from Trowasoft

SuperCollider notes
-------------------
There are several examples set up for you.  You can:

- The default port is 7400 - so note the "openPort" line (this port can be defined in the JSON)
- use raw OSCFunc (constantly)
- SCLang based : activate the provided OSCFuncs that save the incoming data into a global dict called 'e'
- Server based : activate the provided OSCFuncs that save the incoming data into Control Busses on the Server



Using the Foot Switch
---------------------
Several of the examples have footswitch definitiions (called a Button in the JSON).  Several actions are defined pressed, released and a short and long press.  The raw state is constantly broadcast.






Other worlds than the defaultWorld
=================================


Selecting a new world
---------------------
Use the dropdown element on the webpage to load a new world.  Generally this is pretty instant but there my be a lag if you are using a world that requires training a neural network.  Remember that this might change the OSC you receive.



simpleSphere
------------
Like a cubic hotspot by round! The relative positions are distances from the centre (from -1 to +1) and you can also receive angular positions relative to this point.  This file has one sphere that responds to the mid-point between the controllers.  Centered above the GameTrak at [0, 100, 0] with a radius of 50.  It also has foot switch control (b)

/s/rx -0.5
/s/ry 0.7
/s/rz -0.2
/s/rpitch 0.3
/s/ryaw 0.1
/s/rdistance 0.9
/s/active 0
/s/active 1


twoSpheres
----------
Two spheres both radius 100 - one called 'l' at [-50,100,0] and one called 'r' at [50,100,0]
Also has footswitch.



Multi-layer Perceptron Worlds
=============================
The Entangled GameTrak system implements an Sklearn Multi-layer Perceptron network as a regressor rather than a classifier.  This is a deliberate decision to avoid pre-defining gestures and actions but is intended to allow or encourage actions to emerge.  You are best looking at the actual JSON files to get an idea of how to construct your own.  The following summaries might help.

mlpOrgan
--------
This is the Percetron instrument as demonstrated in the worklshop - it uses the two players hand postions as input (6 in total) and has two hidden layers of 24 neurons each, then a 16 neuron output layer.  It is set to generate a 10 entry random training set (of 22 floats in total) each time it loads. 

OSC in addition to core data:

/mlp/n/2 0.45  : the 2 is the second network output will range from 0 to (Number of neurons in out layer - 1) 
/mlp/active 1
/mlp/active 0




mlpOrgan-training
-----------------
Same as the mlpOrgan but includes a fixed set of random training data


mlpSimple
---------
This has 
- a simple MLP (called 'mlp' with 6 inputs and 6 outputs, hidden layers are [16 and 12].  It has fixed training data and responds to both GameTrak controls.
- a cubic hotspot with the same dimensions called 'e'
- foot switch called 'b'



Defining your own worlds
========================
Ok this is getting advanced and as yet is not documented, hence I have not added a browser based editor yet.  Also note that there is NO ERROR checking at the moment - so edit the JSON file in something that checks the format for you e.g. Sublime. 

With that in mind the Ri Zero is running SAMBA with a password free guest account set up.  So try to access that through your network  smb://192.168.10.10  or even smb://entangled.  It might even show up in your network file system (e.g. OSX)

A folder should open that lists the worlds that are on the Pi Zero.  These core ones are write protected so simply save your new version with a different name.  The same worlds are in the "worlds" folder as part of this repositary.

Then go back to the browser tab and select "--- Reload World Files ---" to refresh the list if yours is not there.  You can also simply reload the webpage.

Finally select your file from the drop down list and enjoy!




