#!/bin/bash

mill clean indigo-plugin[2.12.10]
mill clean indigo-plugin[2.13.3]

mill indigo-plugin[2.12.10].compile
mill indigo-plugin[2.13.3].compile

mill indigo-plugin[2.12.10].publishLocal
mill indigo-plugin[2.13.3].publishLocal

