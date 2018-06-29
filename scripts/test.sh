#!/usr/bin/env bash

##
# This script runs stress tests for the query of genotype files.
##


## Parameters

# Repository folder
repo=/mnt/work/marc/tools/Hunt

# Number of threads
nThreads=16
ram="32G"

## Script

# Run one vcf file at a time
echo "Testing per chromosome"
for chr in {1..22}
do
    java -Xmx$ram -Djava.util.concurrent.ForkJoinPool.common.parallelism=$nThreads -cp $repo/bin/hunt-0.0.1/hunt-0.0.1.jar no.uib.hunt.tests.SingleVcfTest $chr
done
echo
echo

# Run multiple vcf files
echo "Testing per chromosome"
java -Xmx$ram -Djava.util.concurrent.ForkJoinPool.common.parallelism=$nThreads -cp $repo/bin/hunt-0.0.1/hunt-0.0.1.jar no.uib.hunt.tests.MultipleVcfTest



