#!/bin/sh
# Докази: код сервісу не змінився при додаванні MarkdownFormat; сервіс не створює залежностей;
# методів для сторінок немає в CsvFormat. Етап 1 (до додавання) лежить у snapshots/stage1_three_formats.
cd "$(dirname "$0")" || exit 1
T=../../tools
A=snapshots/stage1_three_formats/ua/duikt/lavrishko/reports
B=src/main/java/ua/duikt/lavrishko/reports
R=results/evidence.txt
$T/runlog.sh -t $R -- diff -rq snapshots/stage1_three_formats src/main/java
$T/runlog.sh $R -- git diff --no-index --stat $A/service/ReportService.java $B/service/ReportService.java
$T/runlog.sh $R -- git diff --no-index --stat $A/service/PagedReportService.java $B/service/PagedReportService.java
$T/runlog.sh $R -- git diff --no-index --stat $A/format/ReportFormat.java $B/format/ReportFormat.java
$T/runlog.sh $R -- git diff --no-index --stat $A/format/AbstractReportFormat.java $B/format/AbstractReportFormat.java
$T/runlog.sh $R -- sha256sum $A/service/ReportService.java $B/service/ReportService.java
$T/runlog.sh $R -- git diff --no-index --stat $A/app/Main.java $B/app/Main.java
$T/runlog.sh $R -- diff -u $A/app/Main.java $B/app/Main.java
$T/runlog.sh $R -- grep -rn "new \|import ua.duikt.lavrishko.reports.format\|import ua.duikt.lavrishko.reports.source" $B/service
$T/runlog.sh $R -- grep -rn "renderPages" $B/format
$T/runlog.sh $R -- grep -rln "implements PagedFormat" $B/format
