#!/bin/sh
# Докази, що код обробки не змінився при додаванні Audiobook, та що в ньому немає перевірок типу.
# Етап 1 лежить у snapshots/stage1_three_kinds (стан до додавання), етап 2 у src/main/java.
cd "$(dirname "$0")" || exit 1
T=../../tools
A=snapshots/stage1_three_kinds/ua/duikt/lavrishko/media
B=src/main/java/ua/duikt/lavrishko/media
R=results/evidence.txt
$T/runlog.sh -t $R -- diff -rq snapshots/stage1_three_kinds src/main/java
$T/runlog.sh $R -- git diff --no-index --stat $A/Main.java $B/Main.java
$T/runlog.sh $R -- git diff --no-index --stat $A/LibraryReport.java $B/LibraryReport.java
$T/runlog.sh $R -- git diff --no-index --stat $A/MediaContent.java $B/MediaContent.java
$T/runlog.sh $R -- sha256sum $A/LibraryReport.java $B/LibraryReport.java
$T/runlog.sh $R -- diff -u $A/Main.java $B/Main.java
$T/runlog.sh $R -- grep -rnE "instanceof|getClass\(\)|\((Film|Song|Podcast|Audiobook)\) *[a-z]" src/main/java
