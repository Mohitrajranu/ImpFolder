#!/bin/bash
#############################################################
#                                                           #
#       Image Conversion Script                             #
#       Mohit Raj                                           #
#       mohitraj.ranu@gmail.com                             #
#       01/04/2019                                          #
#                                                           #
#                                                           #
#############################################################
home=/home/LinuxScript/ImageConv/TestImage


cd $home
for f in `find . -type f \( -name "*.tif" -or -name "*.tiff" \)`;do  
    chmod 777 $f
	
    echo "Converting $f"
    filename=`echo $f | rev | cut -f 2- -d '.' | rev`	
    convert "$f"  "$(basename "$filename" .tif).jpg" 
done

removeFile = `find . -mmin +59 -type f \( -name "*.tif" -or -name "*.tiff" \) -exec rm -fv {} \;`
echo $removeFile

#!/bin/bashs
LOG_BASE=/home/LinuxScript/ImageConv/FinalOpImage
ImageConv_Running=`ps -ef |  grep -w /home/LinuxScript/ImageConv/[I]mageConverter.sh |grep -v grep | awk '{print $2}' | wc -l`

if [ $ImageConv_Running -ge 1 ]
   then
    echo "script is running"
    else
    echo "script is not running, Starting it"
    sh /home/LinuxScript/ImageConv/ImageConverter.sh  1>>$LOG_BASE/ImageConversion_out.log.`date +%Y-%m-%d` 2>>$LOG_BASE/ImageConversion_err.log.`date +%Y-%m-%d`
fi

for img in *.png; do
    filename=${img%.*}
    convert "$filename.png" "$filename.jpg"
done

mogrify -format jpg *.png
[yY] | [yY][Ee][Ss] 
case "$1" in
    *.[tT][iI][fF])  convert "$1"  "$(basename "$filename" .tif).jpg" ;;
    *.[tT][iI][fF][fF]) convert "$1"  "$(basename "$filename" .tiff).jpg";;
    *.[pP][nN][gG])  convert "$1"  "$(basename "$filename" .png).jpg" ;;
    *) ;;
esac

