#!/bin/bash
set -eu
cd /mnt/NVME4/launchsystems-runs/ls09-performance-20261127/server
exec /usr/lib/jvm/java-21-openjdk/bin/java -Xmx2048M -Dcoasterslaunchsystems.performance=true @/mnt/NVME4/launchsystems-runs/ls09-performance-20261127/build/moddev/gameTestServerRunClasspath.txt @/mnt/NVME4/launchsystems-runs/ls09-performance-20261127/build/moddev/gameTestServerRunVmArgs.txt -Dfml.modFolders= net.neoforged.devlaunch.Main @/mnt/NVME4/launchsystems-runs/ls09-performance-20261127/build/moddev/gameTestServerRunProgramArgs.txt
