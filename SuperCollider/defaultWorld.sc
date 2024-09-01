// **********************************
// DefaultWorld
// **********************************
// See "entangled world documentation - DefaultWorld
// Used in Elastiphone and Stickatron

// **********************************
// Using a global dictionary
// **********************************

(
// this code snippet sets up an OSC receiver function for each of the data points for the "defaultWorld" entangled world

//first open port 7400
thisProcess.openUDPPort(7400);

// disctionary called e to hold the entangled data
e = Dictionary.new;

// standard data

OSCFunc.new({|msg, time, addr, recvPort| e.add(\pitch -> msg[1]);}, "/pitch"); // save pitch
OSCFunc.new({|msg, time, addr, recvPort| e.add(\roll -> msg[1]);}, '/roll'); // save roll
OSCFunc.new({|msg, time, addr, recvPort| e.add(\yaw -> msg[1]);}, '/yaw'); // save yaw

// this function gets called when both the players pick up
OSCFunc.new({|msg, time, addr, recvPort|
	e.add(\engaged -> msg[1]);
	if (msg[1] == 1,
	{
		"we are engaged - start your enginge".postln;
	},{
		"STOP everything - play over".postln;}
)}, '/engaged');

// Hotspot defined data

//relative position of the mid-point between the players within the defined play space called "m" rx = left-right, ry = up-down , rz = forwards and backwards
OSCFunc.new({|msg, time, addr, recvPort| e.add(\rx -> msg[1]);}, '/e/rx'); // save rx
OSCFunc.new({|msg, time, addr, recvPort| e.add(\ry -> msg[1]);}, '/e/ry'); // save ry
OSCFunc.new({|msg, time, addr, recvPort| e.add(\rz -> msg[1]);}, '/e/rz'); // save rz

// this function gets called when the the mid-point enters or exits the defined area
OSCFunc.new({|msg, time, addr, recvPort|
	e.add(\hotspotActive -> msg[1]);
	if (msg[1] == 1,
	{
		"hotspot enabled - start special synths".postln;
	},{
		"STOP special synths - play over".postln;}
)}, '/e/active');

)

// wghen you want the data back
e.postln;

// standard variables
e[\pitch]
e[\roll]
e[\yaw]
e[\engaged]

/default world hotspot data
e[\rx]
e[\ry]
e[\rz]
e[\hotspotActive]

// testing purposes
m = NetAddr.new("127.0.0.1", NetAddr.langPort); // test sending neta address

m.sendMsg("/e/enabled", 1);
m.sendMsg("/pitch", 0.5);
m.sendMsg("/yaw", 0.2);
m.sendMsg("/roll", 0.5);
m.sendMsg("/e/enabled", 0);

//*********************************
// Footswitch events and state saved globally
(

// respond to the state messages
OSCFunc.new({|msg, time, addr, recvPort|
	e.add(\footswitch -> msg[1]);
	if (msg[1] == 1,
	{
		"foot switch pressed".postln;
	},{
		"foot switch released".postln;}
)}, '/b/state');

// respond to the short press event
OSCFunc.new({|msg, time, addr, recvPort|
	"short press !!".postln;
}, '/b/short');


// respond to the long press event
OSCFunc.new({|msg, time, addr, recvPort|
	"long press !!".postln;
}, '/b/long');

)

e[\footswitch]



// **********************************
// Using serverside control Busses
// **********************************

//Alternatively you might want to run all the synth stuff on the Server - this code sets up a control bus for each of the inputs and updates it whenever a new value is received

(
s = Server.local;
s.boot();
)



(
//disctionary called e to hold the entangled control busses
e = Dictionary.new;

// not very pretty but clear way of setting upo the control busses in an understable way!

// standard data
e.add(\pitch -> Bus.control(s,1));
e.add(\roll -> Bus.control(s,1));
e.add(\yaw -> Bus.control(s,1));
e.add(\enabled -> Bus.control(s,1));


// now set up the OSC Functions

// standard data
OSCFunc.new({|msg, time, addr, recvPort| e[\pitch].set(msg[1]);}, '/pitch'); // save pitch
OSCFunc.new({|msg, time, addr, recvPort| e[\roll].set(msg[1]);}, '/roll'); // save rollœ
OSCFunc.new({|msg, time, addr, recvPort| e[\yaw].set(msg[1]);}, '/yaw'); // save yaw

// this function gets called when both the players pick up
OSCFunc.new({|msg, time, addr, recvPort|
	e[\enabled].set(msg[1]);
	if (msg[1] == 1,
	{
		"we are engaged - start your engine".postln;
	},{
		"STOP everything - play over".postln;}
)}, '/engaged');



// Hotspot defined data
// Hotspot defined busses
e.add(\rx -> Bus.control(s,1));
e.add(\ry -> Bus.control(s,1));
e.add(\rz -> Bus.control(s,1));
e.add(\hotspotenabled -> Bus.control(s,1));

//relative position of the mid-point between the players within the defined play space called "m" rx = left-right, ry = up-down , rz = forwards and backwards
OSCFunc.new({|msg, time, addr, recvPort| e[\rx].set(msg[1]);}, '/e/rx'); // save rx
OSCFunc.new({|msg, time, addr, recvPort| e[\ry].set(msg[1]);}, '/e/ry'); // save ry
OSCFunc.new({|msg, time, addr, recvPort| e[\rz].set(msg[1]);}, '/e/rz'); // save rz

OSCFunc.new({|msg, time, addr, recvPort| e[\hotspotenabled].set(msg[1]);}, '/e/active'); // save enabled

// this function gets called when the the mid-point enters or exits the defined area
OSCFunc.new({|msg, time, addr, recvPort|

	if (msg[1] == 1,
	{
		"hotspot enabled - start special synths".postln;
		z = SynthDef(\rlpf, { |out, ffreq, rq, vol = 0.3|
    Out.ar(out, RLPF.ar(WhiteNoise.ar(vol), ffreq*1000, rq))
			}).play(s, [\ffreq, e[\pitch].asMap, \rq, e[\roll].asMap, \vol, e[\yaw].asMap]);

	},{
		"STOP special synths - play over".postln;
		z.free;
})}, '/e/active'); // end of hot spot enabled start synth OSCFunc


)


// ****************
// testing Controll busses
// ****************


e[\pitch].set(0.6);
e[\roll].set(0.2);
e[\yaw].set(0.2);
// testing purposes
m = NetAddr("127.0.0.1", NetAddr.langPort); // test sending neta address

m.sendMsg("/m/engaged", 1);
m.sendMsg("/pitch", 0.7);
m.sendMsg("/yaw", 0.2);
m.sendMsg("/roll", 0.5);
m.sendMsg("/m/engaged", 0);


//*********************************
// Footswitch events and state saved to control bus
(

e.add(\footswitch -> Bus.control(s,1));
// respond to the state messages
OSCFunc.new({|msg, time, addr, recvPort|
	e.[\footswitch].set(msg[1]);
	if (msg[1] == 1,
	{
		"foot switch pressed".postln;
	},{
		"foot switch released".postln;}
)}, '/b/state');

// respond to the short press event
OSCFunc.new({|msg, time, addr, recvPort|
	"short press !!".postln;
}, '/b/short');


// respond to the long press event
OSCFunc.new({|msg, time, addr, recvPort|
	"long press !!".postln;
}, '/b/long');

)

e[\footswitch]
