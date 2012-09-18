#
# Added by Vincent at 2012/5/31
#

# ========== Config ==================

avd_name="my_avd"
apk_path="GGPClient-Android/bin/GGPClient-Automation-release.apk"
main_activity="com.openfeint.qa.ggp/.MainActivity"
dailyRun_activity="com.openfeint.qa.ggp/.DailyRunActivity"

# ========== Init Parameter ==========
# params:
#       $1 : avd name
#       $2 : apk name
#       $3 : activity path
if [ ! -z $1 ]
then
  avd_name=$1
fi

if [ ! -z $2 ]
then
  apk_path=$2
fi

if [ ! -z $3 ]
then
  activity=$3
fi

# ========== Action Begin ==========

# Got last emulator
last_emulator=`adb devices | grep emulator | tail -1 | awk '{print $1}'`
if [ -z $last_emulator ]
then
  last_emulator="empty"
fi
echo "Last emulator before launch is "$last_emulator

# Launch avd
echo "Starting emulator...."
emulator -avd $avd_name &

# Wait until a new emulator is launched
sleep 20s
new_emulator=`adb devices | grep emulator | tail -1 | awk '{print $1}'`
times=0
while [ -z $new_emulator ] || [ $last_emulator == $new_emulator ]
do
  if [ $times -ge 3 ]
  then
    echo "Launch failed, need retry"
    exit
  fi
  echo "emulator still launching..."
  sleep 5s
  let times=$times+1
  new_emulator=`adb devices | grep emulator | tail -1 | awk '{print $1}'`
done
echo "New emulator launched is "$new_emulator

# wait emulator's status is become ready
if [ $last_emulator != $new_emulator ]
then
  emulator_status=`adb devices | grep emulator | tail -1 | awk '{print $2}'`
  while [ $emulator_status != "device" ]
  do
    sleep 3s
    emulator_status=`adb devices | grep emulator | tail -1 | awk '{print $2}'`
  done
  echo "Launch done..."
else
  echo "Launch failed, need retry"
  exit
fi

# Uninstall first
sleep 2s
echo Uninstall automation app first...
adb -s $new_emulator uninstall com.openfeint.qa.ggp

# Install automation app
sleep 2s
echo Install automation app....
adb -s $new_emulator install -r $apk_path 
while [ $? -ne 0 ]
do
  echo "try install again..."
  sleep 5s
  adb -s $new_emulator install -r $apk_path
done

# Start MainActivity for some Global context
sleep 3s
echo "Starting MainActivity for global context"
adb -s $new_emulator shell am start -n $main_activity
# Start DailyRunActivity to begin test
sleep 5s
echo "Let start to run..."
adb -s $new_emulator shell am start -n $dailyRun_activity
