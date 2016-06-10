# Constant Propagation [![Build Status](https://travis-ci.org/Athithyaa/WalaTest.svg?branch=master)](https://travis-ci.org/Athithyaa/WalaTest)

## Introduction

This code repository implements constant propagation using the [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page) framework for
program analysis. Here are some reference for constant propagation:

1. [UCR](http://www.cs.ucr.edu/~gupta/teaching/201-14/Papers/const.pdf)
2. [Tel Aviv University](http://www.cs.tau.ac.il/~msagiv/courses/pa07/lecture2-notes-update.pdf) 

This project has been coded entirely on *Intellij IDEA 16*. Therefore to setup the development environment, import
this project directly onto Intellij IDEA 16.

This project uses Maven. Dependencies can be found here [pom.xml](pom.xml)

## Building and Execution
The following files are important for the functioning of the WALA framework:

1. J2SEClassHierarchyExclusions.txt
2. primordial.txt
3. primordial.jar.model
4. A jar file for analysis (As an example - SampleProgram.jar can be found in the repo)

The project can be built using
*mvn clean install*

Once the project is built compile the java classes (ConstantPropagation, Main and MyCallGraph)

Program can be executed by:

*java Main "jarfilename" "methodsignature" "graphviz"*

where the arguments are:

1. *jarfilename* name of the jar file to analyze
2. *methodsignature* name of the method to analyze. Example: *Program.fun(I)V*, represents the method void fun(int) in class Program.
3. *grahviz* either true of false. True for generating a pdf file("out.pdf") consisting of the Control Flow Graph. False otherwise.


## Algorithm for Constant propagation
Brief overview of the algorithm:

1. Using the WALA framework a Shrike Control Flow Graph is generated consisting of Blocks. Each block represents part of the program to
be analysed
2. The algorithm then iterates over the blocks(Graph traversal) and evaluates them.
3. Evaluation rules can be found out from the references.

## Simple Demonstration:
The SampleProgram.jar contains the following classes:

1. SampleProgramDriver - simple driver program to invoke a method in a different class
 ~~~~
    public class SampleProgramDriver {
    
        public static void main(String[] arg){
            Program program = new Program();
            program.fun(3);
        }
    }
 ~~~~
2. Program - class where the function *fun* resides. The program without any user input analyzes method *fun* by default.
 ~~~~
    public class Program {
        public Program(){
    
        }
    
        public void fun(int x){ // keep z as an argument
            int a=5,b=10,z=7; // keep z as a constant
            if(a==5){
                a=10;
                z=1;
                if(a==10){
                    a=15;
                    z=3;
                }else{
                    a=12;
                    z=4;
                }
            }else{
                a=x+3;
            }
        }
    }
 ~~~~
3. Sample run the program gives the following output:

*Variables after propagation: {b=10, a=15, z=3}*
meaning that at the end of method *fun*, "a" has value "15", "b" has value "10" and "z" has value "3". 


## TO:DO:

1. Program only works for fairly simple conditional constructs (simple if statements).
2. Loops have not been handled.
3. Conditional statements with complicated expressions have not been handled.
4. Instruction evaluation is not exhaustive.
