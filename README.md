# Lutron Binding

This binding integrates with [smartg4control](https://www.smartg4control.com/) control system.

**Note:** this is very much work in progress with no devicediscovery available and only the a few types of instruments used
-- no localisation

## Supported Things

This binding currently supports the following thing types:

*   dimmer
*   switch
*   sensor
*   ddp
*   logic

## Discovery

Not supported at the moment.  need help with the underlying protocol.

## Binding Configuration

This binding does not require any special configuration.  Make sure the RSIP  (https://www.smartg4control.com/products-1/Smart-Bus-Hybrid-Integration-Link-with-IP-SB-RSIP-DN-p62109034) is on the same UDP enabled network segment as your openhab server.

I have found since it is udp, there is no need to connect to specific ip or port for the server. i 've left configuration place for RSIP ip address but it is not used.  


## Thing Configuration

Use the smartcloud software (https://www.smartg4control.com/download-from-our-drive)to get subnet, deviceid and channel where required for each dimmer / relay etc.

```
Thing smartg4control:switch:office_light "light" @ "office" [subnetid = 1, deviceid = 21, channel = 11] 


```

## Channels

The following channels are supported:

| Thing Type      | Channel Type ID   | Item Type | Description                             |
|-----------------|-------------------|-----------|-----------------------------------------|
| dimmer          | lightlevel        | Dimmer    | Increase/decrease the light level       |
| switch          | switchstatus      | Switch    | On/off status of the switch             |
| sensor,ddp      | temperature       | Number    | temperature reading on sensor           |
| sensor          | motion            | Switch    | motion detection on sensor              |
| logic           | datetime          | DateTime  | DatetimeSetting on logic unit           |


## Binding Configuration

This binding does not require any special configuration.

## Full Example

demo.Things:

```
Thing smartg4control:dimmer:office_dimmer "dim light" @ "office" [subnetid = 1, deviceid = 40, channel = 5] 
Thing smartg4control:switch:study_light_1 "light" @ "study_1" [subnetid = 1, deviceid = 40, channel = 1] 
Thing smartg4control:sensor:office_bath "temp" @ "office_bath" [subnetid = 1, deviceid = 86] 
Thing smartg4control:sensor:studybath "movement" @ "study_bath" [subnetid = 1, deviceid = 25] 
Thing smartg4control:ddp:patio"ddp" @ "patio" [subnetid = 1, deviceid = 52]
Thing smartg4control:logic:logic1 "logic" @ "logic" [subnetid = 1, deviceid = 14]
```

demo.items:

```
Switch   Study_Bath_Light   "Study Bath Light"         <light>    (Study, gLight)      { channel = "smartg4control:switch:study_bath_light:switchstatus" }
Dimmer  OfficeDimmer   "Dimmer [%d %%]"	<slider> (Office)	 { channel = "smartg4control:dimmer:office_dimmer:lightlevel" }
Number 	PlayroomTemp "Playroom Temp [%.1f C]" <temperature> (gTemperature) { channel="smartg4control:ddp:play:temperature" }
DateTime LogicTime "Last Update [%1$tT - %1tF]" <light> (logic) { channel="smartg4control:logic:logic1:datetime" }


```

#

## Supported Things

This binding currently supports the following thing types:

| Thing            | Type ID | Description                                          |
|------------------|---------|------------------------------------------------------|
| sensor           | Thing   | 8in1/9in1/6in1/5in1/PIR                              |
| dimmer           | Thing   | Dimmer control 8 channel                             |
| switch           | Thing   | Relay 6/8/12 channel                                 |
| logic            | Thing   | logic unit                                           |

SmartG4Control have unique device IDS per equipment type, these can also be found on the smartcloud sofware the following has been implemented:
(https://www.smartg4control.com/products-1)

DEVICE_ID_DIMMER_8 = 550;
DEVICE_ID_RELAY_12 = 440;
DEVICE_ID_RELAY_6 = 426;
DEVICE_ID_RELAY_8 = 428;
DEVICE_ID_LOGIC = 1108;
DEVICE_ID_DDP = 149;
DEVICE_ID_8IN1 = 319;
DEVICE_ID_9IN1 = 309;
DEVICE_ID_6IN1 = 313;
DEVICE_ID_5IN1 = 314;
DEVICE_ID_PIR = 135;

## Thing Configurations

| Thing            | Config       | Description                                                           |
|------------------|--------------|-----------------------------------------------------------------------|
| ALL              | subnetid     | sg4 subnet, usually 1                                                 |
|                  | devideid     | unique deviceid set at installation time per device on sg4 network    |
| dimmer / switch  | channel      | SG4 channel for the relay / dimmer                                    |


## Channels

The following channels are supported:

| Thing Type                 | Channel ID   | Item Type | Description                        |
|----------------------------|--------------|-----------|------------------------------------|
| ra-dimmer                  | lightlevel   | Dimmer    | Increase/Decrease dimmer intensity |
| ra-switch/ra-phantomButton | switchstatus | Switch    | On/Off state of switch             |



## Discovery

This binding does not support any discovery yet.
