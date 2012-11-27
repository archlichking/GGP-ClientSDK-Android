#
# Added by Vincent at 2012/5/31
#

# ========== Config ==================

mode="device"
avd_name="my_avd"
apk_path="GGPClient-Android/bin/GGPClient-Automation-release.apk"
main_activity="com.openfeint.qa.ggp/.MainActivity"
dailyRun_activity="com.openfeint.qa.ggp/.DailyRunActivity"

# ========== Init Parameter ==========
# params:
#       $1 : mode, device or emulator
#       $2 : device name or avd name
#       $3 : apk path of automation app

if [ -z $1 ]
then
  echo "Must specify a mode: emulator or device"
  exit
else
  mode=$1
fi

if [ ! -z $3 ]
then
  apk_path=$3
fi

# =========== Action Begin ============

killEmulators()
{
  for pid in `ps aux | grep emulator | grep avd | awk '{print $2}'`
  do
    kill -9 $pid
    sleep 3s
  done
}

launchNewEmulator()
{
  echo "Starting emulator...."
  emulator -avd $avd_name &

  # Wait until a new emulator is launched
  sleep 20s
  new_emulator=`adb devices | grep emulator | tail -1 | awk '{print $1}'`
  times=0
  while [ -z $new_emulator ] || [ $last_emulator == $new_emulator ] || [ 1 ]
  do
    if [ $times -ge 3 ]
    then
      echo "Launch failed, need retry"
      return 1
    fi
    echo "emulator still launching..."
    sleep 5s
    let times=$times+1
    new_emulator=`adb devices | grep emulator | tail -1 | awk '{print $1}'`
  done
  echo "New emulator launched is "$new_emulator
}
if [ $mode == "emulator" ]
then
# ====== Emulator mode ======
## init avd name
  if [ ! -z $2 ]
  then
    avd_name=$2
  fi

## Got last emulator
  last_emulator=`adb devices | grep emulator | tail -1 | awk '{print $1}'`
  if [ -z $last_emulator ]
  then
    last_emulator="empty"
  fi
  echo "Last emulator before launch is "$last_emulator

## Launch avd
  while [ 1 ]
  do
    launchNewEmulator
    if [ $? -ne 0 ]
    then
      killEmulators
      launchNewEmulator
    else
      break
    fi
  done

## wait emulator's status is become ready
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
  target=$new_emulator

# ====== Device mode ======
else
## init device name
  if [ -z $2 ]
  then
    echo "Must specify a device name"
    exit
  else
    device_name=$2
  fi
  target=$device_name
fi

# ====== Install app & run ======

# Uninstall first
sleep 2s
echo Uninstall automation app first...
adb -s $target uninstall com.openfeint.qa.ggp

# Install automation app
sleep 2s
echo Install automation app....
adb -s $target install -r $apk_path 
while [ $? -ne 0 ]
do
  echo "try install again..."
  sleep 5s
  adb -s $target install -r $apk_path
done

# Start MainActivity for some Global context
sleep 3s
echo "Starting MainActivity for global context"
adb -s $target shell am start -n $main_activity
# Start DailyRunActivity to begin test
sleep 5s
echo "Let start to run..."
adb -s $target shell am start -n $dailyRun_activity
