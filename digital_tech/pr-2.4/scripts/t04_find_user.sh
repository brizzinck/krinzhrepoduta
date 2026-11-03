#!/bin/bash
# t04: шукає користувача $1 у списку користувачів (/etc/passwd) і груп (/etc/group)
if [ $# -ne 1 ]; then
    echo "Використання: $0 імя_користувача"
    exit 1
fi
echo "Пошук у /etc/passwd:"
grep "^$1:" /etc/passwd || echo "користувача $1 не знайдено"
echo "Пошук у /etc/group (група з таким іменем або членство):"
grep -E "^$1:|[:,]$1(,|$)" /etc/group || echo "у групах $1 не знайдено"
