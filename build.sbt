name := "ConstantPropagationScala"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.ibm.wala" % "com.ibm.wala.core" % "1.3.8"

libraryDependencies += "com.ibm.wala" % "com.ibm.wala.shrike" % "1.3.8"

libraryDependencies += "com.ibm.wala" % "com.ibm.wala.util" % "1.3.8"

libraryDependencies += "junit" % "junit" % "4.12" % Test

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0-M15" % Test

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
