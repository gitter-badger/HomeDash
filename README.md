HomeDash
============================

**HomeDash** is a Java software that bring everything you need into a single place. 


## Requirements
To install and run **HomeDash** all you need is *GIT*, *Java 7 SDK* and a *MYSQL* database server.

## Installation
### First time
```
git clone https://github.com/lamarios/HomeDash
```
```
cd HomeDash
```


Start **HomeDash**:

```
./homedash.sh start
```
Some information will be asked like your database details, the port you want HomeDash to run and the VM settings.

The first time **HomeDash** is ran it may take quite some time as the [PlayFramework](http://www.playframework.com) and the required dependencies will be downloaded.

To access **HomeDash** browse to http://localhost:9000 (default port).

### Update
To update HomeDash to the last available version from GitHub:
```
./homedash.sh update
```

### Available commands
```
./homedash.sh start
```
```
./homedash.sh stop
```
```
./homedash.sh restart
```
```
./homedash.sh update
```

[Activator](https://typesafe.com/community/core-tools/activator-and-sbt) commands are also available for those who want to get more control.

## Available modules

|Module        | Description  |
|------------- | -------------|
|System Info   | CPU & Ram Usage. |
|Hard Disk   | Display current usage of a mount point or a specific folder. |
|Transmission  | List of torrents, upload & download speed, add/pause/remove torrents and switch alternate speed. |
|UPnP port mapping  | Shows all the ports currently opened through UPnP. Add/Save ports to make it persistent. |
|Dynamic DNS  | Check and update your public IP address to selected providers (no-ip, dyndns & ovh currently supported). |
|Twitter  | Browse your own timeline. |
| Couchpotato  | Add movies to a CouchPotato instance. |
|Sickbeard  | Displays the incoming episodes of the TV SHows you're following. |
| Public Google Calendar  | Show upcoming events of a Google public calendar |
| OS X  | Display the dock of your OS X, start and quit applications. *(HomeDash must be running on a Mac)* |
| Yamaha Amplifier  | Control a Yamaha amplifier. Tested on a Yamaha RX-V573. |
|Lychee   | Upload, browse and share easily pictures from [Lychee](http://lychee.electerious.com/). |



## Libraries and Frameworks used

### Java
* [PlayFramework](http://playframework.com)
* [Twitter4j](http://twitter4j.org/en/index.html)
* [GSON](https://code.google.com/p/google-gson/)
* [GSON fire](https://github.com/julman99/gson-fire)
* [WeUpnp](https://github.com/bitletorg/weupnp)
* [DD-Plist](https://code.google.com/p/plist/)
* [Apache HTTP client](http://hc.apache.org/)
* [Benow Java Transmission client](http://benow.ca/projects/Transmission%20Java%20Client/)

### Javascript
* [jQuery](http://jquery.com/)
* [jQuery UI](http://jqueryui.com/)
* [jQuery countTo](https://github.com/mhuggins/jquery-countTo)

###CSS
* [Animate.less](https://github.com/machito/animate.less)
* [Bootstrap](http://getbootstrap.com/)
