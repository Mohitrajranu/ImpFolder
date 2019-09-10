#! /bin/bash

#Author:Mohit Raj

varDate=`./prevoius60mins.p1`
echo $varDate
echo "60 Minutes Back "$varDate

FILE_PATTERN="VAS_SDRSE00_"$varDate
echo $FILE_PATTERN

#cd /home/mbas/LOGS/cdr/
#cd /home/mbas/test
#cd /app/cdr
cd /home/mbas/BackEndProcess/cronjobs

for file1 in `ls $FILE_PATTERN*.cdr`;do
for file2 in `ls $FILE_PATTERN*.tok`;do
VAS_cdr=`echo $file1|awk -F '[_.]' '{print $4}'`;
VAS_tok=`echo $file2|awk -F '[_.]' '{print $4}'`;
if [[ $VAS_cdr == $VAS_tok ]];
then
echo "$file1 is a file and the corresponding token file for it is $file2."
if [ -s $file1 ];
then
echo "$file1 is a complete and it will be processed and moved to new location."
echo $file1
NAME=$file1
echo "hhhhhh"$NAME
lf=`head -1 $NAME`
ln=`tail -2 $NAME | head -1`
j=`echo $lf | cut -d \| -f 18`
h2=`echo $ln | cut -d \| -f 18`
echo "j="$j
echo "h2="$h2
wd=`wc -l $NAME`
wd=`echo $wd| cut -d " " -f 1`
if [ "$wd" != "0" ]; then
echo "WC ="$wd
sed -i '1i\'"80|$j" $NAME

echo "90|$wd|$h2">>$NAME
mv -u $file1 /rocra/subex/DataDir/poll/platform/platform/sdr_transaction
if [ -s $file2 ];
then
echo "$file2 is a file and size is greater than zero."
else
echo "$file2 is an empty file and deleting now..."
rm -rf $file2
fi
fi
else
echo "$file is empty file.wait for it to get completed."
fi
else 
echo "Filename and its corresponding token file does not match."
fi
done
done
