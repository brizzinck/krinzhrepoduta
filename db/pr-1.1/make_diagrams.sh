#!/bin/sh
# Будує діаграми: інфологічна (нотація Чена, neato) і даталогічна (воронячі лапки, dot).
# Інфологічна генерується з опису в gen_infological.py, даталогічна з information_schema реальної бази car_rental.
set -eu
cd "$(dirname "$0")"
python3 gen_infological.py > er_infological.dot
python3 gen_datalogical.py > er_datalogical.dot
neato -Tpng -Gdpi=170 er_infological.dot -o img/er_infological.png
dot -Tpng -Gdpi=170 er_datalogical.dot -o img/er_datalogical.png
neato -Tsvg er_infological.dot -o img/er_infological.svg
dot -Tsvg er_datalogical.dot -o img/er_datalogical.svg
echo "діаграми побудовано"
