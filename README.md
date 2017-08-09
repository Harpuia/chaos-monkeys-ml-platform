# CMUChaosMonkeysExperimentationPlatform


## Prerequisites

You will need to install MySQL, the latest JRE (and JDK if you would like to modify the code and rebuild), the latest Maven, Node, NPM and R.
A prerequisite to running the algorithm services is having R installed, with the commands "rscript" and "r" configured to be executed in command line.
Before running the system, make sure you create databases ("configurationdb" and "logdb") from the scripts in the SQL folder (latest versions), then modify the configuration files: "JerseyServices\dbConfig.ini", "CoordinationService\config\dbConfig.json" and "CoordinationService\config\logDbConfig.json" to point them to the databases.

## To Build Jersey Services

```
$ bash JerseyServices/build.sh
```

## To Run All Jersey Services

```
$ cd JerseyServices/launch_scripts/
$ bash run_jersey_all.sh
```

## To Run Algorithm Input Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_algr_input.sh
```

## To Run Data Input Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_data_input.sh
```

## To Run Training Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_training.sh
```

## To Run Execution Service

```
$ cd JerseyServices/launch_scripts/
$ bash run_algr_input.sh
```

## To Build and Run the Coordination Service

```
$ cd CoordinationService
$ npm install
$ npm start
```
