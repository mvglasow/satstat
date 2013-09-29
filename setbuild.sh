#!/bin/bash
git update-index --assume-unchanged res/raw/build.txt
echo git-`git log -1 --pretty=format:%h``git diff-index --quiet HEAD || echo -dirty` > res/raw/build.txt
