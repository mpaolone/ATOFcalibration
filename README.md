# ATOFcalibration
Calibration software for ALERT ATOF

Prepare DB: first, under DB/ insert the data file "latest.sqlite", notice the current one is 0kb, you should download it from the CLAS DB i.e. https://clasweb.jlab.org/clas12offline/sqlite/ccdb/ 

Prepare run data: find the hipo4 file for calibration from CCDB, i.e. clas_021465.evio.00037.hipo

1) Run from the out/artifacts/ATOFcalibration_jar/ATOFcalibration.jar

$ java -jar ATOFcalibration.jar


or Run from the target/classes/org/clas/modules/ALERTCalibrationEngine.class


2) Text prompt will ask which calibration to run (T0, Veff, Atten, TW)

Chose H4, select your data file from the prompt window

Click Run

After processing events, click on an entry in the table to display relevant histograms.  This will also output calibration table to a text file in the main directory.



3) Source code under src/main/java/org/clas/modules/

for example, scint/ has the plotting and fitting codes for calibration


4) After  modification, run either:

$ mvn compile 

$ jar uf ./out/artifacts/ATOFcalibration_jar/ATOFcalibration.jar -C target/classes org/clas/modules/scint/ALERTT0Calibration.class
and other files you recompiled


or in your code luncher, find and run file   /target/classes/org/clas/modules/AppLauncher.class

Then you may run the .jar code again to visual the changes

