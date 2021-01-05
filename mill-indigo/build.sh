#!/usr/bin/env bash

mill clean mill-indigo
mill mill-indigo.compile
mill mill-indigo.publishLocal
