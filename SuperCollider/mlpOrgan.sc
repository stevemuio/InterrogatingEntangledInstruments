// **********************************
// mlpOrgan
// **********************************
// See "MLP world documentation - mlpOrgan



// **********************************
// Using a global dictionary
// **********************************

(
// this code snippet sets up an OSC receiver function for each of the standard data points for the "defaultWorld" entangled world

//first open port 7400
thisProcess.openUDPPort(7400);

// disctionary called e to hold the standard data
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


)


// MLP data OSC receivers

(
var maxNumberOfInputs, baseAddress;

// base adress that inputs come to e.g. /n/2 .. /n/3 ..
baseAddress = "/mlp/n";

//set the number of inputs in the series
maxNumberOfInputs = 16;

e = Array.newClear(maxNumberOfInputs);
maxNumberOfInputs.do { arg item;
	OSCFunc.newMatching({|msg, time, addr, recvPort| e[msg[0].asString.split[3].asInteger] = msg[1];}, baseAddress +/+ item);
};
)


//testing
m = NetAddr("127.0.0.1", NetAddr.langPort); // loopback

m.sendMsg("/n/15", 0.78);

//retrieving

e[1]

e




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

)


// route the MLP data to control busses
(
var maxNumberOfInputs, baseAddress;

// base adress that inputs come to e.g. /n/2 .. /n/3 ..
baseAddress = "/mlp/n";

//set the number of inputs in the series
maxNumberOfInputs = 16;

n = Array.newClear(maxNumberOfInputs);
maxNumberOfInputs.do { arg item;
	e[item] = Bus.control(s,1);
	OSCFunc.newMatching({|msg, time, addr, recvPort| e[msg[0].asString.split[3].asInteger].set(msg[1])}, baseAddress +/+ item);

};

// this function gets called when the the mid-point enters or exits the defined area
OSCFunc.new({|msg, time, addr, recvPort|

	if (msg[1] == 1,
	{
		"we are engaged - start synths".postln;
		z = SynthDef(\rlpf, { |out, ffreq, rq, vol = 0.3|
    Out.ar(out, RLPF.ar(WhiteNoise.ar(vol), ffreq*1000, rq))
			}).play(s, [\ffreq, e[1].asMap, \rq, e[4].asMap, \vol, e[8].asMap]);

	},{
			"play over - STOP".postln;
			z.free;
})}, '/mlp/active'); // end of engaged start synth OSCFunc

)


// ****************
// testing Controll busses
// ****************

e
e[1].set(0.6);
e[4].set(0.2);
e[8].set(0.2);
// testing purposes
m = NetAddr("127.0.0.1", NetAddr.langPort); // test sending neta address

m.sendMsg("/mlp/active", 1);
m.sendMsg("/mlp/n/1", 0.7);
m.sendMsg("/mlp/n/4", 0.2);
m.sendMsg("/mlp/n/8", 0.5);
m.sendMsg("/mlp/active", 0);




