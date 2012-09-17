#
# Added by Vincent at 2012/6/6
#

# ========== Config ==================

workspace_root=`pwd`"/"
# Below path are all base on the workspace_root
project_root="GGPClient-Android/"
script_root="auto-script/"
qalib_root="OFQAAPI/"
sdk_root="../../ClientSDK-Android/workspace/"
sdk_path="sdk/"
gson_path="vendor/gson/"
PTR_path="vendor/Android-PullToRefresh/library/"
signpost_core_path="vendor/signpost/signpost-core/"
signpost_common_path="vendor/signpost/signpost-commonshttp4/"
local_pro="local.properties"
build_xml="build.xml"
apk_path="bin/GGPClient-Automation-release.apk"


# ========== Action Begin ==========

##Copy local.properties to all relied project
#
#if [ ! -f $local_pro ] || [ ! -f $build_xml ]
#then
#  echo "$local_pro or $build_xml is not exists, generate a new one"
#  sh $script_root"GenConfigFiles.sh"
#fi
#
##Copy local.properties
#cp $local_pro $project_root
#cp $local_pro $project_root"../OFQAAPI/"
#cp $local_pro $sdk_root$sdk_path
#cp $local_pro $sdk_root$gson_path
#cp $local_pro $sdk_root$PTR_path
#cp $local_pro $sdk_root$signpost_core_path
#cp $local_pro $sdk_root$signpost_common_path
#
##Copy build.xml
#flag="true"
#if [ "true" == $flag ]
#then
#  cp $build_xml $sdk_root$sdk_path
#  cp $build_xml $sdk_root$gson_path
#  cp $build_xml $sdk_root$PTR_path
#  cp $build_xml $sdk_root$signpost_core_path
#  cp $build_xml $sdk_root$signpost_common_path
#fi

cd $workspace_root$project_root
android update project -p .
cd $workspace_root$qalib_root
android update project -p .
cd $workspace_root$sdk_root$sdk_path
android update project -p .
cd $workspace_root$sdk_root$gson_path
android update project -p .
cd $workspace_root$sdk_root$PTR_path
android update project -p .
cd $workspace_root$sdk_root$signpost_core_path
android update project -p .
cd $workspace_root$sdk_root$signpost_common_path
android update project -p .

#Use ant to build the project
cd $workspace_root$project_root
ant clean > /dev/null
ant release >/dev/null
if [ -f $apk_path ]
then
  echo "build success..."
fi
