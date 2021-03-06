# AirboxDataLake

The Airbox Data Lake is an experiment in exposing digital data that is inherently FAIR (rather than FAIR by virtue of the holding system.)   The key architectural components of the experiment are PID Kernel Information (PID KI) (see http://doi.org/10.15497/RDA00031) and the Cordra object store (www.cordra.org) for holding type information (see https://www.rd-alliance.org/groups/data-type-registries-wg.html), and test Handles obtained from the E-RPID testbed at Indiana University (https://github.com/rpidproject). 

# Installation Guide

## Software Dependencies

1. Apache Maven V3.0 or higher
2. JDK V1.6 or higher
3. MongoDB Server V4.0 or higher
4. Cordra V2.0
5. Unis service with IBP
6. Unis client

## Hardware Requirement

1. This software can be deployed on physcial resources or on a VM instance that has a public network interface.
2. For public access, 2 open ports are required with iptables rules allowing traffic through the firewall for both LIDO and Cordra.

## Building the Dependencies

1. Cordra (Object Service): 

   1. download the Cordra service from https://cordra.org/index.html
   2. follow the instructions to install and run the Cordra service
      1. create the proper types in Cordra (please follow the `CordraREADME.md` under the `doc`)

2. Unis Service with IBP

   1. **coming soon**

3. Unis Client

   1. create the python virtual enviroment

   2. donwload the Unis Client

      ```
      git clone https://github.com/datalogistics/libdlt.git
      ```

   3. Install the Client

      ```
      pip3 install google-auth
      pip3 install -r requirements.txt
      python3 setup.py develop
      ```

## Building the Source

Check out source codes:

```
git clone https://github.com/Data-to-Insight-Center/LIDO.git
```

Edit the `application.properties` file under `src/main/resources` and set your port for the service

```
vi LIDO/src/main/resources/application.properties
```

Edit the `config.properties` file under `src/main/java/indiana/edu/property` and set the information for Handle System, Cordra, and temporal folder for service

```
vi LIDO/src/main/java/indiana/edu/property/config.properties
```

Edit the `Property.java` file under `src/main/java/indiana/edu/property` and set the correct path to the `config.properties` file

```
vi LIDO/src/main/java/indiana/edu/property/Property.java
```

Based on the information in temporary folder, create the relative folders by using

```
mkdir Path/folderName
```

Edit the `href` field in `.html` files under `src/main/resources/templates` and set the proper service address

```
vi xxxx.html
```

Build and run the Airbox Data Lake

```
mvn spring-boot:run
```

# Contributing

The LIDO software is licensed under an Apache 2.0 License.  Cordra licensing information can be found on the Cordra site at https://www.cordra.org/.

# Release History

* 0.1 1st release 2019.10.3
