#!/bin/bash
git init
git add .
git commit -m "Initial commit"
sbt11 compile run clean
v src/main/scala/hw.scala
