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
times=0
while [ ! -f $file_path ]
do
  if [ $times -ge 720 ]
  then
    echo "Waiting for test report time out!"
    exit
  fi
  sleep 5s
  let times=$times+1
done
echo "Ok, test run done!"
