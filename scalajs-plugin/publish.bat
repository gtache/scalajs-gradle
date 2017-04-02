@echo off
setlocal enabledelayedexpansion
set "scalaVersion=2.11"
set "subVersion=8"
set "scalaJSVersion=0.6.15"
set "file=gradle.properties"
cd scalajs-plugin
(echo scalaVersion=!scalaVersion!
echo subVersion=!subVersion!
echo scalaJSVersion=!scalaJSVersion!)>%file%
call gradlew.bat uploadArchives -Pupload
set "scalaVersion=2.12"
set "subVersion=1"
(echo scalaVersion=!scalaVersion!
echo subVersion=!subVersion!
echo scalaJSVersion=!scalaJSVersion!)>%file%
call gradlew.bat uploadArchives -Pupload
