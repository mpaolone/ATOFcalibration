# ATOFcalibration
Calibration software for ALERT ATOF

usage:


Run from the out/artifacts/ATOFcalibration_jar/ATOFcalibration.jar
$ java -jar ATOFcalibration.jar


or Run from the target/classes/org/clas/modules/ALERTCalibrationEngine.class

Text prompt will ask which calibration to run (T0, Veff, Atten, TW)

After processing events, click on an entry in the table to display relevant histograms.  This will also output calibration table to a text file in the main directory.



Source code under src/main/java/org/clas/modules/
for example, scint/ has the plotting and fitting codes for calibration

after  modification, run either:
$ mvn compile 
$ jar uf ./out/artifacts/ATOFcalibration_jar/ATOFcalibration.jar -C target/classes org/clas/modules/scint/ALERTT0Calibration.class
and other files you recompiled

or in your code luncher, find and run file   /target/classes/org/clas/modules/AppLauncher.class



