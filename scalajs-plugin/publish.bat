@echo off
setlocal enabledelayedexpansion
set "scalaVersion=2.11"
set "subVersion=8"
set "file=gradle.properties"
for /l %%j in (9,1,15) do (
	set "scalaJSVersion=0.6.%%j"
	cd scalajs-plugin
	(echo scalaVersion=!scalaVersion!
	echo subVersion=!subVersion!
	echo scalaJSVersion=!scalaJSVersion!)>%file%
	call gradlew.bat uploadArchives -Pupload
)
set "scalaVersion=2.12"
set "subVersion=1"
for /l %%k in (13,1,15) do (
	set "scalaJSVersion=0.6.%%k"
	(echo scalaVersion=!scalaVersion!
	echo subVersion=!subVersion!
	echo scalaJSVersion=!scalaJSVersion!)>%file%
	call gradlew.bat uploadArchives -Pupload
)