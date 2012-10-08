#
# Added by Vincent at 2012/5/31
#

# ========= init check path =============
if [ -z $1 ]
then
  echo "Must input the file path"
  exit
else
  file_path=$1
fi

# ========= Begin to check ==============
while [ ! -f $file_path ]
do
  sleep 5s
done
echo "Ok, test run done!"
